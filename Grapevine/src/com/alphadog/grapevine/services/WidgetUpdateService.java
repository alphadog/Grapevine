package com.alphadog.grapevine.services;

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
import com.alphadog.grapevine.widgets.GrapevineUpdateProvider;

public class WidgetUpdateService extends IntentService {

	public static final String MAP_VIEW = "com.alphadog.grapevine.widgets.GrapevineUpdateProvider.MapView";
	private SharedPreferences preferences = null;

	public WidgetUpdateService() {
		//Just a unique string to create a worker with a name
		super("WidgetUpdateService");
	}

	@Override
	public void onCreate() {
		super.onCreate();
		//TODO need to use preferences to see some user defined parameters
		//for service calls. This will be done.
		preferences = PreferenceManager.getDefaultSharedPreferences(this);
	}
	
	@Override
	protected void onHandleIntent(Intent intent) {
		ComponentName component = new ComponentName(this, GrapevineUpdateProvider.class);
		AppWidgetManager widgetManager = AppWidgetManager.getInstance(this);
		widgetManager.updateAppWidget(component, getUpdatedRemoteView(this));
	}

	private RemoteViews getUpdatedRemoteView(Context context) {
		RemoteViews updatedView = new RemoteViews(context.getPackageName(),R.layout.grapevine_update_widget);
		
		//Clicking on the widget should open the app with the review on MAP
		Intent i = new Intent(this, GrapevineUpdateProvider.class);
		i.setAction(MAP_VIEW);
		PendingIntent pi = PendingIntent.getBroadcast(context, 0,i, 0);
		updatedView.setOnClickPendingIntent(R.id.button_left, pi);
		
		return updatedView;
	}
}