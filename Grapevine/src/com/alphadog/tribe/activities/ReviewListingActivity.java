package com.alphadog.tribe.activities;

import java.util.ArrayList;
import java.util.List;

import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.alphadog.tribe.R;
import com.alphadog.tribe.alarms.ReviewsSyncServiceAlarmReceiver;
import com.alphadog.tribe.db.ReviewsTable;
import com.alphadog.tribe.db.TribeDatabase;
import com.alphadog.tribe.helpers.TwitterHelper;
import com.alphadog.tribe.models.Review;
import com.alphadog.tribe.services.ReviewsSyncService;
import com.alphadog.tribe.views.ReviewCustomAdapter;
import com.alphadog.tribe.views.TaskWithProgressIndicator;
import com.alphadog.tribe.views.TribePreferences;

public class ReviewListingActivity extends ListActivity {
	
	private static final String LOG_TAG = "ReviewListing";
	private TribeDatabase database;
	private ReviewsTable reviewTable;
	private ReviewCustomAdapter messageListAdapter = null;
	private List<Review> reviewList = new ArrayList<Review>();
	private SharedPreferences sharedPreferences;
	
	private final static int SETTINGS = 1;
	private final static int ABOUT = 2;
	private final static int QUIT = 3;

	//Handler to refresh views
	private Handler viewUpdater = new Handler() {
		@Override
		public void handleMessage(Message message) {
			switch (message.what) {
			case 0:
				refreshViews();
				break;
			default:
				break;
			}
		}
	};
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		database = new TribeDatabase(this);
		reviewTable = new ReviewsTable(database);
		setContentView(R.layout.review_list);
		
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		Log.i(LOG_TAG, "Registering broadcast recievers from listing view");
		registerReceiver(viewRefreshReceiver, new IntentFilter(ReviewsSyncService.BROADCAST_ACTION));
		refreshViews();
		showTribeIfSet();
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
		showTribeIfSet();
	}

	@Override
	public void onPause() {
		super.onPause();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, SETTINGS, 0, getString(R.string.settings)).setIcon(R.drawable.settings);
	    menu.add(0, ABOUT, 0, getString(R.string.about)).setIcon(R.drawable.about);
	    menu.add(0, QUIT, 0, getString(R.string.quit)).setIcon(R.drawable.cancel);
	    return true;
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		if(reviewList != null && reviewList.size() >= position) {
			Log.i(LOG_TAG, "List item clicked at position :: " + position);
			Intent intent = new Intent(this, ReviewDetailsActivity.class);
			intent.putExtra("REVIEW_ID", reviewList.get(position).getId());
			startActivity(intent);
		}
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	    case SETTINGS:
	    	Intent newIntent = new Intent(this, TribePreferences.class);
	    	startActivity(newIntent);
	        return true;
	    case ABOUT:
	    	Intent aboutIntent = new Intent(this, About.class);
	    	startActivity(aboutIntent);
	        return true;
	    case QUIT:
	        this.finish();
	    }
	    return false;
	}
	
	private BroadcastReceiver viewRefreshReceiver=new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			Log.i(LOG_TAG, "Broadcast received ::" + intent.getAction());
			Thread broadcastReceieverAction = new Thread() {
				public void run() {
					Message msg = new Message();
					msg.what = 0;
					viewUpdater.sendMessage(msg);
				}
			};
			broadcastReceieverAction.start();
		}
	};

	private void refreshViews() {
		(new TaskWithProgressIndicator(this, getString(R.string.loading_reviews)) {
			@Override
			public void executeTask() {
				populateReviewList();
				messageListAdapter = new ReviewCustomAdapter(ReviewListingActivity.this, R.layout.review, reviewList); 
				setListAdapter(messageListAdapter);
			}
		}).executeTaskWithProgressIndicator();
	}

	private void showTribeIfSet() {
		String tribeName = sharedPreferences.getString("tribe_name", "");
		TextView tribeNameView = (TextView) findViewById(R.id.following_tribe);
		
		if (tribeName.equals("")) {
			tribeNameView.setVisibility(View.GONE);
		} else {
			tribeNameView.setVisibility(View.VISIBLE);
			tribeNameView.setText(Html.fromHtml(getResources().getString(R.string.following_tribe) + 
												" <font color=\"#FFCC66\">" + tribeName + "</font>"));
		}
	}
	
	private void populateReviewList() {
		reviewList = reviewTable.findAll();
	}
	
	private void bindTitleBarButtons() {
		//Map View Button
		ImageView mapViewImage = (ImageView) findViewById(R.id.map_icon);
		mapViewImage.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				//Invoke map activity in new thread
				new Thread(new Runnable() {
				    public void run() {
				    	Intent mapIntent = new Intent(ReviewListingActivity.this, com.alphadog.tribe.activities.ReviewsMapActivity.class);
				    	startActivity(mapIntent);
				    }
				}).start();		
			}
		});
		
		//On Demand Refresh Button
		ImageView onDemandRefreshImage = (ImageView) findViewById(R.id.refresh_icon);
		onDemandRefreshImage.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Toast notificationToast = Toast.makeText(ReviewListingActivity.this, getString(R.string.refresh_notification), Toast.LENGTH_LONG);
				notificationToast.show();
				
				//Invoke service in new thread
				new Thread(new Runnable() {
				    public void run() {
				    	new ReviewsSyncServiceAlarmReceiver().onReceive(ReviewListingActivity.this,null); 
				    }
				}).start();				
			}
		});
		
		//New Review Button Binding
		ImageView newReviewImage = (ImageView) findViewById(R.id.new_review);
		newReviewImage.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if(new TwitterHelper(ReviewListingActivity.this).isTwitterCredentialsSetup()) {
					Intent newReviewIntent = new Intent(ReviewListingActivity.this, com.alphadog.tribe.activities.CameraActivity.class);
					startActivity(newReviewIntent);
				} else {
					Toast setupTwitterCredentialsNotification = Toast.makeText(ReviewListingActivity.this, getString(R.string.setup_twitter_credentials), Toast.LENGTH_LONG);
					setupTwitterCredentialsNotification.show();
				}
			}
		});
	}
}
