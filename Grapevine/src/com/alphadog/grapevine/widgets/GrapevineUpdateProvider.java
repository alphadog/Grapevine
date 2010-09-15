package com.alphadog.grapevine.widgets;

import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.RemoteViews;

import com.alphadog.grapevine.R;

public class GrapevineUpdateProvider extends AppWidgetProvider {
	
	//The action broadcast value that is uniquely identified
	public static final String MAP_VIEW = "com.alphadog.grapevine.widgets.GrapevineUpdateProvider.MapView"; 

	@Override
	public void onReceive(Context context, Intent intent) {
		if(MAP_VIEW.equals(intent.getAction())) {
			//TODO Need to kick off the app which will show the user review on the map
		} else {
			super.onReceive(context, intent);
		}
	}
	
	@Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		//Currently I am using a service and not AppWidgetProvider to update widget
		//as I think it doesn't have wait timeout restriction, so the app doesn't 
		//seem to hang, instead it continues to wait until the update is available.
		context.startService(new Intent(context, UpdateService.class));
    }
	
	class UpdateService extends IntentService {
		private SharedPreferences preferences = null;

		public UpdateService(String name) {
			//Just a unique string to create a worker with a name
			super("GrapevineUpdateProvider+UpdateService");
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
			updatedView.setOnClickPendingIntent(R.id.widget_text, pi);
			
			return updatedView;
		}
	}
}