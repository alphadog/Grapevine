package com.alphadog.grapevine.widgets;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.alphadog.grapevine.activities.ReviewsMapActivity;
import com.alphadog.grapevine.alarms.WidgetUpdateAlarmScheduler;
import com.alphadog.grapevine.services.WidgetUpdateService;

public class GrapevineUpdateProvider extends AppWidgetProvider {

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i("GrapevineUpdateProvider", "Received Intent with action " + intent.getAction());
		
		if(AppWidgetManager.ACTION_APPWIDGET_ENABLED.equals(intent.getAction())) {
			(new WidgetUpdateAlarmScheduler(context)).updateAlarmSchedule();
		} else if(AppWidgetManager.ACTION_APPWIDGET_DISABLED.equals(intent.getAction())) {
			(new WidgetUpdateAlarmScheduler(context)).cancelAlarmSchedule();
<<<<<<< HEAD
		} else if(WidgetUpdateService.APP_VIEW.equals(intent.getAction())) {
			//TODO Open Map view but For now just open our app
			Intent appIntent = new Intent(context, Dashboard.class);
=======
		}  else if(WidgetUpdateService.APP_VIEW.equals(intent.getAction())) {
			Intent appIntent = new Intent(context, ReviewsMapActivity.class);
>>>>>>> b5550f84005e1189024625b0956bd633a30a13ec
			appIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(appIntent);
		} else {
			super.onReceive(context, intent);
		}
	}
	
	@Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		Log.i("GrapevineUpdateProvider", "Called On Update for the widget. Now service will be invoked");
		
		//It is critical to use a service here as, as per android documentation
		//since WidgetProvider is just a broadcast receiver, it's considerd disabled
		//as soon as onRecieve method returns. And so are all spawned threads. So
		//for any long processes a separate service must be invoked! If this is
		//not done; the threads can be killed by jvm at any point of time. 
		context.startService(new Intent(context, WidgetUpdateService.class));
    }

}