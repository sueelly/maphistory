package com.changui0.will_it_rain;

import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

public class AlarmActivity extends AppCompatActivity {
    MainActivity mainActivity;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        mainActivity = MainActivity.mainActivity;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub);
        Button btn = (Button) findViewById(R.id.timeSave);
        TimePicker timePicker = (TimePicker) findViewById(R.id.time_picker);

        MyAlarm myAlarm = new MyAlarm(mainActivity);
        myAlarm.readTime();
        if (0 <= MyAlarm.hour && MyAlarm.hour <= 23 && 0 <= MyAlarm.min && MyAlarm.min <= 59) {
            timePicker.setHour(MyAlarm.hour);
            timePicker.setMinute(MyAlarm.min);
        }


        btn.setOnClickListener(v -> {
            int hour = timePicker.getHour(), min = timePicker.getMinute();
            myAlarm.setAlarm(hour, min);
            String str = String.format("알림이 %d시 %d분에 저장되었습니다.", hour, min);
            Toast.makeText(this, str, Toast.LENGTH_LONG).show();
            myAlarm.writeTime();
        });
        btn = (Button) findViewById(R.id.timeCancel);
        btn.setOnClickListener(v -> {
            myAlarm.cancelAlarm();
            myAlarm.setTimeInvalid();
            myAlarm.writeTime();
        });
    }
}
