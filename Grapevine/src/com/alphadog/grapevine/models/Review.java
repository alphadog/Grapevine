package com.alphadog.grapevine.models;


public class Review {

	private long id;
	private String heading;
	private String description;
	private long imageId;
	private long mapLocationId;
	private boolean isGripe;

	public Review(long reviewId, String heading, String description, long imageId, long mapLocationId, int gripe) {
		this.id = reviewId;
		this.heading = heading;
		this.description = description;
		this.imageId = imageId;
		this.mapLocationId = mapLocationId;
		this.isGripe = (gripe == 1); 
	}

	public void setId(long id) {
		this.id = id;
	}
	
	public long getId() {
		return this.id;
	}

	public long getMapLocationId() {
		return this.mapLocationId;
	}

	public long getImageId() {
		return this.imageId;
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
			   this.getImageId() == object.getImageId() &&
			   this.getMapLocationId() == object.getMapLocationId() &&
			   this.isGripe() == object.isGripe();
	}
}
