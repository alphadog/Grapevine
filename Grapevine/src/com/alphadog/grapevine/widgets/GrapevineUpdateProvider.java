package com.alphadog.grapevine.widgets;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;

import com.alphadog.grapevine.services.WidgetUpdateService;

public class GrapevineUpdateProvider extends AppWidgetProvider {

	@Override
	public void onReceive(Context context, Intent intent) {
		if(WidgetUpdateService.MAP_VIEW.equals(intent.getAction())) {
			//TODO Need to kick off the app which will show the user review on the map
		} else {
			super.onReceive(context, intent);
		}
	}
	
	@Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		//It is critical to use a service here as, as per android documentation
		//since WidgetProvider is just a broadcast receiver, it's considerd disabled
		//as soon as onRecieve method returns. And so are all spawned threads. So
		//for any long processes a separate service must be invoked! If this is
		//not done; the threads can be killed by jvm at any point of time. 
		context.startService(new Intent(context, WidgetUpdateService.class));
    }

}