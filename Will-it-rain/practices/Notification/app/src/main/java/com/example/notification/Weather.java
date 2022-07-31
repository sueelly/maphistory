package com.example.notification;


import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Weather extends Thread {

    private final String endPoint = "http://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getVilageFcst?";
    private final String serviceKey = "1Y66bkrYDIVc22RQE5oIxSR7KAViZKtP4JGd21BwS31M4cYJT%2BC%2B%2F69m0AHecwDar5bZrkYmMePhuiA3Qcay3A%3D%3D";
    private final int ERROR = -1, NOT_POP = -2, MISSING1 = -900, MISSING2 = 900;// POP: probability of precipitation (rainfall)
    private final int ROWS_PER_HOUR = 12;

    private class ApiBaseTime { // a helper class for using api base time
        public String baseDate, baseTime; // YOU CAN USE THESE!!
        private long now;
        private final long TEM_MIN_MS = 10L * 60L * 1000L; // spare time
        private final long ONE_DAY_MS = 24L * 60L * 60L * 1000L; // for calculating yesterday
        private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH");
        private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");

        public ApiBaseTime() { // get now from system
            now = System.currentTimeMillis() - TEM_MIN_MS;
            setBase();
        }

        public ApiBaseTime(long now) { // get specific moment
            this.now = now - TEM_MIN_MS;
            setBase();
        }

        private void setBase() {
            Date date = new Date(now);
            int hour = Integer.parseInt(timeFormat.format(date));
            int newHour = getBaseTime(hour);

            if (hour < newHour) { // hour:0~1 and baseHour:23
                date = new Date(now - ONE_DAY_MS); // use yesterday's data
            }
            baseTime = newHour + "00";
            baseDate = dateFormat.format(date);
        }

        private int getBaseTime(int hour24) {
            if (hour24 % 3 == 2) return hour24; // 2, 5, 8, 11, 14, 17, 20, 23
            else if (hour24 < 2) return 23; // 0,1 => 23
            else return hour24 - hour24 % 3 - 1; // else
        }
    }

    // use this function to get the max of pop 0~100 (integer)
    public int isGoingToRain(int duration, int x, int y, long now) {
        ret = -1000;
        ApiBaseTime bt = new ApiBaseTime(now);
        calculatePops(bt, duration, x, y);
        return ret;
    }

    public String makeNotificatoinText(int duration, int maxPop) {
        String str = "There's an error";
        if (maxPop < 0 || 100 < maxPop)
            return str;
        str = String.format("향후 %d시간 내의 최대 강수확률은 %d%%입니다.", duration, maxPop);
        return str;
    }

    private URL makeURL(int pageNo, int numRow, String date, String time, int x, int y) {
        // time should be 0200, 0500, 0800, 1100, 1400, 1700, 2000, 2300
        StringBuilder sb = new StringBuilder();
        sb.append(endPoint);
        sb.append("serviceKey=").append(serviceKey);
        sb.append("&pageNo=").append(pageNo);
        sb.append("&numOfRows=").append(numRow);
        sb.append("&dataType=").append("JSON");
        sb.append("&base_date=").append(date);
        sb.append("&base_time=").append(time);
        sb.append("&nx=").append(x);
        sb.append("&ny=").append(y);
        URL url;
        try {
            url = new URL(sb.toString());
        } catch (MalformedURLException e) {
            url = null;
        }
//        Log.d("Api URL", sb.toString());
        return url;
    }

    private int getPop(ApiBaseTime bt, int page, int x, int y) {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            URL url = makeURL(page, 1, bt.baseDate, bt.baseTime, x, y);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            BufferedReader bufferedReader;
            if (conn.getResponseCode() == 200) {
                bufferedReader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            } else
                return ERROR;
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }
            bufferedReader.close();
            conn.disconnect();

            JSONObject mainObject = new JSONObject(stringBuilder.toString());
            JSONArray itemArray = mainObject.getJSONObject("response").getJSONObject("body").getJSONObject("items").getJSONArray("item");
            JSONObject item = itemArray.getJSONObject(0);
            if (item.getString("category").compareTo("POP") != 0)
                return NOT_POP;
            return Integer.parseInt(item.getString("fcstValue"));

        } catch (Exception e) {
            Log.d("Api Error", stringBuilder.toString());
            return ERROR;
        }
    }

    private int ret; // used in getPop() and isGoingToRain()

    private void calculatePops(ApiBaseTime bt, int duration, int x, int y) {
        int[] pops = new int[duration];
        int offset = 8;
        try {
            for (int i = 0; i < duration; i++) {
                pops[i] = getPop(bt, ROWS_PER_HOUR * i + offset, x, y);
                if (pops[i] == ERROR) ;
                else if (pops[i] == MISSING1 || pops[i] == MISSING2) ;
                else if (pops[i] == NOT_POP) {
                    offset++;
                    i--;
                }
                // else: do nothing
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.d("Api Ret", arr2Str(pops));
        Arrays.sort(pops);
        ret = pops[pops.length - 1];
    }

    public String arr2Str(int[] arr) {
        StringBuilder sb = new StringBuilder();
        for (int val : arr)
            sb.append(val).append(' ');
        sb.append('\n');
        return sb.toString();
    }

}