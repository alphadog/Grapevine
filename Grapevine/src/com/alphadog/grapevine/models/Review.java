package com.alphadog.grapevine.models;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;


public class Review {

	private long id;
	private String heading;
	private String description;
	private String imageUrl;
	private String latitude;
	private String longitude;
	private boolean isGripe;

	public Review(long reviewId, String heading, String description, String imageUrl, String longitude, String latitude, int gripe) {
		this.id = reviewId;
		this.heading = heading;
		this.description = description;
		this.imageUrl = imageUrl;
		this.longitude = longitude;
		this.latitude = latitude;
		this.isGripe = (gripe == 1); 
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

	public boolean isGripe() {
		return this.isGripe;
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
			   this.isGripe() == object.isGripe();
	}

	public static Review fromJsonObject(JSONObject jsonObject) {
		Review convertedReview = null;
		String text;
		boolean gripe;
		String imageUrl;
		String latitude;
		String longitude;
		if(jsonObject != null) {
			try {
				text = jsonObject.getString("text");
				gripe = jsonObject.getBoolean("gripe");
				imageUrl = jsonObject.getString("image_url");
				latitude = jsonObject.getString("latitude");
				longitude = jsonObject.getString("longitude");
				convertedReview = new Review(-1, text,null, imageUrl, longitude, latitude, gripe ? 1 : 0);
			} catch (JSONException e) {
				Log.e("Review","Error occured while creating a Review object from json "+ e.getMessage());
			}
		}
		
		return convertedReview;
	}
}
