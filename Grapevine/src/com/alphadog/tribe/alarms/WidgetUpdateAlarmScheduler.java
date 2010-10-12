package com.alphadog.tribe.alarms;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;

public class WidgetUpdateAlarmScheduler extends AlarmScheduler {
	
	public WidgetUpdateAlarmScheduler(Context context) {
		super(context);
	}
	
	@Override
	public void updateAlarmSchedule() {
		Intent i=new Intent(context, WidgetUpdateAlarmReceiver.class);
		PendingIntent pi=PendingIntent.getBroadcast(context, 0, i, 0);
		
		String period = "10000";
		try {
			alarmManager.setRepeating(	AlarmManager.ELAPSED_REALTIME_WAKEUP, 
										SystemClock.elapsedRealtime()+5000,
										Integer.parseInt(period),
										pi);
			Log.i("WidgetUpdateAlarmScheduler","WidgetUpdateAlarmReceiver Alarm scheduled to go off at an interval of "+period+" millisec");
		}
		catch(Exception e) {
			Log.e("WidgetUpdateAlarmScheduler", "Could not reschedule the WidgetUpdateAlarmReceiver's alarm, exception occured." + e.getMessage());
		}
	}

	@Override
	public void cancelAlarmSchedule() {
		Intent i=new Intent(context, WidgetUpdateAlarmReceiver.class);
		PendingIntent pi=PendingIntent.getBroadcast(context, 0, i, 0);
		alarmManager.cancel(pi);
	}

}
