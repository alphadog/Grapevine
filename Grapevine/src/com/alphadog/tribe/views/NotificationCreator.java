package com.alphadog.tribe.views;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.alphadog.tribe.activities.ReviewListingActivity;

public class NotificationCreator {
    
    private Context context;
    
    public NotificationCreator(Context context) {
        this.context = context;
    }

    private NotificationManager notificationManager() {
        return (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public void create(int id, String summary, String title, String text, long when, boolean vibrate, int icon) {
        Intent intent = new Intent(context, ReviewListingActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        Notification notification = new Notification(icon, summary, when);
        notification.setLatestEventInfo(context, title, text, pendingIntent);

        if (vibrate) {
            long[] v = { 0, 100, 200, 300 };
            notification.vibrate = v;
        }

        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        notificationManager().notify(id, notification);
    }
    
    public void cancel(int id) {
        notificationManager().cancel(id);
    }
}