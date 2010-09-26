package com.alphadog.grapevine.models;

public class PendingReview extends Review {

	private String error;
	private String status;
	private long retries;
	private long id;

	public PendingReview(long pendingReviewId, Review review) {
		this(pendingReviewId, review.getHeading(), review.getDescription(), review.getImageUrl(),
				review.getLongitude(), review.getLatitude(), review.isLike() ? 1 : 0 , null, null, 0);
	}
	
	public PendingReview(	long pendingReviewId, String heading, String description,
							String imageUrl, String longitude, String latitude, int like,
							String error, String status, long retries) 
	{
		super(-1, heading, description, imageUrl, longitude, latitude, like);
		this.id = pendingReviewId;
		this.error = error;
		this.status = status;
		this.retries = retries;
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
}
