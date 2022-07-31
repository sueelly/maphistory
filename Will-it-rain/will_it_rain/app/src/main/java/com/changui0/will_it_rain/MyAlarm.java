package com.changui0.will_it_rain;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Calendar;
import java.util.StringTokenizer;

public class MyAlarm extends BroadcastReceiver {
    public static final String fileName = "alarmTime.txt";
    public static int hour = -1, min = -1;
    private Context context;
    private final String channelId = "alarm_channel"; // don't change
    private final String channelName = "Probability of Rainfall"; // don't change
    private final String actionName = "alarm_action";
    private final int requestCode = 5;
    private static int times;
    private AlarmManager alarmManager;
    private final long alarmInterval = AlarmManager.INTERVAL_DAY;

    public MyAlarm() {
    }

    public MyAlarm(Context context) {
        this.context = context;
        alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("Alarm", "@@@@@@ Alram begin @@@@@");
        long now = System.currentTimeMillis();

        // set next alarm
        MyAlarm myAlarm = new MyAlarm(context);
        if (!MyAlarm.isTimeValid())
            myAlarm.readTime();
        myAlarm.readTime();
        myAlarm.setAlarm(MyAlarm.hour, MyAlarm.min);

        // get valid x, y
        MyGps myGps = new MyGps(context);
        if (!MyGps.isXyValid())
            myGps.readXy();

        // thread (api)
        MyBackgroundThread th = new MyBackgroundThread();
        th.setContext(context);
        th.setNow(now);
        th.start();
        try {
            th.join(10 * 60 * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // notification generate
        MyNotification myNoti = new MyNotification(context, (int) now);
        myNoti.makeNotification("Will it rain?", th.resultStr);
        Log.d("Alarm", "@@@@@@ Alram end @@@@@");
    }

    public void setAlarm(int hour, int minute) {
        this.hour = hour;
        this.min = minute;
        if (alarmManager == null)
            return;
        Calendar calendar = Calendar.getInstance();
        long now = System.currentTimeMillis();
        calendar.setTimeInMillis(now);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);

        long next = calendar.getTimeInMillis();
        while (next <= now)
            next += alarmInterval;

        PendingIntent alarmIntent = getAlamPIntent(requestCode, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager.AlarmClockInfo info = new AlarmManager.AlarmClockInfo(next, alarmIntent);
        alarmManager.setAlarmClock(info, alarmIntent);
    }

    public void cancelAlarm() {
        if (alarmManager == null)
            return;
        PendingIntent pi = getAlamPIntent(requestCode, PendingIntent.FLAG_UPDATE_CURRENT);
        if (pi != null) {
            alarmManager.cancel(pi);
            pi.cancel();
            Toast.makeText(context, "Alarm is canceled", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "There's no alarm to be canceled", Toast.LENGTH_SHORT).show();
        }

    }

    public PendingIntent getAlamPIntent(int reqCode, int flags) {
        Intent intent = new Intent(context, MyAlarm.class);
        intent.setAction(actionName);
        PendingIntent ret = PendingIntent.getBroadcast(context.getApplicationContext(), reqCode, intent, flags);
        return ret;
    }

    public static boolean isTimeValid() {
        if (0 <= hour && hour <= 23 && 0 <= min && min <= 59)
            return true;
        return false;
    }

    public static void setTimeInvalid() {
        hour = min = -1;
    }

    public void writeTime() {
        try {
            OutputStreamWriter oStreamWriter = new OutputStreamWriter(
                    context.openFileOutput(fileName, Context.MODE_PRIVATE));
            oStreamWriter.write(String.format("%d %d", hour, min));
            oStreamWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void readTime() {
        try {
            InputStream iStream = context.openFileInput(fileName);
            if (iStream != null) {
                InputStreamReader iStreamReader = new InputStreamReader(iStream);
                BufferedReader bufferedReader = new BufferedReader(iStreamReader);
                StringTokenizer st = new StringTokenizer(bufferedReader.readLine());
                hour = Integer.parseInt(st.nextToken());
                min = Integer.parseInt(st.nextToken());
                iStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
