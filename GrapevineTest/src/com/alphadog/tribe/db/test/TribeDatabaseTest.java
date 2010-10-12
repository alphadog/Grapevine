package com.alphadog.tribe.db.test;

import android.database.sqlite.SQLiteDatabase;
import android.test.ActivityInstrumentationTestCase2;

import com.alphadog.tribe.activities.ReviewListingActivity;
import com.alphadog.tribe.db.TribeDatabase;


public class TribeDatabaseTest extends ActivityInstrumentationTestCase2<ReviewListingActivity> {

	private TribeDatabase testDBInstance;
	private SQLiteDatabase db;
	
	public TribeDatabaseTest() {
		super("com.alphadog.tribe.activities", ReviewListingActivity.class);
	}
	
	public void setUp() throws Exception {
		testDBInstance = new TribeDatabase(getActivity(), "GRAPEVINE_TEST");
		db = testDBInstance.getWritableDatabase();
	}
	
	public void testDatabaseCreatedSuccessfully() {
		assertNotNull(db);
	}
	
	public void tearDown() throws Exception {
		testDBInstance.close();
		getActivity().finish();
		super.tearDown();
	}
}