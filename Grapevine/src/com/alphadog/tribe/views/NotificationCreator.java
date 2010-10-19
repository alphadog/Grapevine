package com.alphadog.tribe.views;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.alphadog.tribe.R;
import com.alphadog.tribe.activities.ReviewListingActivity;

public class NotificationCreator {

    public static void create(Context notificationContext, int notificationIcon, int id, String summary, String title, String text, long when, boolean isVibrate) {
        Intent notificationIntent = new Intent(notificationContext, ReviewListingActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(notificationContext, 0, notificationIntent, 0);
        NotificationManager notificationManager = (NotificationManager) notificationContext
                .getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = new Notification(notificationIcon, summary, when);
        notification.setLatestEventInfo(notificationContext, title, text, pendingIntent);

        if (isVibrate) {
            long[] vibrate = { 0, 100, 200, 300 };
            notification.vibrate = vibrate;
        }

        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        notificationManager.notify(id, notification);
    }
}
