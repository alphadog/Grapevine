package com.alphadog.tribe.alarms;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;

public class ReviewCleanupServiceAlarmScheduler extends AlarmScheduler {

	public ReviewCleanupServiceAlarmScheduler(Context context) {
		super(context);
	}

	@Override
	public void updateAlarmSchedule() {
		Intent i=new Intent(context, ReviewCleanupServiceAlarmReceiver.class);
		PendingIntent pi=PendingIntent.getBroadcast(context, 0, i, 0);
		
		String period = ScheduleCalculator.scheduleForReviewCleanup();
		try {
			alarmManager.setRepeating(	AlarmManager.ELAPSED_REALTIME, 
										SystemClock.elapsedRealtime()+5000,
										Integer.parseInt(period),
										pi);
			Log.i("UploadImageServiceAlarmScheduler","ImageUploadServiceAlarmScheduler Alarm scheduled to go off at an interval of "+period+" millisec");
		}
		catch(Exception e) {
			Log.e("UploadImageServiceAlarmScheduler", "Could not reschedule the UploadImageServiceAlarmReceiver's alarm, exception occured." + e.getMessage());
		}
	}

	@Override
	public void cancelAlarmSchedule() {
		Log.i("UploadImageServiceAlarmScheduler","Cancelling all existing alarms");
		Intent i=new Intent(context, ReviewCleanupServiceAlarmReceiver.class);
		PendingIntent pi=PendingIntent.getBroadcast(context, 0, i, 0);
		alarmManager.cancel(pi);
	}

}
