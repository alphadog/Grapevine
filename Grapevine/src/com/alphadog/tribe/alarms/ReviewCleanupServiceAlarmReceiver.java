package com.alphadog.tribe.alarms;

import com.alphadog.tribe.services.ReviewCleanupService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ReviewCleanupServiceAlarmReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i("ReviewCleanupServiceAlarmReceiver", "Received Alarm Notification, will invoke review cleanup service");
		ReviewCleanupService.acquireStaticLock(context);
		context.startService(new Intent(context, ReviewCleanupService.class));
	}

}
