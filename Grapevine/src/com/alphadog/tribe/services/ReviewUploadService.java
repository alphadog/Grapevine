package com.alphadog.tribe.services;

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

import com.alphadog.tribe.R;
import com.alphadog.tribe.TribeApplication;
import com.alphadog.tribe.db.PendingReviewsTable;
import com.alphadog.tribe.exceptions.TwitterCredentialsBlankException;
import com.alphadog.tribe.models.PendingReview;

public class ReviewUploadService extends WakeEventService {

    private PendingReviewsTable pendingReviewsTable;
    private TweetAndPicUploader tweetAndPicUploader;

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
    protected void doServiceTask() {
        pendingReviewsTable = new PendingReviewsTable(((TribeApplication) getApplication()).getDB());

        Log.i(this.getClass().getName(), "Loading all the pending reviews to be uploaded");
        List<PendingReview> pendingReviews = pendingReviewsTable.findEligiblePendingReviews();
        if (pendingReviews == null || pendingReviews.size() > 0) {
            Log.i(this.getClass().getName(), "No pending reviews to upload.");
            return;
        }

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (preferences.getBoolean("tweet_always", false)) {
            try {
                tweetAndPicUploader = new TweetAndPicUploader(this);
            }
            catch (TwitterCredentialsBlankException tcbe) {
                Log.e(this.getClass().getName(),
                        "Will not upload images to twit4pic because twitter credentials are blank.", tcbe);
                tweetAndPicUploader = null;
            }
        }

        List<NameValuePair> payload = new ArrayList<NameValuePair>(10);
        for (PendingReview pendingReview : pendingReviews) {
            HttpClient httpClient = new DefaultHttpClient();
            HttpPost postRequest = new HttpPost(getString(R.string.remote_service_url));

            payload.clear();
            Log.i(this.getClass().getName(), "handling intent for uploading review id: " + pendingReview.getId());
            try {
                String imageUrl = uploadImageFor(pendingReview);
                payload.add(new BasicNameValuePair("image_url", imageUrl));
                payload.add(new BasicNameValuePair("text", pendingReview.getHeading()));
                payload.add(new BasicNameValuePair("like", pendingReview.isLike() ? "true" : "false"));
                payload.add(new BasicNameValuePair("latitude", pendingReview.getLatitude()));
                payload.add(new BasicNameValuePair("longitude", pendingReview.getLongitude()));
                payload.add(new BasicNameValuePair("username", preferences.getString("twitter_username", "")));
                payload.add(new BasicNameValuePair("created_at", pendingReview.getReviewDate()));
                payload.add(new BasicNameValuePair("location_name", "Unknown")); // For now.
                payload.add(new BasicNameValuePair("token", getString(R.string.request_token)));
                postRequest.setEntity(new UrlEncodedFormEntity(payload));

                Log.i(this.getClass().getName(), "posting data: " + payload.toString());
                HttpResponse response = httpClient.execute(postRequest);

                // successfully uploaded to mark pending review as complete
                if (response.getStatusLine().getStatusCode() == 201) {
                    runPostUploadTaskForReview(pendingReview);
                }
            }
            catch (Exception e) {
                Log.e(this.getClass().getName(),
                        "Exception occured while uploading review with id " + pendingReview.getId() + ". Error is ", e);
            }
        }
        Log.i(this.getClass().getName(), "Stopping service as our job here is done!");
        // Stop this service as the intended task of the service is done!!
        stopSelf();
    }

    private String uploadImageFor(PendingReview pendingReview) {
        if (null == tweetAndPicUploader) { return null; }
        return tweetAndPicUploader.uploadImageFor(pendingReview.getImagePath(), pendingReview.getTwitterMessage());
    }

    private void runPostUploadTaskForReview(PendingReview uploadedReview) {
        deleteImageFileFor(uploadedReview);
        markUploadedReviewAsComplete(uploadedReview);
    }

    private void markUploadedReviewAsComplete(PendingReview uploadedReview) {
        Map<String, String> valuesToUpdate = new HashMap<String, String>();
        valuesToUpdate.put(PendingReviewsTable.PendingReviewCursor.getStatusFieldName(),
                PendingReviewsTable.COMPLETE_STATUS);
        pendingReviewsTable.updateFieldsForId(uploadedReview.getId(), valuesToUpdate);
    }

    private void deleteImageFileFor(PendingReview uploadedReview) {
        Log.i(this.getClass().getName(), "Deleting image for pendingReview");
        File uploadedPhoto = new File(uploadedReview.getImagePath());
        // delete the uploaded photo from local disk
        if (uploadedPhoto != null && uploadedPhoto.exists()) {
            uploadedPhoto.delete();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
