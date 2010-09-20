package com.alphadog.grapevine.views;

import com.alphadog.grapevine.models.Review;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.OverlayItem;

public class ReviewOverlay extends OverlayItem {

	private Review review;
	
	public ReviewOverlay(GeoPoint point, String title, String snippet, Review review) {
		super(point, title, snippet);
		this.review = review;
	}
	
	public Review getReviewDetails() {
		return this.review;
	}
}
