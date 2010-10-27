package com.alphadog.tribe.alarms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.alphadog.tribe.services.ReviewsSyncService;

public class ReviewsSyncServiceAlarmReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i("ReviewsSyncServiceAlarmReceiver", "Received Alarm Notification, will invoke review sync service");

		ReviewsSyncService.acquireStaticLock(context);
		context.startService(new Intent(context, ReviewsSyncService.class));
	}

}
