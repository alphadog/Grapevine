package com.alphadog.grapevine.services;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import com.alphadog.grapevine.R;
import com.alphadog.grapevine.db.GrapevineDatabase;
import com.alphadog.grapevine.db.PendingReviewsTable;
import com.alphadog.grapevine.exceptions.TwitterCredentialsBlankException;
import com.alphadog.grapevine.models.PendingReview;

public class ReviewUploadService extends WakeEventService {

	private GrapevineDatabase database;
	private PendingReviewsTable pendingReviewsTable;
	private List<PendingReview> pendingReviews;
	private TweetWithImageUpload imageUploader;
	private SharedPreferences preferences;
	
	@Override
	public void onCreate() {
		super.onCreate();
	}
	
	@Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("ReviewsSyncService", "Received start id " + startId + ": " + intent);
        return START_STICKY;
    }
	
	@Override
	public void doServiceTask() {
		try {
			database = new GrapevineDatabase(this);
			pendingReviewsTable = new PendingReviewsTable(database);
			
			preferences = PreferenceManager.getDefaultSharedPreferences(this);
			Log.i(this.getClass().getName(), "Loading all the pending reviews to be uploaded");
			loadAllPendingReviews();
			if(pendingReviews != null && pendingReviews.size() > 0) {
				
				String imageUrl = null;
				
				List<NameValuePair> payload = new ArrayList<NameValuePair>(10);
				for(PendingReview eachPendingReview : pendingReviews) {
					HttpClient httpClient = new DefaultHttpClient();
					HttpPost postRequest = new HttpPost(getString(R.string.remote_service_url));
					imageUploader = new TweetWithImageUpload(this);
					payload.clear();
					Log.i(this.getClass().getName(), "handling intent for uploading review id: " + eachPendingReview.getId());
					
					try {
						try {
							imageUrl = uploadImageFor(eachPendingReview);
						} catch(TwitterCredentialsBlankException tcbe) {
							imageUrl = "";
							Log.e(this.getClass().getName(), "Could not upload image to twit4pic because twitter credentials were blank.", tcbe);
						}
						payload.add(new BasicNameValuePair("image_url", imageUrl));
						payload.add(new BasicNameValuePair("text", eachPendingReview.getHeading()));
						payload.add(new BasicNameValuePair("like", eachPendingReview.isLike() ? "true" : "false"));
						payload.add(new BasicNameValuePair("latitude", eachPendingReview.getLatitude()));
						payload.add(new BasicNameValuePair("longitude", eachPendingReview.getLongitude()));
						payload.add(new BasicNameValuePair("username", preferences.getString("twitter_username", null)));
						payload.add(new BasicNameValuePair("created_at", eachPendingReview.getReviewDate()));
						payload.add(new BasicNameValuePair("location_name", eachPendingReview.getLocationName()));
						payload.add(new BasicNameValuePair("token", getString(R.string.request_token)));
						postRequest.setEntity(new UrlEncodedFormEntity(payload));
						
						Log.i(this.getClass().getName(), "posting data: " + payload.toString());
						HttpResponse response = httpClient.execute(postRequest);					
						
						//successfully uploaded to mark pending review as complete
						if(response.getStatusLine().getStatusCode() == 201) {
							runPostUploadTaskForReview(eachPendingReview);
						}
					} catch(Exception e) {
						Log.e(this.getClass().getName(), "Exception occured while uploading review with id "+eachPendingReview.getId()+". Error is ", e);
					}
					
				}
			}
			
			//At the end we need to ensure that all the uploaded and stale reviews are cleaned from database
			cleanupPendingReviews();
		}
		finally {
			Log.i(this.getClass().getName(), "Stopping service as our job here is done!");
			//Stop this service as the intended task of the service is done!!
			stopSelf();
		}
	}

	private void cleanupPendingReviews() {
		pendingReviewsTable.cleanupStaleAndCompletedReviews();
	}

	private String uploadImageFor(PendingReview pendingReview) throws TwitterCredentialsBlankException {
		return imageUploader.uploadImageFor(pendingReview.getImagePath(), pendingReview.getTwitterMessage());		
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (database != null)
			database.close();
	}
	
	private void loadAllPendingReviews() {
		pendingReviews = pendingReviewsTable.findEligiblePendingReviews();
	}
	
	private void runPostUploadTaskForReview(PendingReview uploadedReview) {
		deleteImageFileFor(uploadedReview);
		markUploadedReviewAsComplete(uploadedReview);
	}

	private void markUploadedReviewAsComplete(PendingReview uploadedReview) {
		Map<String, String> valuesToUpdate = new HashMap<String, String>();
		valuesToUpdate.put(PendingReviewsTable.PendingReviewCursor.getStatusFieldName(), PendingReviewsTable.COMPLETE_STATUS);
		pendingReviewsTable.updateFieldsForId(uploadedReview.getId(), valuesToUpdate);
	}

	private void deleteImageFileFor(PendingReview uploadedReview) {
		Log.i(this.getClass().getName(), "Deleting image for pendingReview");
		File uploadedPhoto = new File(uploadedReview.getImagePath());
		//delete the uploaded photo from local disk
		if(uploadedPhoto != null  && uploadedPhoto.exists()) {
			uploadedPhoto.delete();
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
}
