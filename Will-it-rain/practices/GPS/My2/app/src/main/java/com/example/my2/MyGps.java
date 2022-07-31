package com.example.my2;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;

import java.io.*;
import java.util.StringTokenizer;

public class MyGps {
    public static final String fileName = "xy.txt";
    public static int x = -1, y = -1;
    private final LocationManager lm;
    private final MainActivity mainActivity;

    public MyGps(MainActivity m) {
        mainActivity = m;
        this.lm = (LocationManager) m.getSystemService(Context.LOCATION_SERVICE);
    }

    public void enableGps() {
        try {
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 1,
                    mLocationListener);

        } catch (SecurityException ex) {
        }

    }

    private final LocationListener mLocationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            double longitude = location.getLongitude(); //경도
            double latitude = location.getLatitude();   //위도
            LlXyConverter.init();
            Point2D point = LlXyConverter.LonLat2xy(longitude, latitude);
            x = (int) point.x;
            y = (int) point.y;
            writeXy();

            mainActivity.tv.setText(String.format("GPS: (%d, %d)", x, y));
            lm.removeUpdates(mLocationListener);  //  미수신할때는 반드시 자원해체를 해주어야 한다
        }
    };

    private void writeXy() {
        try {
            OutputStreamWriter oStreamWriter = new OutputStreamWriter(mainActivity.openFileOutput(fileName, Context.MODE_PRIVATE));
            oStreamWriter.write(String.format("%d %d", x, y));
            oStreamWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void readXy() {
        try {
            InputStream iStream = mainActivity.openFileInput(fileName);
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


}
