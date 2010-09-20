package com.alphadog.grapevine.models;

import org.json.JSONException;
import org.json.JSONObject;

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

	public Review(long reviewId, String heading, String description, String imageUrl, String longitude, String latitude, int like) {
		this.id = reviewId;
		this.heading = heading;
		this.description = description;
		this.imageUrl = imageUrl;
		this.longitude = longitude;
		this.latitude = latitude;
		this.like = (like == 1); 
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
		if(jsonObject != null) {
			try {
				text = jsonObject.getString("text");
				like = jsonObject.getBoolean("like") ? 1 : 0;
				imageUrl = jsonObject.getString("image_url");
				latitude = jsonObject.getString("latitude");
				longitude = jsonObject.getString("longitude");
				convertedReview = new Review(-1, text,null, imageUrl, longitude, latitude, like);
			} catch (JSONException e) {
				Log.e("Review","Error occured while creating a Review object from json "+ e.getMessage());
			}
		}
		
		return convertedReview;
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
