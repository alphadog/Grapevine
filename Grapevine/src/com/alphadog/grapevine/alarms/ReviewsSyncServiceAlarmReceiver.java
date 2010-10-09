package com.alphadog.grapevine.alarms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.alphadog.grapevine.services.ReviewUploadService;
import com.alphadog.grapevine.services.ReviewsSyncService;

public class ReviewsSyncServiceAlarmReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i("ReviewsSyncServiceAlarmReceiver", "Received Alarm Notification, will invoke review sync service");
	
		//Before we fire the service sync, we should upload pending reviews from
		//current phone.	
	 	ReviewUploadService.acquireStaticLock(context);
		context.startService(new Intent(context, ReviewUploadService.class));	

		//This will ensure that if alarm exists when phone is going to sleep
		//It'll acquire a lock on CPU and while screen is allowed to
		//get switched off. 
		ReviewsSyncService.acquireStaticLock(context);
		context.startService(new Intent(context, ReviewsSyncService.class));
	}

}
