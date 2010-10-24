package com.alphadog.tribe.helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import static com.alphadog.tribe.helpers.StringHelpers.isBlank;

public class TwitterHelper {
	public static boolean isTwitterCredentialsSetup(Context context) {
	    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		String username = preferences.getString("twitter_username", "").trim();
		String password = preferences.getString("twitter_password", "").trim();
		return (isBlank(username) || isBlank(password));
	}
}
