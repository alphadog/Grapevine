package com.alphadog.tribe.alarms;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;

public class ReviewsSyncServiceAlarmScheduler extends AlarmScheduler {

	public ReviewsSyncServiceAlarmScheduler(Context context) {
		super(context);
	}
	
	@Override
	public void updateAlarmSchedule() {
		Intent i=new Intent(context, ReviewsSyncServiceAlarmReceiver.class);
		PendingIntent pi=PendingIntent.getBroadcast(context, 0, i, 0);
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		
		//By Default every 15 minutes if preferences is not set.
		//And we don't wake the device up if it's sleeping
		String period = sharedPreferences.getString("time_freq_unit", "900000");
		try {
			alarmManager.setRepeating(	AlarmManager.ELAPSED_REALTIME, 
										SystemClock.elapsedRealtime()+5000,
										Integer.parseInt(period),
										pi);
			Log.i("ReviewsSyncServiceAlarmScheduler","ReviewsSyncServiceAlarmReceiver Alarm scheduled to go off at an interval of "+period+" millisec");
		}
		catch(Exception e) {
			Log.e("ReviewsSyncServiceAlarmScheduler", "Could not reschedule the ReviewsSyncServiceAlarmReceiver's alarm, exception occured." + e.getMessage());
		}
	}

	@Override
	public void cancelAlarmSchedule() {
		Intent i=new Intent(context, ReviewsSyncServiceAlarmReceiver.class);
		PendingIntent pi=PendingIntent.getBroadcast(context, 0, i, 0);
		alarmManager.cancel(pi);
	}
}
