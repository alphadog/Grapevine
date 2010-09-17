package com.alphadog.grapevine.alarms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class AlarmsInitiator extends BroadcastReceiver {

	//This class gets an invocation at the phone boot time and it is
	//responsible for setting the repeating alarms of all the services
	//which run in background at periodic intervals.
	
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i("AlarmsInitiator", "Received Boot Notification. Will start service and will do a recurring schedule for all alarms");
		(new ReviewsSyncServiceAlarmScheduler(context)).updateAlarmSchedule();
	}
	
}
