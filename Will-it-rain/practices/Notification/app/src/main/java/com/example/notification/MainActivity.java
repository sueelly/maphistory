package com.example.notification;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TimePicker;

public class MainActivity extends AppCompatActivity {

    private Button save;
    private TimePicker timePicker;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MyAlarm myAlarm = new MyAlarm(this);
        myAlarm.readTime();
        timePicker = (TimePicker) findViewById(R.id.time_picker);
        save = (Button) findViewById(R.id.save);

        if (MyAlarm.isTimeValid()) {
            timePicker.setHour(MyAlarm.hour);
            timePicker.setMinute(MyAlarm.min);
        }
        save.setOnClickListener(v -> {
            int hour = timePicker.getHour(), min = timePicker.getMinute();
            myAlarm.setAlarm(hour, min);
            myAlarm.writeTime();
        });

        Button cancel = (Button) findViewById(R.id.cancel);
        cancel.setOnClickListener(v -> {
            myAlarm.cancelAlarm();
            myAlarm.setTimeInvalid();
            myAlarm.writeTime();
        });
    }


}