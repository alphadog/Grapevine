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
		
		updatedView.setTextViewText(R.id.widget_text, nextReview.getHeading());
		//Clicking on the widget should open the app with the review on MAP
		Intent i = new Intent(this, GrapevineUpdateProvider.class);
		i.setAction(APP_VIEW);
		PendingIntent pi = PendingIntent.getBroadcast(context, 0,i, 0);
		updatedView.setOnClickPendingIntent(R.id.button_left, pi);
		
		return updatedView;
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