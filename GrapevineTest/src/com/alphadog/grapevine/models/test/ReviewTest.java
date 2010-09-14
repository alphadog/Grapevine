package com.alphadog.grapevine.models.test;

import junit.framework.TestCase;

import com.alphadog.grapevine.models.Review;


public class ReviewTest extends TestCase {
	
	private Review testReview;
	
	public void testReviewObjectAttributesGettersSetters() {
		testReview = new Review(-1, "Heading", "Description", 11, 12, 0);
		assertNotNull(testReview);
		assertEquals("Heading", testReview.getHeading());
		assertEquals("Description", testReview.getDescription());
		assertEquals(11, testReview.getImageId());
		assertEquals(12, testReview.getMapLocationId());
		assertEquals(false, testReview.isGripe());
		testReview.setId(12);
		assertEquals(12, testReview.getId());
	}
	
	public void testEqualizationOfReview() {
		Review review = new Review(-1, "Heading", "Description", 11, 12, 0);
		Review duplicateReview = new Review(-1, "Heading", "Description", 11, 12, 0);
		assertFalse(review.equals(new StringBuffer("Random Object")));
		assertTrue(review.equals(duplicateReview));
		assertTrue(review.equals(review));
		duplicateReview.setId(122);
		assertFalse(review.equals(duplicateReview));
	}
}
