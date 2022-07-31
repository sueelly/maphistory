package com.example.notification;

import android.content.Context;

public class MyBackgroundThread extends Thread {
    private Context context;

    public void setContext(Context context) {
        this.context = context;
    }

    @Override
    public void run() {
        long now = System.currentTimeMillis();
        Weather wth = new Weather();
        int pop = wth.isGoingToRain(24, 61, 127, now);
        System.out.println("@@@@@@@ pop: " + pop);

        String str = wth.makeNotificatoinText(24, pop);
        MyNotification myNoti = new MyNotification(context, (int) now);

        // notification generate
        myNoti.makeNotification("Will it rain?", str);
    }
}
