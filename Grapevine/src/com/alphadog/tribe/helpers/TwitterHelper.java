package com.alphadog.tribe.helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import static com.alphadog.tribe.helpers.StringHelpers.isBlank;

public class TwitterHelper {
	public static boolean isTwitterCredentialsSetup(Context c) {
	    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(c);
		String username = preferences.getString("twitter_username", "").trim();
		String password = preferences.getString("twitter_password", "").trim();
		return (!isBlank(username) && !isBlank(password));
	}
	
	public static boolean isTweetingRequested(Context c) {
	    return PreferenceManager.getDefaultSharedPreferences(c).getBoolean("tweet_always", false);
	}
}
