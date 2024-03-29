package com.example.maphistory;

import android.os.Build;

import androidx.annotation.RequiresApi;

import java.text.SimpleDateFormat;

@RequiresApi(api = Build.VERSION_CODES.N)
public class AppConstants {

    public static final int REQ_LOCATION_BY_ADDRESS = 101;
    public static final int REQ_WEATHER_BY_GRID = 102;
    public static final int REQ_PHOTO_CAPTURE = 103;
    public static final int REQ_PHOTO_SELECTION = 104;
    public static final int CONTENT_PHOTO = 105;
    public static final int CONTENT_PHOTO_EX = 106;

    // 저장:1 , 수정: 2
    public static int SAVE_MODIFY = 1;
    public static double X;
    public static double Y;

    public static String FOLDER_PHOTO="";

    public static final String KEY_URI_PHOTO = "URI_PHOTO";
    public static String DATABASE_NAME = "note.db";

    public static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmm");
    public static SimpleDateFormat dateFormat2 = new SimpleDateFormat("yyyy-MM-dd HH시");
    public static SimpleDateFormat dateFormat3 = new SimpleDateFormat("MM월 dd일");
    public static SimpleDateFormat dateFormat4 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public static SimpleDateFormat dateFormat5 = new SimpleDateFormat("yyyyMMdd");

}
