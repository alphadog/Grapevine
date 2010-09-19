package com.alphadog.grapevine;

import java.util.List;

import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;

import com.alphadog.grapevine.R;
import com.alphadog.grapevine.activities.ReviewCustomAdapter;
import com.alphadog.grapevine.activities.TaskWithProgressIndicator;
import com.alphadog.grapevine.db.GrapevineDatabase;
import com.alphadog.grapevine.db.ReviewsTable;
import com.alphadog.grapevine.models.Review;
import com.alphadog.grapevine.services.ReviewsSyncService;

public class ReviewListing extends ListActivity {
	
	private static final String LOG_TAG = "ReviewListing";
	private GrapevineDatabase database;
	private ReviewsTable reviewTable;
	private ReviewCustomAdapter messageListAdapter = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		database = new GrapevineDatabase(this);
		reviewTable = new ReviewsTable(database);
		setContentView(R.layout.review_list);
		refreshViews();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		if(database != null)
			database.close();
	}
	
	@Override
	public void onResume() {
		Log.i(LOG_TAG, "Registering broadcast recievers from listing view");
		super.onResume();
		registerReceiver(viewRefreshReceiver, new IntentFilter(ReviewsSyncService.BROADCAST_ACTION));
	}

	@Override
	public void onPause() {
		Log.i(LOG_TAG, "Unregistering broadcast recievers from listing view");
		super.onPause();
		unregisterReceiver(viewRefreshReceiver);
	}

	private BroadcastReceiver viewRefreshReceiver=new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			Log.i(LOG_TAG, "Broadcast received ::" + intent.getAction());
			refreshViews();
		}
	};

	private void refreshViews() {
		(new TaskWithProgressIndicator(this, getString(R.string.loading_reviews)) {
			
			@Override
			public void executeTask() {
				messageListAdapter = new ReviewCustomAdapter(ReviewListing.this, R.layout.review, fetchReviewList()); 
				setListAdapter(messageListAdapter);
			}
		}).executeTaskWithProgressIndicator();
	}
	
	private List<Review> fetchReviewList() {
		return reviewTable.findAll();
	}
}
