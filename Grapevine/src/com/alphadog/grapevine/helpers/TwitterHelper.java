package com.alphadog.grapevine.helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class TwitterHelper {
	
	private SharedPreferences preferences;

	public TwitterHelper(Context context) {
		preferences = PreferenceManager.getDefaultSharedPreferences(context);
	}
	
	public boolean isTwitterCredentialsSetup() {
		String username = preferences.getString("twitter_username", "");
		String password = preferences.getString("twitter_password", "");
		
		return (username != null && password != null && username.trim().length() > 0 && password.trim().length() > 0);
	}

}
