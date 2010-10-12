package com.alphadog.tribe.services.test;

import android.content.Intent;
import android.test.ServiceTestCase;

import com.alphadog.tribe.services.WidgetUpdateService;

public class WidgetUpdateServiceTest extends ServiceTestCase<WidgetUpdateService> {

	public WidgetUpdateServiceTest() {
		super(WidgetUpdateService.class);
	}
	
	@Override
	public void setUp() throws Exception {
		super.setUp();
	}
	
	public void testStartable() {
		Intent startIntent = new Intent();
		startIntent.setClass(getContext(), WidgetUpdateService.class);
		startService(startIntent);
	}
	
}
