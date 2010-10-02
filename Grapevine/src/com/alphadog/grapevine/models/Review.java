package com.alphadog.grapevine.models;

import org.json.JSONException;
import org.json.JSONObject;

import android.text.format.Time;
import android.util.Log;

import com.google.android.maps.GeoPoint;


public class Review {

	private long id;
	private String heading;
	private String description;
	private String imageUrl;
	private String latitude;
	private String longitude;
	private boolean like;
	private String reviewDate;

	private static String localtimezone = new Time().timezone;
	
	public Review(long reviewId, String heading, String description, String imageUrl, String longitude, String latitude, int like, String reviewDate) {
		this.id = reviewId;
		this.heading = heading;
		this.description = description;
		this.imageUrl = imageUrl;
		this.longitude = longitude;
		this.latitude = latitude;
		this.like = (like == 1);
		this.reviewDate = reviewDate;
	}

	public void setId(long id) {
		this.id = id;
	}
	
	public long getId() {
		return this.id;
	}

	public String getLongitude() {
		return this.longitude;
	}
	
	public String getLatitude() {
		return this.latitude;
	}

	public String getImageUrl() {
		return this.imageUrl;
	}

	public String getDescription() {
		return this.description;
	}

	public String getHeading() {
		return this.heading;
	}

	public boolean isLike() {
		return this.like;
	}
	
	@Override
	public boolean equals(Object objectToCompare) {
		if(!(objectToCompare instanceof Review)) {
			return false;
		}
		Review object = (Review)objectToCompare;
		return this.getId() == object.getId() &&
			   this.getHeading().equals(object.getHeading()) &&
			   this.getDescription().equals(getDescription()) &&
			   this.getImageUrl().equals(object.getImageUrl()) &&
			   this.getLatitude().equals(object.getLatitude()) &&
			   this.getLongitude().equals(object.getLongitude()) &&
			   this.isLike() == object.isLike();
	}

	public static Review fromJsonObject(JSONObject jsonObject) {
		Review convertedReview = null;
		String text;
		int like;
		String imageUrl;
		String latitude;
		String longitude;
		String reviewDate;
		if(jsonObject != null) {
			try {
				text = jsonObject.getString("text");
				like = jsonObject.getBoolean("like") ? 1 : 0;
				imageUrl = jsonObject.getString("image_url");
				latitude = jsonObject.getString("latitude");
				longitude = jsonObject.getString("longitude");
//				reviewDate = jsonObject.getString("review_date");
				convertedReview = new Review(-1, text,null, imageUrl, longitude, latitude, like, null);
			} catch (JSONException e) {
				Log.e("Review","Error occured while creating a Review object from json "+ e.getMessage());
			}
		}
		
		return convertedReview;
	}
	
	public String getReviewDateInLocalTime() {
		if(getReviewDate() != null) {
			Time newTime = new Time("UTC");
			newTime.switchTimezone(localtimezone);
			newTime.parse3339(getReviewDate());
			return  newTime.format("%d-%b-%Y %H:%M:%S");
		}
		
		return "";
	}
	
	public String getReviewDate() {
		return reviewDate;
	}

	public int getMicroLatitudes() {
		if(null != this.getLatitude()) {
			Double value = Double.parseDouble(this.getLatitude()) * 10E5;
			return value.intValue();
		}
		return 0;
	}
	
	public int getMicroLongitudes() {
		if(null != this.getLongitude()) {
			Double value = Double.parseDouble(this.getLongitude()) * 10E5;
			return value.intValue();
		}
		return 0;
	}

	public GeoPoint getGeoPoint() {
		return new GeoPoint(getMicroLatitudes(), getMicroLongitudes());
	}
}
