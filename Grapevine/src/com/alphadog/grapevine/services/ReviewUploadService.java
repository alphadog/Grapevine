package com.alphadog.grapevine.services;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import com.alphadog.grapevine.R;
import com.alphadog.grapevine.db.GrapevineDatabase;
import com.alphadog.grapevine.db.PendingReviewsTable;
import com.alphadog.grapevine.models.PendingReview;
import com.alphadog.grapevine.models.Review;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

public class ReviewUploadService extends IntentService {

	private GrapevineDatabase database;
	
	public ReviewUploadService() {
		super("ReviewUploadService");
		database = new GrapevineDatabase(this);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		long reviewId = intent.getLongExtra("REVIEW_ID", 0);
		Log.i(this.getClass().getName(), "Handling intent for uploading review id: " + reviewId);
		
		PendingReviewsTable pendingReviewsTable = new PendingReviewsTable(database);
		Review review = pendingReviewsTable.findById(reviewId);

		HttpClient httpClient = new DefaultHttpClient();
		HttpPost postRequest = new HttpPost(getString(R.string.remote_service_url));
		
		try {
			List<NameValuePair> payload = new ArrayList<NameValuePair>(6);
			payload.add(new BasicNameValuePair("image_url", review.getImageUrl()));
			payload.add(new BasicNameValuePair("text", review.getHeading()));
			payload.add(new BasicNameValuePair("like", review.isLike() ? "true" : "false"));
			payload.add(new BasicNameValuePair("latitude", review.getLatitude()));
			payload.add(new BasicNameValuePair("longitude", review.getLongitude()));
			payload.add(new BasicNameValuePair("token", getString(R.string.request_token)));
			postRequest.setEntity(new UrlEncodedFormEntity(payload));

			Log.i(this.getClass().getName(), "posting data: " + payload.toString());
			HttpResponse response = httpClient.execute(postRequest);
			
			if(response.getStatusLine().getStatusCode() == 201) {
				// still to figure out how to 
				// delete from pending reviews table
			}
			
		} catch (Exception e) {
			Log.e(this.getClass().getName(), "Error posting review to server.");
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (database != null)
			database.close();
	}
}