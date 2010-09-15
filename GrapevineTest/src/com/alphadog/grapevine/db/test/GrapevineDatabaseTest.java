package com.alphadog.grapevine.db.test;

import android.database.sqlite.SQLiteDatabase;
import android.test.ActivityInstrumentationTestCase2;

import com.alphadog.grapevine.Dashboard;
import com.alphadog.grapevine.db.GrapevineDatabase;


public class GrapevineDatabaseTest extends ActivityInstrumentationTestCase2<Dashboard> {

	private GrapevineDatabase testDBInstance;
	private SQLiteDatabase db;
	
	public GrapevineDatabaseTest() {
		super("com.alphadog.grapevine", Dashboard.class);
	}
	
	public void setUp() throws Exception {
		testDBInstance = new GrapevineDatabase(getActivity(), "GRAPEVINE_TEST");
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