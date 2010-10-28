package com.alphadog.tribe;

import android.app.Application;
import android.content.ComponentCallbacks;
import android.content.res.Configuration;
import android.util.Log;

import com.alphadog.tribe.db.TribeDatabase;

public class TribeApplication extends Application implements ComponentCallbacks {

    private TribeDatabase mDB;

    public TribeApplication() {
        super();
        mDB = new TribeDatabase(this);
        Log.i("TribeApplication", "Initialized!");
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        // TODO Auto-generated method stub
        // Nothing to do yet.
    }

    @Override
    public void onLowMemory() {
        return;
    }

    public TribeDatabase getDB() {
        Log.i("TribeApplication", "Returning DB");
        return mDB;
    }

    @Override
    public void onTerminate() {
        mDB.close();
    }
}
