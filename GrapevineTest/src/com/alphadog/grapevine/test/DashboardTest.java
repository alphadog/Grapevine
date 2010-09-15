package com.alphadog.grapevine.test;

import android.test.ActivityInstrumentationTestCase2;

import com.alphadog.grapevine.Dashboard;
import com.jayway.android.robotium.solo.Solo;

public class DashboardTest extends ActivityInstrumentationTestCase2<Dashboard> {

	private Solo solo;

	public DashboardTest() {
		super("com.alphadog.grapevine", Dashboard.class);
	}

	public void setUp() throws Exception {
		solo = new Solo(getInstrumentation(), getActivity());
	}
	
	public void testDashboardPageOpensAsExpected() {
		boolean textFound = solo.searchText("Dashboard");
		assertEquals(true, textFound);
	}
	
	public void tearDown() throws Exception {
		try {
			solo.finalize();
		} catch(Throwable e) {
			e.printStackTrace();
		}
		getActivity().finish();
		super.tearDown();
	}

}
