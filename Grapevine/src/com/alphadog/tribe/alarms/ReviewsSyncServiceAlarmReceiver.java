package com.alphadog.tribe.alarms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.alphadog.tribe.services.ReviewCleanupService;
import com.alphadog.tribe.services.ReviewUploadService;
import com.alphadog.tribe.services.ReviewsSyncService;

public class ReviewsSyncServiceAlarmReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i("ReviewsSyncServiceAlarmReceiver", "Received Alarm Notification, will invoke review sync service");
	
		//Before we fire the service sync, we should upload pending reviews from
		//current phone.	
	 	ReviewUploadService.acquireLock(context);
		context.startService(new Intent(context, ReviewUploadService.class));	

		//This will ensure that if alarm exists when phone is going to sleep
		//It'll acquire a lock on CPU and while screen is allowed to
		//get switched off. 
		ReviewsSyncService.acquireLock(context);
		context.startService(new Intent(context, ReviewsSyncService.class));

		//This will ensure that all the old stale and orphan data is cleaned up
		//everytime we try to sync
		ReviewCleanupService.acquireLock(context);
		context.startService(new Intent(context, ReviewCleanupService.class));
	}

}
