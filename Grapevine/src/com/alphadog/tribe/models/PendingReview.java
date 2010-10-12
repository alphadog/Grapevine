package com.alphadog.tribe.models;

public class PendingReview extends Review {

	private String imagePath;
	private String error;
	private String status;
	private long retries;
	private long id;

	public PendingReview(long pendingReviewId, Review review, String imagePath) {
		this(pendingReviewId, review.getHeading(), review.getDescription(), review.getImageUrl(), imagePath,
				review.getLongitude(), review.getLatitude(), review.isLike() ? 1 : 0, review.getReviewDate(), review.getUsername(), review.getLocationName() , null, null, 0);
	}
	
	public PendingReview(	long pendingReviewId, String heading, String description,
							String imageUrl, String imagePath, String longitude, String latitude, 
							int like, String reviewDate, String username, String locationName, String error, String status, long retries) 
	{
		super(-1, heading, description, imageUrl, longitude, latitude, like, reviewDate, username, locationName);
		this.id = pendingReviewId;
		this.error = error;
		this.status = status;
		this.retries = retries;
		this.imagePath = imagePath;
	}

	public long getReviewId() {
		return super.getId();
	}
	
	public long getId() {
		return this.id;
	}
	
	public String getError() {
		return error;
	}

	public String getStatus() {
		return status;
	}

	public long getRetries() {
		return retries;
	}
	
	public String getImagePath() {
		return this.imagePath;
	}	

	public String setImagePath(String imagePath) {
		return this.imagePath = imagePath;
	}

	public String getTwitterMessage() {
		if(getHeading() != null) {
			int end = getHeading().length() > 108 ? 108 : getHeading().length();
			return getHeading() != null ? getHeading().substring(0, end) + " " : "";
		}
		return "";
	}	
}
