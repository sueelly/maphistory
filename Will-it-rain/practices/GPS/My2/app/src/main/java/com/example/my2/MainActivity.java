package com.example.my2;

import androidx.appcompat.app.AppCompatActivity;

import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    TextView tv;
    Button tb;
    LocationManager lm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tv = (TextView) findViewById(R.id.textView2);
        tb = (Button) findViewById(R.id.toggle1);

        MyGps gps = new MyGps(this);
        gps.readXy();

        tv.setText(String.format("Read: (%d, %d)", MyGps.x, MyGps.y));
        tb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyGps gps = new MyGps(MainActivity.this);
                gps.enableGps();
            }
        });
    }

}