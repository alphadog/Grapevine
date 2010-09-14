package com.alphadog.grapevine.test;

import junit.framework.TestSuite;
import android.test.InstrumentationTestRunner;
import android.test.InstrumentationTestSuite;

import com.alphadog.grapevine.activities.test.DashboardTest;

public class CompleteTestSuite extends InstrumentationTestRunner {

	@Override
	public TestSuite getAllTests() {
		InstrumentationTestSuite testSuite = new InstrumentationTestSuite(this);
		
		testSuite.addTestSuite(DashboardTest.class);
		return testSuite;
	}
	
	@Override
	public ClassLoader getLoader() {
		return CompleteTestSuite.class.getClassLoader();
	}
	
}
