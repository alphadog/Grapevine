package com.alphadog.tribe.services;

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

import com.alphadog.tribe.R;
import com.alphadog.tribe.db.ReviewsTable;
import com.alphadog.tribe.db.TribeDatabase;
import com.alphadog.tribe.helpers.TwitterHelper;
import com.alphadog.tribe.models.Review;
import com.alphadog.tribe.widgets.TribeUpdateProvider;

public class WidgetUpdateService extends IntentService {
	
	public static final String APP_VIEW = "com.alphadog.tribe.widgets.TribeUpdateProvider.AppView";
	public static final String NEW_REVIEW = "com.alphadog.tribe.widgets.TribeUpdateProvider.NewReview";
	private static final String SEQUENCE_KEY = "sequence_cache";
	private SharedPreferences preferences = null;
	private TribeDatabase database;

	public WidgetUpdateService() {
		//Not defining a Default constructor in an IntentService results in an 
		//Instantiation error. This is critical and unfortunately not documented.
		super("WidgetUpdateService");
	}

	@Override
	public void onCreate() {
		super.onCreate();
		preferences = PreferenceManager.getDefaultSharedPreferences(this);
		database = new TribeDatabase(this, null);
	}
	
	@Override
	public void onDestroy() {
		//don't forget to close the database
		database.close();
	}
	
	@Override
	protected void onHandleIntent(Intent intent) {
		Review nextReview = fetchNextReview();
		ComponentName component = new ComponentName(this, TribeUpdateProvider.class);
		AppWidgetManager widgetManager = AppWidgetManager.getInstance(this);
		widgetManager.updateAppWidget(component, getUpdatedRemoteView(this, nextReview));
	}

	private RemoteViews getUpdatedRemoteView(Context context, Review nextReview) {
		RemoteViews updatedView = new RemoteViews(context.getPackageName(),R.layout.tribe_update_widget);
		
		updatedView.setTextViewText(R.id.widget_text, getWidgetDisplayTextFor(nextReview));
		
		int imageId = nextReview == null || nextReview.isLike() ? R.drawable.star : R.drawable.no_star;
		updatedView.setImageViewResource(R.id.button_left, imageId);
		
		Intent i = new Intent(this, TribeUpdateProvider.class);
		if(nextReview != null) {
			i.setAction(APP_VIEW);			
			Log.i(this.getClass().getName(), "Putting the ID in intent :"+ nextReview.getId());
			i.putExtra("REVIEW_ID", nextReview.getId());
		} else {
			if(new TwitterHelper(this).isTwitterCredentialsSetup()) {
				i.setAction(NEW_REVIEW);
				Log.i(this.getClass().getName(), "Action is new review to initiate new review action.");
			}
		}

		//Clicking on the widget should open the app with the review on MAP
		PendingIntent pi = PendingIntent.getBroadcast(context, 0,i, PendingIntent.FLAG_CANCEL_CURRENT);
		updatedView.setOnClickPendingIntent(R.id.widget_review_info_box, pi);
		updatedView.setOnClickPendingIntent(R.id.button_left, pi);
		
		return updatedView;
	}

	private CharSequence getWidgetDisplayTextFor(Review nextReview) {
		String textToDisplay = "No existing reviews. But you can add one!";
		if(nextReview != null && nextReview.getHeading() != null && nextReview.getHeading().length() > 0) {
			if(nextReview.getHeading().length() > 28)
			{
				textToDisplay = nextReview.getHeading().substring(0, 27) + "...";
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