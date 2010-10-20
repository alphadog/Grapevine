package com.alphadog.tribe.helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class TwitterHelper {
	
	public static boolean isTwitterCredentialsSetup(Context context) {
	    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		String username = preferences.getString("twitter_username", "").trim();
		String password = preferences.getString("twitter_password", "").trim();
		return (username != null && password != null && username.length() > 0 && password.length() > 0);
	}
}
