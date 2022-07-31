package com.example.notification;

import androidx.core.app.NotificationCompat;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.os.Build;

public class MyNotification {
    private final Context parent;
    public final String channelId = "pop channel"; // don't change
    private final String channelName = "Rainfall"; // don't change
    private NotificationManager noti = null;
    private int notiID = 1;
    public static final int DefaultNotiID = 1;

    public MyNotification(Context parent, int notiID) {
        this.parent = parent;
        this.notiID = notiID;
        noti = (NotificationManager) parent.getSystemService(parent.NOTIFICATION_SERVICE);
        createChannel();
    }

    public void createChannel() {
        NotificationChannel channel = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                channel = noti.getNotificationChannel(channelId);
            } catch (Exception e) {
                channel = null;
            }
            if (channel == null)
                noti.createNotificationChannel(new NotificationChannel(channelId, channelName, noti.IMPORTANCE_HIGH));
        }
    }

    public void makeNotification(String title, String text) {

        Intent busRouteIntent = new Intent(parent, MainActivity.class);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(parent);
        stackBuilder.addNextIntentWithParentStack(busRouteIntent);
        PendingIntent busRoutePendingIntent =
                stackBuilder.getPendingIntent(1, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(parent, channelId);
        builder.setContentTitle(title).setContentText(text);
        builder.setSmallIcon(R.drawable.ic_launcher_foreground);
        builder.setDefaults(Notification.DEFAULT_ALL);
        builder.setContentIntent(busRoutePendingIntent);
        builder.setAutoCancel(true);
        builder.setPriority(NotificationCompat.PRIORITY_HIGH);
        builder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
        noti.notify(notiID, builder.build());
    }

    public void destroyNotification() {
        noti.cancel(notiID);
    }

}
