package com.changui0.will_it_rain;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.widget.Button;

import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

public class MainActivity extends AppCompatActivity {
    Intent intent;
    public static MainActivity mainActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mainActivity = this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        OnCheckPermission();
        Button btn = (Button) findViewById(R.id.gotoTimePicker);
        intent = new Intent(MainActivity.this, AlarmActivity.class);


        btn.setOnClickListener(v -> {
            {
                startActivity(intent);
            }
        });

        Button btn1 = (Button) findViewById(R.id.getPop);
        btn1.setOnClickListener(v -> {
            long now = System.currentTimeMillis();

            MyGps myGps = new MyGps(MainActivity.this);
            if (!MyGps.isXyValid())
                myGps.readXy();

            MyBackgroundThread th = new MyBackgroundThread();
            th.setContext(MainActivity.this);
            th.setNow(now);
            th.start();
            try {
                th.join(10 * 60 * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            Toast.makeText(MainActivity.this, th.resultStr, Toast.LENGTH_LONG).show();
        });


        MyGps gps = new MyGps(this);
        gps.readXy();
        if (MyGps.isXyValid()) {
            Toast.makeText(this, "사용자의 위치가 정상 작동합니다.", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "내 위치 가져오기를 실행해야 합니다.", Toast.LENGTH_LONG).show();
        }
        btn = (Button) findViewById(R.id.getLoc);
        btn.setOnClickListener(v -> {
            FusedLocationProviderClient flc = LocationServices.getFusedLocationProviderClient(MainActivity.this);
            if (ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            } else {
                flc.getLastLocation().addOnSuccessListener(MainActivity.this, location -> {
                    MyGps myGps = new MyGps(MainActivity.this);
                    if (location != null)
                        myGps.setXyFromLocation(location);
                    else
                        myGps.enableGps();
                });
            }

        });
    }

    final int PERMISSIONS_REQUEST = 1;

    public void OnCheckPermission() {
        int ok = PackageManager.PERMISSION_GRANTED;
        String[] permisson = new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION};
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != ok ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != ok) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                Toast.makeText(this, "앱 실행을 위해서는 권한을 설정해야 합니다", Toast.LENGTH_LONG).show();
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION},
                        PERMISSIONS_REQUEST);
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION},
                        PERMISSIONS_REQUEST);
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.SCHEDULE_EXACT_ALARM) != ok) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SCHEDULE_EXACT_ALARM},
                        PERMISSIONS_REQUEST);

            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            String packageName = getPackageName();
            if (pm.isIgnoringBatteryOptimizations(packageName)) {

            } else { // 메모리 최적화가 되어 있다면, 풀기 위해 설정 화면 띄움.
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData(Uri.parse("package:" + packageName));
                startActivityForResult(intent, 0);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grandResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grandResults);

        switch (requestCode) {
            case PERMISSIONS_REQUEST:
                if (grandResults.length > 0 && grandResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "앱 실행을 위한 권한이 설정 되었습니다", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, "앱 실행을 위한 권한이 취소 되었습니다", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }
}