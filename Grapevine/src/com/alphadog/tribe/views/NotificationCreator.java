package com.alphadog.tribe.views;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.alphadog.tribe.R;
import com.alphadog.tribe.activities.ReviewListingActivity;


public class NotificationCreator {
	
	private Context notificationContext;
	private NotificationManager notificationManager; 
	private PendingIntent notificationIntent;
	
	public NotificationCreator(Context context) {
		this.notificationContext= context;
		this.notificationManager = (NotificationManager) notificationContext.getSystemService(Context.NOTIFICATION_SERVICE);
	}

	public void createNotification(int id, String summary, String title, String text, long when, boolean isVibrate, int icon) {
		int notificationIcon = icon == 0 ? R.drawable.alert : icon;
		Intent notificationIntent = new Intent(notificationContext, ReviewListingActivity.class);
		this.notificationIntent = PendingIntent.getActivity(notificationContext, 0, notificationIntent, 0);
		Notification notification = new Notification(notificationIcon, summary, when);
		notification.setLatestEventInfo(notificationContext, title, text, this.notificationIntent);

		if (isVibrate) {
			long[] vibrate = {0,100,200,300};
			notification.vibrate = vibrate;
		}
		
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		notificationManager.notify(id, notification);
	}	
	
	public void cancelAllNotifications() {
		notificationManager.cancelAll(); 
	}

	public void cancelNotificationWithId(int notificationId) {
		notificationManager.cancel(notificationId); 
	}
}
