package com.example.weather_api_test;

import androidx.core.app.NotificationCompat;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

public class MyNotification {
    private final MainActivity parent;
    private final String channelId = "normal pop noti"; // don't change
    private final String channelName = "Probability of Rainfall"; // don't change
    private NotificationManager noti = null;
    private int notiID = 1;
    public static final int DefaultNotiID = 1;

    public MyNotification(MainActivity parent, int notiID) {
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
        NotificationCompat.Builder builder = new NotificationCompat.Builder(parent, channelId);
        builder.setContentTitle(title).setContentText(text);
        builder.setSmallIcon(R.drawable.ic_launcher_foreground);
        builder.setDefaults(Notification.DEFAULT_ALL);
        noti.notify(notiID, builder.build());
    }

    public void destroyNotification() {
        noti.cancel(notiID);
    }

}
