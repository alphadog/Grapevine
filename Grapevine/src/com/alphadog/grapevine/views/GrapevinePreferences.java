package com.alphadog.grapevine.views;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.util.Log;

import com.alphadog.grapevine.R;
import com.alphadog.grapevine.alarms.ReviewsSyncServiceAlarmScheduler;

public class GrapevinePreferences extends PreferenceActivity implements OnSharedPreferenceChangeListener {

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }
	
	@Override
	public void onResume() {
		super.onResume();
		getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
	}
	
	@Override
	public void onPause() {
		super.onPause();
		getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
	}
	
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		Log.i("Preference Change Listener", "Change Listener has been called for key:: " + key);
		if("time_freq_unit".equalsIgnoreCase(key)) {
			Log.i("GrapevinePreferences", "Updating alarm schedule for service");
			new ReviewsSyncServiceAlarmScheduler(this).updateAlarmSchedule();
		}
	}
}
