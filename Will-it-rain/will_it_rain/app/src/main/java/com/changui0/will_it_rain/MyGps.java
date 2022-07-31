package com.changui0.will_it_rain;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.StringTokenizer;

public class MyGps {
    public static final String fileName = "xy.txt";
    public static int x = -1, y = -1;
    private final LocationManager lm;
    private final Context context;

    public MyGps(Context context) {
        this.context = context;
        this.lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
    }

    public void enableGps() {
        try {
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 1,
                    mLocationListener);
            Log.d("GPS", "enable the location update");

        } catch (SecurityException ex) {
        }
    }

    private final LocationListener mLocationListener = new LocationListener() {
        public void onLocationChanged(@NonNull Location location) {
            setXyFromLocation(location);
            lm.removeUpdates(mLocationListener);  //  미수신할때는 반드시 자원해체를 해주어야 한다
        }
    };

    public void setXyFromLocation(Location location) {

        double longitude = location.getLongitude(); //경도
        double latitude = location.getLatitude();   //위도
        LlXyConverter.init();
        Point2D point = LlXyConverter.LonLat2xy(longitude, latitude);
        x = (int) point.x;
        y = (int) point.y;
        writeXy();
        Log.d("GPS", String.format("(x,y) is (%d, %d)", x, y));
        Toast.makeText(context, "Location set!", Toast.LENGTH_LONG).show();
    }

    private void writeXy() {
        try {
            OutputStreamWriter oStreamWriter = new OutputStreamWriter(context.openFileOutput(fileName, Context.MODE_PRIVATE));
            oStreamWriter.write(String.format("%d %d", x, y));
            oStreamWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void readXy() {
        try {
            InputStream iStream = context.openFileInput(fileName);
            if (iStream != null) {
                InputStreamReader iStreamReader = new InputStreamReader(iStream);
                BufferedReader bufferedReader = new BufferedReader(iStreamReader);
                StringTokenizer st = new StringTokenizer(bufferedReader.readLine());
                x = Integer.parseInt(st.nextToken());
                y = Integer.parseInt(st.nextToken());
                iStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean isXyValid() {
        return LlXyConverter.isXyValid(new Point2D(x, y));
    }

    public static void setXyInvalid() {
        x = y = -1;
    }

}
