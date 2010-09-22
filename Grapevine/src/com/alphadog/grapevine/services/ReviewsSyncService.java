package com.alphadog.grapevine.services;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import com.alphadog.grapevine.R;
import com.alphadog.grapevine.db.GrapevineDatabase;
import com.alphadog.grapevine.db.ReviewsTable;
import com.alphadog.grapevine.models.Review;
import com.alphadog.grapevine.services.LocationUpdateTrigger.LocationResultExecutor;

public class ReviewsSyncService extends WakeEventService {

	public static String BROADCAST_ACTION = "com.alphadog.grapevine.services.ReviewSyncService";
	private static String url, token;
	private GrapevineDatabase database;
	private ReviewsTable reviewTable;
	

	@Override
	public IBinder onBind(Intent intent) {
		//We don't need anyone to interface with this service right now
		//so we'll return null for now.
		return null;
	}
	
	@Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("ReviewsSyncService", "Received start id " + startId + ": " + intent);
        return START_STICKY;
    }

	@Override
	public void onDestroy() {
		super.onDestroy();
		database.close();
	}

	public void doServiceTask() {		
		Log.i("ReviewSyncService", "Registering the listeners and trying to listen to latest location");
		
		url = getString(R.string.remote_service_url);
		database = new GrapevineDatabase(this, null);
		reviewTable = new ReviewsTable(database);
		token = getString(R.string.request_token);
		
		new LocationUpdateTrigger(this, 60000, new LocationResultExecutor() {
			@Override
			public void executeWithUpdatedLocation(Location location) {
				try {
					fetchRemoteSyncData(location);
				} catch(Exception e) {
					Log.e("ReviewSyncService", "Some error occured while fetching the location. Error is :" + e.getMessage());
				}	finally {
					//No matter what happens in the task, we eventually want to shut down the running service
					//as it won't be a good idea to keep it running forever in case of an exception. It'll
					//drain out the battery of cellphone. 
					Log.d("ReviewsSyncService", "Shutting down ReviewsSyncService.");
					stopSelf();
				}
			}
		}).fetchLatestLocation();
	}

	private void fetchRemoteSyncData(Location lastKnownLocation) {
		if(lastKnownLocation != null) {
			Log.d("ReviewSyncService", "Got a Location to fetch reviews. Latitude: " + lastKnownLocation.getLatitude() + " Longitude: " + lastKnownLocation.getLongitude());
			SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
			String urlForRangeQuery = getUrlForRangeQuery(url, lastKnownLocation, Integer.toString(sharedPreferences.getInt("radius_setting", 3)));
			Log.d("ReviewSyncService", "Getting reviews in range from: " + urlForRangeQuery);
			try 
			{
				HttpGet request = new HttpGet(urlForRangeQuery);
		        request.setHeader("Accept", "application/json");
		 
		        DefaultHttpClient httpClient = new DefaultHttpClient();
		        HttpResponse response = httpClient.execute(request);
		 
		        HttpEntity responseEntity = response.getEntity();
		         
		        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(responseEntity.getContent()));
		        StringBuilder finalJsonString = new StringBuilder();
		        String eachLine;
		        while ((eachLine = bufferedReader.readLine()) != null) {
		            finalJsonString.append(eachLine);
		        }
	
		        JSONObject jsonString = new JSONObject(finalJsonString.toString());
		        Log.d("ReviewsSyncService", "Fetched JSON String from service :"+ jsonString);
		        
		        storeLatestReviewsSet(getReviewList(jsonString));
		        generateBroadcasts();
		    } catch (Exception e) {
		        Log.e("ReviewsSyncService", "Could not fetch data from remote service. Error is: " + e.getMessage());
		    }
		}
	}

	private String getUrlForRangeQuery(String url, Location location, String range) {
		Map<String, String> queryParams = new HashMap<String, String>();
		queryParams.put("token", token);
		queryParams.put("latitude", Double.toString(location.getLatitude()));
		queryParams.put("longitude", Double.toString(location.getLongitude()));
		queryParams.put("range", range);
		
		String urlForRangeQuery = url.concat("?");
		for(Map.Entry<String, String> entry: queryParams.entrySet()) {
			urlForRangeQuery += entry.getKey() + "=" + entry.getValue() + "&";
		}
		
		return urlForRangeQuery;
	}
	
	private void generateBroadcasts() {
		sendBroadcast(new Intent(BROADCAST_ACTION));
	}

	private List<Review> getReviewList(JSONObject jsonString) {
		List<Review> listToReturn = new ArrayList<Review>();
		
		if(jsonString != null) {
			try {
				JSONArray array = jsonString.getJSONArray("reviews");
				for(int i=0; i < array.length(); i++) {
					listToReturn.add(Review.fromJsonObject(array.getJSONObject(i)));
				}
			} catch (JSONException e) {
				Log.e("ReviewsSyncService", "Error fetching a json list of reviews from service response" + e.getMessage());
			}
		}
		
		return listToReturn;
	}
	
	private void storeLatestReviewsSet(List<Review> reviewList) {
		reviewTable.replaceAllWith(reviewList);
	}
}
