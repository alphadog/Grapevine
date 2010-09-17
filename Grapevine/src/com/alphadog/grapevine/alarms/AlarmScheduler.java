package com.alphadog.grapevine.alarms;

import android.app.AlarmManager;
import android.content.Context;

public abstract class AlarmScheduler {
	
	protected AlarmManager alarmManager;
	protected Context context;
	
	public AlarmScheduler(Context context) {
		this.context = context;
		alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
	}

	public abstract void updateAlarmSchedule();

	public abstract void cancelAlarmSchedule();
	
}