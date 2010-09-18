package com.alphadog.grapevine.services;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.util.Log;

import com.alphadog.grapevine.R;
import com.alphadog.grapevine.db.GrapevineDatabase;
import com.alphadog.grapevine.db.ReviewsTable;
import com.alphadog.grapevine.models.Review;

public class ReviewsSyncService extends SchedulableService {

	private static String url;
	private GrapevineDatabase database;
	private ReviewsTable reviewTable;
	
	public ReviewsSyncService() {
		super("ReviewsSyncService");
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		url = getString(R.string.remote_service_url);
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
		try {
			// Send GET request to <service>/GetPlates
	        HttpGet request = new HttpGet(url);
	        request.setHeader("Accept", "application/json");
	 
	        DefaultHttpClient httpClient = new DefaultHttpClient();
	        HttpResponse response = httpClient.execute(request);
	 
	        HttpEntity responseEntity = response.getEntity();
	         
	        // Read response data into buffer
	        char[] buffer = new char[(int)responseEntity.getContentLength()];
	        InputStream stream = responseEntity.getContent();
	        InputStreamReader reader = new InputStreamReader(stream);
	        reader.read(buffer);
	        stream.close();
	        
	        JSONObject jsonString = new JSONObject(new String(buffer));
	        Log.d("ReviewsSyncService", "Fetched JSON String from service :"+ jsonString);
	        
	        storeLatestReviewsSet(getReviewList(jsonString));
	    } catch (Exception e) {
	        Log.e("ReviewsSyncService", "Could not fetch data from remote service. Error is: " + e.getMessage());
	    }
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
