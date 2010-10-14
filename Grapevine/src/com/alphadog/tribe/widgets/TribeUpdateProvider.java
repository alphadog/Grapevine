package com.alphadog.tribe.widgets;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.alphadog.tribe.activities.CameraActivity;
import com.alphadog.tribe.activities.ReviewDetailsActivity;
import com.alphadog.tribe.alarms.WidgetUpdateAlarmScheduler;
import com.alphadog.tribe.services.WidgetUpdateService;

public class TribeUpdateProvider extends AppWidgetProvider {

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i("TribeUpdateProvider", "Received Intent with action " + intent.getAction());
		
		if(AppWidgetManager.ACTION_APPWIDGET_ENABLED.equals(intent.getAction())) {
			(new WidgetUpdateAlarmScheduler(context)).updateAlarmSchedule();
		} else if(AppWidgetManager.ACTION_APPWIDGET_DISABLED.equals(intent.getAction())) {
			(new WidgetUpdateAlarmScheduler(context)).cancelAlarmSchedule();
		}  else if(WidgetUpdateService.APP_VIEW.equals(intent.getAction())) {
			Intent appIntent = new Intent(context, ReviewDetailsActivity.class);
			appIntent.putExtra("REVIEW_ID", intent.getLongExtra("REVIEW_ID",1));
			appIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(appIntent);
		}  else if(WidgetUpdateService.NEW_REVIEW.equals(intent.getAction())) {
			Intent appIntent = new Intent(context, CameraActivity.class);
			appIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(appIntent);
		} else {
			super.onReceive(context, intent);
		}
	}
	
	@Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		Log.i("TribeUpdateProvider", "Called On Update for the widget. Now service will be invoked");
		
		//It is critical to use a service here as, as per android documentation
		//since WidgetProvider is just a broadcast receiver, it's considerd disabled
		//as soon as onRecieve method returns. And so are all spawned threads. So
		//for any long processes a separate service must be invoked! If this is
		//not done; the threads can be killed by jvm at any point of time. 
		context.startService(new Intent(context, WidgetUpdateService.class));
    }

}