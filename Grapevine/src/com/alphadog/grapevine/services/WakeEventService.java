package com.alphadog.grapevine.services;

import android.app.Service;
import android.content.Context;
import android.os.PowerManager;
import android.util.Log;

abstract public class WakeEventService extends Service {

	private static final String LOCK_NAME_STATIC = "com.alphadog.grapevine.services.WakeEventService";
	private static PowerManager.WakeLock lockStatic;
	
	public static void acquireStaticLock(Context context) {
		getLock(context).acquire();
	}
	
	synchronized private static PowerManager.WakeLock getLock(Context context) {
		if (lockStatic==null) {
			PowerManager mgr=(PowerManager)context.getSystemService(Context.POWER_SERVICE);
			lockStatic=mgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,LOCK_NAME_STATIC);
			lockStatic.setReferenceCounted(true);
		}

		return(lockStatic);
	}

	public abstract void doServiceTask();
	
	@Override
	public void onCreate() {
		super.onCreate();
		doServiceTask();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.d("WakeEventService", "Releasing Lock now!");
		getLock(this).release();
	}
}
