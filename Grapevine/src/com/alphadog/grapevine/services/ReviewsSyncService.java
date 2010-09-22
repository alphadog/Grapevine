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
import android.preference.PreferenceManager;
import android.util.Log;

import com.alphadog.grapevine.R;
import com.alphadog.grapevine.db.GrapevineDatabase;
import com.alphadog.grapevine.db.ReviewsTable;
import com.alphadog.grapevine.models.Review;

public class ReviewsSyncService extends SchedulableService {

	public static String BROADCAST_ACTION = "com.alphadog.grapevine.services.ReviewSyncService";
	private static String url, token;
	private GrapevineDatabase database;
	private ReviewsTable reviewTable;
	
	public ReviewsSyncService() {
		super("ReviewsSyncService");
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		url = getString(R.string.remote_service_url);
		token = getString(R.string.request_token);
		database = new GrapevineDatabase(this, null);
		reviewTable = new ReviewsTable(database);
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		database.close();
	}

	@Override
	public void doServiceTask(Intent intent) {
		Log.i("ReviewSyncService", "In doServiceTask Method");
			
//		TimerTask newTask = new TimerTask() {			
//			LocationResultExecutor x = new LocationResultExecutor() {
//				@Override
//				public void executeWithUpdatedLocation(Location location) {
//					Intent t = new Intent(ReviewsSyncService.this, ReviewsMapActivity.class);
//					t.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//					startActivity(t);
//				}
//			};
//			
//			@Override
//			public void run() {
//				LocationUpdateTrigger trigger = new LocationUpdateTrigger(ReviewsSyncService.this, 30000, x);
//				trigger.fetchLatestLocation();
//			}
//		};
//		newTask.run();
//		
		//This service is called in a separate thread. So we'll fire up the location lookup
		//in this new thread and once we have it, then through callback method, we call the
		//remote fetch, so that I can get my reviews as per my current location.
//		(new LocationTracker(this) {
//			@Override
//			protected LocationListener getLocationListener() {
//				return new LocationListener() {
//					public void onLocationChanged(Location location) {
//						try {
//							Log.d("LocationTracker@ReviewSyncService","Got a fix on current location with location as :"+location);
//							if(location == null) {
//								Log.d("LocationTracker@ReviewSyncService","Since current location was returned as null, returning last known location");
//								location =  getLastKnownLocation();
//							}
//							//I read somewhere that GPS is a bit jumpy when it gets it's first fix on the location
//							//It is usually advisable to use second or third reading when it attains some more accuracy.
//							//Unfortunately for that design this code will become too hacky. We might need to refactor
//							//if we decide to go that way.
//						    updateWithNewLocation(location);
//						}
//					    finally {
//					    	//unhook the listener as we need only the current location and if we keep it hooked
//					    	//anymore, then we'd be just wasting battery
//					    	unhookLocationListener(this);
//					    }
//					}
//					
//					private void updateWithNewLocation(Location location) {
//						fetchRemoteSyncData(location);
//					}
//					
//					public void onProviderDisabled(String provider){}
//					public void onProviderEnabled(String provider) {}
//					public void onStatusChanged(String provider, int status, Bundle extras) {}
//				};
//			}
//		}).getCurrentLocation();
		fetchRemoteSyncData(new LocationTracker(this).getLastKnownLocation());
	}

	private void fetchRemoteSyncData(Location location) {
		if(location != null) {
			SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
			String urlForRangeQuery = getUrlForRangeQuery(url, location, Integer.toString(sharedPreferences.getInt("radius_setting", 3)));
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
