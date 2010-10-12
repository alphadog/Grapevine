package com.alphadog.tribe.test;

import junit.framework.TestSuite;
import android.test.InstrumentationTestRunner;
import android.test.InstrumentationTestSuite;

import com.alphadog.tribe.db.test.TribeDatabaseTest;
import com.alphadog.tribe.db.test.ReviewTableTest;
import com.alphadog.tribe.models.test.ReviewTest;
import com.alphadog.tribe.services.test.WidgetUpdateServiceTest;

public class CompleteTestSuite extends InstrumentationTestRunner {

	@Override
	public TestSuite getAllTests() {
		InstrumentationTestSuite testSuite = new InstrumentationTestSuite(this);
		
		testSuite.addTestSuite(TribeDatabaseTest.class);
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
