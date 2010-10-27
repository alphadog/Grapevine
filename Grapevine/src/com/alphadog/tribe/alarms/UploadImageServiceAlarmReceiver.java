package com.alphadog.tribe.alarms;

import com.alphadog.tribe.services.ReviewUploadService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class UploadImageServiceAlarmReceiver extends BroadcastReceiver {
	
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i("UploadImageServiceAlarmReceiver", "Received Alarm Notification, will invoke review upload service");
	 	ReviewUploadService.acquireStaticLock(context);
		context.startService(new Intent(context, ReviewUploadService.class));	
	}

}
