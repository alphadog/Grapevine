package com.alphadog.grapevine.services;

import java.util.List;

import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.RemoteViews;

import com.alphadog.grapevine.R;
import com.alphadog.grapevine.db.GrapevineDatabase;
import com.alphadog.grapevine.db.ReviewsTable;
import com.alphadog.grapevine.models.Review;
import com.alphadog.grapevine.widgets.GrapevineUpdateProvider;

public class WidgetUpdateService extends IntentService {
	
	public static final String APP_VIEW = "com.alphadog.grapevine.widgets.GrapevineUpdateProvider.AppView";
	private static final String SEQUENCE_KEY = "sequence_cache";
	private SharedPreferences preferences = null;
	private GrapevineDatabase database;

	public WidgetUpdateService() {
		//Not defining a Default constructor in an IntentService results in an 
		//Instantiation error. This is critical and unfortunately not documented.
		super("WidgetUpdateService");
	}

	@Override
	public void onCreate() {
		super.onCreate();
		preferences = PreferenceManager.getDefaultSharedPreferences(this);
		database = new GrapevineDatabase(this, null);
	}
	
	@Override
	public void onDestroy() {
		//don't forget to close the database
		database.close();
	}
	
	@Override
	protected void onHandleIntent(Intent intent) {
		Review nextReview = fetchNextReview();
		if(null != nextReview) {
			ComponentName component = new ComponentName(this, GrapevineUpdateProvider.class);
			AppWidgetManager widgetManager = AppWidgetManager.getInstance(this);
			widgetManager.updateAppWidget(component, getUpdatedRemoteView(this, nextReview));
		}
	}

	private RemoteViews getUpdatedRemoteView(Context context, Review nextReview) {
		RemoteViews updatedView = new RemoteViews(context.getPackageName(),R.layout.grapevine_update_widget);
		
		updatedView.setTextViewText(R.id.widget_text, getWidgetDisplayTextFor(nextReview));
		//Clicking on the widget should open the app with the review on MAP
		Intent i = new Intent(this, GrapevineUpdateProvider.class);
		i.setAction(APP_VIEW);
		Log.i(this.getClass().getName(), "Putting the ID in intent :"+ nextReview.getId());
		i.putExtra("REVIEW_ID", nextReview.getId());
		PendingIntent pi = PendingIntent.getBroadcast(context, 0,i, PendingIntent.FLAG_CANCEL_CURRENT);
		updatedView.setOnClickPendingIntent(R.id.widget_review_info_box, pi);
		updatedView.setOnClickPendingIntent(R.id.button_left, pi);
		
		return updatedView;
	}

	private CharSequence getWidgetDisplayTextFor(Review nextReview) {
		String textToDisplay = "No Info available.";
		if(nextReview != null && nextReview.getHeading() != null && nextReview.getHeading().length() > 0) {
			if(nextReview.getHeading().length() > 31)
			{
				textToDisplay = nextReview.getHeading().substring(0, 30) + "...";
			} else {
				textToDisplay = nextReview.getHeading();
			}
		}
		return textToDisplay;
	}

	protected Review fetchNextReview() {
		List<Review> allReviews = new ReviewsTable(database).findAll();
		
		int lastFetchIndex = retrieveSequenceFromCache();
		if(allReviews != null && allReviews.size() > 0 ) {
			if(lastFetchIndex > allReviews.size() -1 ) {
				updateSequenceInCache(0);
				return allReviews.get(0);
			} else {
				updateSequenceInCache(lastFetchIndex + 1);
				return allReviews.get(lastFetchIndex++);
			}
		}	
		
		return null;
	}

	private void updateSequenceInCache(int sequenceId) {
		if(null != preferences) {
			preferences.edit().putInt(SEQUENCE_KEY, sequenceId).commit();
		}
	}
	
	private int retrieveSequenceFromCache() {
		if(null != preferences) {
			return preferences.getInt(SEQUENCE_KEY, 0);
		}
		return 0;
	}
}