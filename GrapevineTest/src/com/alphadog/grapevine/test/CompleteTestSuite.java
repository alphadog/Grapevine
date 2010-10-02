package com.alphadog.grapevine.test;

import junit.framework.TestSuite;
import android.test.InstrumentationTestRunner;
import android.test.InstrumentationTestSuite;

import com.alphadog.grapevine.db.test.GrapevineDatabaseTest;
import com.alphadog.grapevine.db.test.ReviewTableTest;
import com.alphadog.grapevine.models.test.ReviewTest;
import com.alphadog.grapevine.services.test.WidgetUpdateServiceTest;

public class CompleteTestSuite extends InstrumentationTestRunner {

	@Override
	public TestSuite getAllTests() {
		InstrumentationTestSuite testSuite = new InstrumentationTestSuite(this);
		
		testSuite.addTestSuite(GrapevineDatabaseTest.class);
		testSuite.addTestSuite(ReviewTableTest.class);
		testSuite.addTestSuite(ReviewTest.class);
		testSuite.addTestSuite(WidgetUpdateServiceTest.class);
		return testSuite;
	}
	
	@Override
	public ClassLoader getLoader() {
		return CompleteTestSuite.class.getClassLoader();
	}
	
}
