package com.alphadog.grapevine.activities;

import android.app.Activity;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import com.alphadog.grapevine.R;
import com.alphadog.grapevine.services.LocationUpdateTrigger;
import com.alphadog.grapevine.services.LocationUpdateTrigger.LocationResultExecutor;

public class NewReviewActivity extends Activity {
	
	@Override 
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		updateLocationInBackground();
		setContentView(R.layout.new_review);
	}
	

	@Override
	public void onDestroy() {
		super.onDestroy();
	}
	
	private void updateLocationInBackground() {
		new LocationUpdateTrigger(NewReviewActivity.this, 60000L, new LocationResultExecutor() {
			@Override
			public void executeWithUpdatedLocation(Location location) {
				//Currently we don't want to do anything here. But lets see if we
				//can use this block later to cache the latest location when
				//review is completed and uploaded to website
				Log.i("NewReviewActivity", "Updating location as new review activity was initiated. New Location is " + location);
			}
		}).fetchLatestLocation();
	}

}
