package com.alphadog.tribe.helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class TwitterHelper {
	
	private SharedPreferences preferences;

	public TwitterHelper(Context context) {
		preferences = PreferenceManager.getDefaultSharedPreferences(context);
	}
	
	public boolean isTwitterCredentialsSetup() {
		String username = preferences.getString("twitter_username", "").trim();
		String password = preferences.getString("twitter_password", "").trim();
		
		return (username != null && password != null && username.length() > 0 && password.length() > 0);
	}

}
