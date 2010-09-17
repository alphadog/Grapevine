package com.alphadog.grapevine.alarms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.alphadog.grapevine.services.WidgetUpdateService;


public class WidgetUpdateAlarmReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i("WidgetUpdateAlarmReceiver", "Received Alarm Notification, will invoke widget update service");
		context.startService(new Intent(context, WidgetUpdateService.class));
	}

}
