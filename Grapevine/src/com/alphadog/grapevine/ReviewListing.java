package com.alphadog.grapevine;

import java.util.List;

import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.alphadog.grapevine.activities.ReviewsMapActivity;
import com.alphadog.grapevine.db.GrapevineDatabase;
import com.alphadog.grapevine.db.ReviewsTable;
import com.alphadog.grapevine.models.Review;
import com.alphadog.grapevine.services.ReviewsSyncService;
import com.alphadog.grapevine.views.GrapevinePrefrences;
import com.alphadog.grapevine.views.ReviewCustomAdapter;
import com.alphadog.grapevine.views.TaskWithProgressIndicator;

public class ReviewListing extends ListActivity {
	
	private static final String LOG_TAG = "ReviewListing";
	private GrapevineDatabase database;
	private ReviewsTable reviewTable;
	private ReviewCustomAdapter messageListAdapter = null;
	
	private final static int SETTINGS = 1;
	private final static int ABOUT = 2;
	private final static int CANCEL = 3;
	
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
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, SETTINGS, 0, getString(R.string.settings)).setIcon(R.drawable.settings);
	    menu.add(0, ABOUT, 0, getString(R.string.about)).setIcon(R.drawable.about);
	    menu.add(0, CANCEL, 0, getString(R.string.cancel)).setIcon(R.drawable.cancel);
	    return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	    case SETTINGS:
	    	Intent newIntent = new Intent(this, GrapevinePrefrences.class);
	    	startActivity(newIntent);
	        return true;
	    case ABOUT:
	    	Intent mapIntent = new Intent(this, com.alphadog.grapevine.activities.ReviewsMapActivity.class);
	    	startActivity(mapIntent);
	        return true;
	    case CANCEL:
	        this.finish();
	    }
	    return false;
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
