package com.changui0.will_it_rain;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

public class Weather extends Thread {

    private final String endPoint = "http://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getVilageFcst?";
    private final String serviceKey = "1Y66bkrYDIVc22RQE5oIxSR7KAViZKtP4JGd21BwS31M4cYJT%2BC%2B%2F69m0AHecwDar5bZrkYmMePhuiA3Qcay3A%3D%3D";

    public enum ERROR_CODE {
        ERROR(-1),
        NOT_POP(-2),
        MISSNG1(-900),
        MISSING2(900),
        INVALID_XY(-3),
        URL_NULL(-4),
        COMMUNICATION(-5),
        API_OPEN(-6),
        API_READ(-7),
        API_CLOSE(-8),
        API_JSON(-9),
        INVALID_ERROR_CODE(Integer.MAX_VALUE);
        private int i;

        ERROR_CODE(int i) {
            this.i = i;
        }

        public int getVal() {
            return i;
        }

        public static ERROR_CODE getCode(int val) {
            for (ERROR_CODE e : ERROR_CODE.values())
                if (e.getVal() == val)
                    return e;
            return ERROR_CODE.INVALID_ERROR_CODE;
        }
    }

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
            baseTime = String.format("%02d", newHour) + "00";
            baseDate = dateFormat.format(date);
        }

        private int getBaseTime(int hour24) {
            if (hour24 % 3 == 2)
                return hour24; // 2, 5, 8, 11, 14, 17, 20, 23
            else if (hour24 < 2)
                return 23; // 0,1 => 23
            else
                return hour24 - hour24 % 3 - 1; // else
        }
    }

    // use this function to get the max of pop 0~100 (integer)
    public int isGoingToRain(int duration, int x, int y, long now) {
        ret = -1000;
        ApiBaseTime bt = new ApiBaseTime(now);
        calculatePops(bt, duration, x, y);
        return ret;
    }

    public static boolean isPopValid(int pop) {
        if (pop < 0 || 100 < pop)
            return false;
        return true;
    }

    // use this function to get the string to notify
    public String makeNotificatoinText(int duration, int maxPop) {
        String str = "Error : ";
        if (!isPopValid(maxPop))
            return str + ERROR_CODE.getCode(maxPop);
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
        // Log.d("Api URL", sb.toString());
        return url;
    }

    private int getPop(ApiBaseTime bt, int page, int x, int y) {
        URL url = makeURL(page, 1, bt.baseDate, bt.baseTime, x, y);
        if (url == null)
            return ERROR_CODE.URL_NULL.getVal();

        HttpURLConnection conn;
        BufferedReader bufferedReader;
        StringBuilder stringBuilder = new StringBuilder();
        try {
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("Api Error Url", url.toString());
            return ERROR_CODE.API_OPEN.getVal();
        }
        try {
            if (conn.getResponseCode() == 200) {
                bufferedReader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            } else
                return ERROR_CODE.COMMUNICATION.getVal();

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }

            bufferedReader.close();
        } catch (IOException e) {
            e.printStackTrace();
            Log.d("Api Read Error", stringBuilder.toString());
            return ERROR_CODE.API_READ.getVal();
        }
        try {
            conn.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
            return ERROR_CODE.API_CLOSE.getVal();
        }

        try {
            JSONObject mainObject = new JSONObject(stringBuilder.toString());
            JSONArray itemArray = mainObject.getJSONObject("response").getJSONObject("body").getJSONObject("items")
                    .getJSONArray("item");
            JSONObject item = itemArray.getJSONObject(0);
            if (item.getString("category").compareTo("POP") != 0)
                return ERROR_CODE.NOT_POP.getVal();
            return Integer.parseInt(item.getString("fcstValue"));
        } catch (JSONException e) {
            e.printStackTrace();
            Log.d("Api Json Error", stringBuilder.toString());
            return ERROR_CODE.API_JSON.getVal();
        }

    }

    private int ret; // used in getPop() and isGoingToRain()

    private void calculatePops(ApiBaseTime bt, int duration, int x, int y) {
        int[] pops = new int[duration];
        int offset = 8;
        for (int i = 0; i < duration; i++) {
            pops[i] = getPop(bt, ROWS_PER_HOUR * i + offset, x, y);
            if (pops[i] == ERROR_CODE.NOT_POP.getVal()) {
                offset++;
                i--;
            }
            // else: do nothing
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