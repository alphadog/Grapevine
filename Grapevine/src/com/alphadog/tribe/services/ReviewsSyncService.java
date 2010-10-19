package com.alphadog.tribe.services;

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

import com.alphadog.tribe.R;
import com.alphadog.tribe.activities.NewReviewActivity;
import com.alphadog.tribe.db.PendingReviewsTable;
import com.alphadog.tribe.db.TribeDatabase;
import com.alphadog.tribe.db.ReviewsTable;
import com.alphadog.tribe.models.Review;
import com.alphadog.tribe.services.LocationUpdateTrigger.LocationResultExecutor;
import com.alphadog.tribe.views.NotificationCreator;

public class ReviewsSyncService extends WakeEventService {

    public static String BROADCAST_ACTION = "com.alphadog.tribe.services.ReviewSyncService";
    private TribeDatabase database;
    private ReviewsTable reviewTable;
    private SharedPreferences sharedPreferences;

    @Override
    public IBinder onBind(Intent intent) {
        // We don't need anyone to interface with this service right now
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
        if (database != null) database.close();
    }
    
    private void initDBVars() {
        if (null == database) database = new TribeDatabase(this, null);
        if (null == reviewTable && null != database) reviewTable = new ReviewsTable(database);
    }

    public void doServiceTask() {
        Log.i("ReviewSyncService", "Registering the listeners and trying to listen to latest location");

        initDBVars();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        NotificationCreator.create(this, R.drawable.one, LocationUpdateTrigger.LOCATION_NOTIFICATION,
                "Tribe Sync", "Sync step 1", "Getting current cordinates", NewReviewActivity.getCurrentTime(), false);

        new LocationUpdateTrigger(this, 60000, new LocationResultExecutor() {
            @Override
            public void executeWithUpdatedLocation(Location location) {
                try {
                    // If there was no location fetched then do nothing and shut down the service.
                    if (location != null) {
                        NotificationCreator.create(ReviewsSyncService.this, R.drawable.two,
                                LocationUpdateTrigger.LOCATION_NOTIFICATION, "Tribe Sync", "Sync step 2",
                                "Location available. Initiating sync from server.", NewReviewActivity.getCurrentTime(),
                                false);
                        fetchRemoteSyncData(location);
                    }
                    else {
                        NotificationCreator.create(ReviewsSyncService.this, R.drawable.alert,
                                LocationUpdateTrigger.LOCATION_NOTIFICATION, "Tribe Sync", "Error",
                                "Tribe could not fetch data from server.", NewReviewActivity.getCurrentTime(), false);
                    }
                }
                catch (Exception e) {
                    Log.e("ReviewSyncService",
                            "Some error occured while fetching the location. Error is :" + e.getMessage());
                }
                finally {
                    // No matter what happens in the task, we eventually want to shut down the running
                    // service as it won't be a good idea to keep it running forever in
                    // case of an exception. It'll drain out the phone battery.
                    Log.d("ReviewsSyncService", "Shutting down ReviewsSyncService.");
                    stopSelf();
                }
            }
        }).fetchLatestLocation();
    }

    private void fetchRemoteSyncData(Location lastKnownLocation) {
        if (null == lastKnownLocation) return;
        Log.d("ReviewSyncService", "Got a Location to fetch reviews. Latitude: " + lastKnownLocation.getLatitude()
                + " Longitude: " + lastKnownLocation.getLongitude());

        try {
            String urlForRangeQuery = getUrlForRangeQuery(lastKnownLocation,
                    Integer.toString(sharedPreferences.getInt("radius_setting", 3)));
            Log.d("ReviewSyncService", "Getting reviews in range from: " + urlForRangeQuery);
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
            Log.d("ReviewsSyncService", "Fetched JSON String from service :" + jsonString);

            storeLatestReviewsSet(getReviewList(jsonString));
            NotificationCreator.create(this, R.drawable.three,
                    LocationUpdateTrigger.LOCATION_NOTIFICATION, "Tribe Sync", "Sync step 3", "Sync complete!",
                    NewReviewActivity.getCurrentTime(), false);
            generateBroadcasts();
        }
        catch (Exception e) {
            Log.e("ReviewsSyncService", "Could not fetch data from remote service. Error is: " + e.getMessage());
        }
    }

    private String getUrlForRangeQuery(Location location, String range) {
        String url = getString(R.string.remote_service_url);
        String token = getString(R.string.request_token);
        Map<String, String> queryParams = new HashMap<String, String>();
        queryParams.put("token", token);
        queryParams.put("latitude", Double.toString(location.getLatitude()));
        queryParams.put("longitude", Double.toString(location.getLongitude()));
        queryParams.put("range", range);

        String tribeName = sharedPreferences.getString("tribe_name", null);
        if (tribeName != null) {
            queryParams.put("tribe", tribeName);
        }

        String urlForRangeQuery = url.concat("?");
        for (Map.Entry<String, String> entry : queryParams.entrySet()) {
            urlForRangeQuery += entry.getKey() + "=" + entry.getValue() + "&";
        }

        return urlForRangeQuery;
    }

    private void generateBroadcasts() {
        sendBroadcast(new Intent(BROADCAST_ACTION));
    }

    private List<Review> getReviewList(JSONObject jsonString) {
        List<Review> listToReturn = new ArrayList<Review>();

        if (jsonString != null) {
            try {
                JSONArray array = jsonString.getJSONArray("reviews");
                for (int i = 0; i < array.length(); i++) {
                    listToReturn.add(Review.fromJsonObject(array.getJSONObject(i)));
                }
            }
            catch (JSONException e) {
                Log.e("ReviewsSyncService",
                        "Error fetching a json list of reviews from service response" + e.getMessage());
            }
        }

        return listToReturn;
    }

    private void storeLatestReviewsSet(List<Review> reviewList) {
        reviewTable.replaceAllWith(reviewList);
    }
}