package com.changui0.will_it_rain;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class MyBootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (!action.equals("android.intent.action.BOOT_COMPLETED"))
            return;

        MyAlarm myAlarm = new MyAlarm(context);
        myAlarm.readTime();
        myAlarm.setAlarm(MyAlarm.hour, MyAlarm.min);

    }
}
