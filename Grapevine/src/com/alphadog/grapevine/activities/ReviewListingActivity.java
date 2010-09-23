package com.alphadog.grapevine.activities;

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
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.alphadog.grapevine.R;
import com.alphadog.grapevine.db.GrapevineDatabase;
import com.alphadog.grapevine.db.ReviewsTable;
import com.alphadog.grapevine.models.Review;
import com.alphadog.grapevine.services.ReviewsSyncService;
import com.alphadog.grapevine.views.GrapevinePrefrences;
import com.alphadog.grapevine.views.ReviewCustomAdapter;
import com.alphadog.grapevine.views.TaskWithProgressIndicator;

public class ReviewListingActivity extends ListActivity {
	
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
		
		Log.i(LOG_TAG, "Registering broadcast recievers from listing view");
		registerReceiver(viewRefreshReceiver, new IntentFilter(ReviewsSyncService.BROADCAST_ACTION));
		refreshViews();
		bindTitleBarButtons();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		try {
			Log.i(LOG_TAG, "Unregistering broadcast recievers from listing view");
			unregisterReceiver(viewRefreshReceiver);
		} catch(Exception e) {
			Log.e("ReviewListing", "Error occured while unregistering recievers. Error is :" + e.getMessage());
		}finally { 
			if(database != null)
				database.close();
		}
	}
	
	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, SETTINGS, 0, getString(R.string.settings)).setIcon(R.drawable.settings);
	    menu.add(0, ABOUT, 0, getString(R.string.about)).setIcon(R.drawable.about);
	    menu.add(0, CANCEL, 0, getString(R.string.cancel)).setIcon(R.drawable.cancel);
	    return true;
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		Log.i(LOG_TAG, "List item clicked at position :: " + position);
		Intent intent = new Intent(this, ReviewDetailsActivity.class);
		intent.putExtra("POS", position);
		startActivity(intent);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	    case SETTINGS:
	    	Intent newIntent = new Intent(this, GrapevinePrefrences.class);
	    	startActivity(newIntent);
	        return true;
	    case ABOUT:
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
				messageListAdapter = new ReviewCustomAdapter(ReviewListingActivity.this, R.layout.review, fetchReviewList()); 
				setListAdapter(messageListAdapter);
			}
		}).executeTaskWithProgressIndicator();
	}
	
	private List<Review> fetchReviewList() {
		return reviewTable.findAll();
	}
	
	private void bindTitleBarButtons() {
		//Map View Button
		ImageButton mapViewButton = (ImageButton) findViewById(R.id.map_icon);
		mapViewButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent mapIntent = new Intent(ReviewListingActivity.this, com.alphadog.grapevine.activities.ReviewsMapActivity.class);
		    	startActivity(mapIntent);
			}
		});
		
		//On Demand Refresh Button
		ImageButton onDemandRefreshButton = (ImageButton) findViewById(R.id.refresh_icon);
		onDemandRefreshButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ReviewsSyncService.acquireStaticLock(ReviewListingActivity.this);
				Intent serviceIntent = new Intent(ReviewListingActivity.this, ReviewsSyncService.class);
				startService(serviceIntent);
			}
		});
		
		//New Review Button Binding
		ImageButton newReviewButton = (ImageButton) findViewById(R.id.new_review);
		newReviewButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Toast newToast = Toast.makeText(ReviewListingActivity.this, "I need to be implemented soon", Toast.LENGTH_SHORT);
				newToast.show();
			}
		});
	}

}
