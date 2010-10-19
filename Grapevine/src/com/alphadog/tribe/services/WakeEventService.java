package com.alphadog.tribe.services;

import android.app.Service;
import android.content.Context;
import android.os.PowerManager;
import android.util.Log;

abstract public class WakeEventService extends Service {

    private static final String LOCK_NAME_STATIC = "com.alphadog.tribe.services.WakeEventService";
    private static PowerManager.WakeLock sLock;

    public static void acquireLock(Context context) {
        getLock(context).acquire();
    }

    synchronized private static PowerManager.WakeLock getLock(Context context) {
        if (sLock == null) {
            PowerManager mgr = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            sLock = mgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, LOCK_NAME_STATIC);
            sLock.setReferenceCounted(true);
        }

        return (sLock);
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
