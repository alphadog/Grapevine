package com.alphadog.tribe.activities;

import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.InputFilter;
import android.text.format.Time;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;

import com.alphadog.tribe.R;
import com.alphadog.tribe.db.TribeDatabase;
import com.alphadog.tribe.db.PendingReviewsTable;
import com.alphadog.tribe.models.PendingReview;
import com.alphadog.tribe.services.LocationUpdateTrigger;
import com.alphadog.tribe.services.ReviewUploadService;
import com.alphadog.tribe.services.LocationUpdateTrigger.LocationResultExecutor;

public class NewReviewActivity extends Activity {
	
	protected static final String PENDING_REVIEW_ID = "PENDING_REVIEW_ID";
	private TribeDatabase database;
	private PendingReviewsTable pendingReviewTable;
	private long reviewId;
	private String imagePath;
	private volatile int progressStep;
	
	private static String localtimezone = new Time().timezone;
	
	@Override 
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//reset progressStep to 0 when we start the activity
		progressStep = 0;
		
		//initialize pending review table wrapper
		database = new TribeDatabase(this);
		pendingReviewTable = new PendingReviewsTable(database);
				
		reviewId = getIntent().getLongExtra(PENDING_REVIEW_ID,-1);
		imagePath = getIntent().getStringExtra(CameraActivity.IMAGE_PATH);
		reviewId = reviewId == -1 ? getCurrentTime() : reviewId;
		
		//create blank pending review in database
		createBlankPendingReview();
		
		//call for a location update
		updateLocationInBackground();
		
		//update the UI
		setContentView(R.layout.new_review);

		//bind the components on UI
		bindViewComponents();
		
		//And we are done!!
	}

	private void bindViewComponents() {
		
		EditText reviewText = (EditText) findViewById(R.id.review_text);
		
		// include tribe tag by default
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		String tribeName = sharedPreferences.getString("tribe_name", "");
		if(tribeName != null && tribeName.length() > 1) {
			reviewText.setText("#" + tribeName+" ");
		}
		
		//A review of max 200 chars.
		int maxLength = 200;
		InputFilter[] filters = new InputFilter[1];
		filters[0] = new InputFilter.LengthFilter(maxLength);
		reviewText.setFilters(filters);
		
		Button submitButton = (Button) findViewById(R.id.review_submit);
		submitButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.i("NewReviewActivity", "Review submitted. Updating details and picture.");
				//save the review
				updateReviewDetails();

				//Done with activity
				NewReviewActivity.this.finish();
			}
		});
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if(database != null)
			database.close();
		
	}
		
	private void updateLocationInBackground() {
		//we'll wait for 2 minutes
		new LocationUpdateTrigger(NewReviewActivity.this, 120000L, new LocationResultExecutor() {
			@Override
			public void executeWithUpdatedLocation(Location location) {
				try {
					if(location != null) 
					{
						//once we have the location, then lets update the pending review with the
						//user location. 
						Map<String, String> valuesMap = new HashMap<String, String>();
						valuesMap.put(PendingReviewsTable.PendingReviewCursor.getLatitudeFieldName(), Double.toString(location.getLatitude()));
						valuesMap.put(PendingReviewsTable.PendingReviewCursor.getLongitudeFieldName(), Double.toString(location.getLongitude()));

						//since this runs in a different thread and so does saving of a photo, we need to ensure
						//that the access to database is synchronized, else it'll throw database is locked error.
						synchronized(pendingReviewTable) {
							pendingReviewTable.updateFieldsForId(reviewId, valuesMap);
						}
						Log.i("NewReviewActivity", "Updating location as new review activity was initiated. New Location is " + location);
				
						fireUploadService();
					}
				} catch (Exception e) {
					Log.e("NewActivityLocationThread", "Error while updating location for new review", e);
				} finally {
					if(database != null)
						database.close();
				}
			}
		}).fetchLatestLocation();
	}
	
	private void fireUploadService() {
		++progressStep;
		Log.i(this.getClass().getName(), "Current value for progressStep is:"+ progressStep);
		if(progressStep == 2) {
			Log.i(this.getClass().getName(), "Firing upload service now. We have both progress steps completed");
			//Fire up service to upload the review to net
			ReviewUploadService.acquireLock(NewReviewActivity.this);
			startService(new Intent(NewReviewActivity.this, ReviewUploadService.class));
		}
	}

	private void createBlankPendingReview() {
		//Create a blank copy in database that all the other threads can update with information
		Time currentTime = new Time("UTC");
		currentTime.setToNow();
		currentTime.switchTimezone(localtimezone);
		synchronized(pendingReviewTable) {
			pendingReviewTable.create(new PendingReview(reviewId, "", "", "", imagePath, "","", 1, currentTime.format3339(false), null, null,"", PendingReviewsTable.INITIAL_STATUS, 0));
		}
	}
	
	private void updateReviewDetails() {
		RadioButton goodReview = (RadioButton) findViewById(R.id.good_review);
		int like = goodReview.isChecked() ? 1 : 0;
		String reviewText = ((EditText) findViewById(R.id.review_text)).getText().toString();

		Map<String, String> valuesToUpdate = new HashMap<String, String>();
		valuesToUpdate.put(PendingReviewsTable.PendingReviewCursor.getReviewHeadingFieldName(), reviewText);
		valuesToUpdate.put(PendingReviewsTable.PendingReviewCursor.getLikeFieldName(), Integer.toString(like));
		valuesToUpdate.put(PendingReviewsTable.PendingReviewCursor.getStatusFieldName(), PendingReviewsTable.PENDING_STATUS);
		valuesToUpdate.put(PendingReviewsTable.PendingReviewCursor.getRetriesFieldName(), Long.toString(0L));
		synchronized(pendingReviewTable) {
			pendingReviewTable.updateFieldsForId(reviewId, valuesToUpdate);
		}
		
		//Invoke service in new thread
		new Thread(new Runnable() {
		    public void run() {
		      fireUploadService();
		    }
		}).start();
	}

	public static long getCurrentTime() {
		//generate the new pending review's id
		Time reviewTime = new Time();
		reviewTime.setToNow();
		//This is the id name the image and is passed across to the new
		//review activity so that it can be used across all places
		//to refer the same review
		return reviewTime.toMillis(true);
	}
}
