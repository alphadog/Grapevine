package com.alphadog.grapevine.db.test;

import java.util.List;

import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.test.ActivityInstrumentationTestCase2;

import com.alphadog.grapevine.activities.Dashboard;
import com.alphadog.grapevine.db.GrapevineDatabase;
import com.alphadog.grapevine.db.ReviewsTable;
import com.alphadog.grapevine.db.Table;
import com.alphadog.grapevine.models.Review;

public class ReviewTableTest extends ActivityInstrumentationTestCase2<Dashboard> {
	
	private GrapevineDatabase testDBInstance;
	private SQLiteDatabase db;
	private Table<Review> reviewsTable;
	
	public ReviewTableTest() {
		super("com.alphadog.grapevine.activities", Dashboard.class);
	}
	
	@Override
	public void setUp() throws Exception {
		testDBInstance = new GrapevineDatabase(getActivity(), "GRAPEVINE_TEST");
		reviewsTable = new ReviewsTable(testDBInstance);
		db = testDBInstance.getWritableDatabase();
	}
	
	public void testCreationOfNewReviewRecord() {
		String heading = "I hate this place really!";
		String description = "This place has been a real pain in butt, like forever now.";
		Review reviewRecord = new Review(-1, heading, description, 12,11, 1); 
		
		reviewRecord = reviewsTable.create(reviewRecord);
		
		assertNotSame(-1, reviewRecord.getId());
		assertEquals(heading, reviewRecord.getHeading());
		assertEquals(description, reviewRecord.getDescription());
		assertEquals(12, reviewRecord.getImageId());
		assertEquals(11, reviewRecord.getMapLocationId());
		assertEquals(true, reviewRecord.isGripe());
	}

	public void testLookupOfReviewsWithReviewId() {
		
		String description1 = "This place is so sucky, that I almost never ever go there!";
		String description2 = "This is going to be a big surprise for you guys, it's amazing";
		String heading1 = "Bad Place";
		String heading2 = "Good Place";
		Review reviewRecordOne = new Review(-1, heading1, description1, 12,11, 1); 
		Review reviewRecordTwo = new Review(-1, heading2, description2, 12,11, 0); 
		
		reviewRecordOne = reviewsTable.create(reviewRecordOne);
		reviewRecordTwo = reviewsTable.create(reviewRecordTwo);
		
		Review fetchedReview = reviewsTable.findById(reviewRecordOne.getId());
		
		assertEquals(reviewRecordOne, fetchedReview);
	}
	
	public void testLookupOfAllReviewRecords() {
		String description1 = "This place is so sucky, that I almost never ever go there!";
		String description2 = "This is going to be a big surprise for you guys, it's amazing";
		String heading1 = "Bad Place";
		String heading2 = "Good Place";
		Review reviewRecordOne = new Review(-1, heading1, description1, 12,11, 1); 
		Review reviewRecordTwo = new Review(-1, heading2, description2, 12,11, 0); 
		
		reviewRecordOne = reviewsTable.create(reviewRecordOne);
		reviewRecordTwo = reviewsTable.create(reviewRecordTwo);
		
		List<Review> reviewRecords = reviewsTable.findAll();
		
		assertEquals(2, reviewRecords.size());
		assertEquals(reviewRecordTwo, reviewRecords.get(1));
		assertEquals(reviewRecordOne, reviewRecords.get(0));
	}
	
	@Override
	public void tearDown() throws Exception {
		dbCleanup("DELETE FROM REVIEWS;");
		
		try {
			//to compress the unused space and clean pages left after cleaning tables
			db.execSQL("VACUUM;");
		} catch(SQLException sqle) {
			fail();
			sqle.printStackTrace();
		} finally {
			testDBInstance.close();
		}
		
		getActivity().finish();
		super.tearDown();
	}
	
	//Since android doesn't provide db transaction methods, we'll clean up db ourselves
	//This is dependent on real code which it shouldn't be but for now I think it's ok.
	private void dbCleanup(final String sqlStatement) {
		db.beginTransaction();
		try {
			db.execSQL(sqlStatement);
			db.setTransactionSuccessful();
		} catch(SQLException sqle) {
			sqle.printStackTrace();
			fail();
		}finally {
			db.endTransaction();
		}
	}
}
