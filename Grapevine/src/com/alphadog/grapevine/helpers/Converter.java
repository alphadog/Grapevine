package com.alphadog.grapevine.helpers;

import java.util.Iterator;
import java.util.Map;

import android.content.ContentValues;
import android.util.Log;

public class Converter {
	
	public static ContentValues toContentValues(Map<String, String> fromMap, ContentValues optionalTo) {
		ContentValues toMap = optionalTo;
		if (toMap == null) {
			toMap = new ContentValues();
		}
		
		Iterator<String> iterator = fromMap.keySet().iterator();
		while(iterator.hasNext()) {
			String key = iterator.next();
			toMap.put(key, fromMap.get(key));
			Log.d("PendingReviewsTable", "Updating table column [" + key + "] with value [" + fromMap.get(key) + "]");
		}
		return toMap;
	}
}
