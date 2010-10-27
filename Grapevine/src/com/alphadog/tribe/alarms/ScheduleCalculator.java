package com.alphadog.tribe.alarms;


public class ScheduleCalculator {
	
	public static String scheduleForImageUploader() {
		//Four times a day
		return "21600000";
	}

	public static String scheduleForReviewCleanup() {
		//Once a day
		return "86400000";
	}
}
