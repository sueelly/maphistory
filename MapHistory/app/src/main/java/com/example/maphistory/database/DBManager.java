package com.example.maphistory.database;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import com.example.maphistory.Note;

import java.util.ArrayList;

public class DBManager extends SQLiteOpenHelper {
    static final String DATABASE_NAME = "DiaryTest.db";

    // DBHelper 생성자
    public DBManager(Context context, int version) {
        super(context, DATABASE_NAME, null, version);
    }

    public ArrayList<Note> loadNoteList() {
        ArrayList<Note> items = new ArrayList<>();

        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM TABLE_NAME", null);

        if(cursor!=null && cursor.getCount() > 0)
        {
            if (cursor.moveToFirst())
            {
                do {
                    Note item = new Note();
                    item._id = cursor.getInt(0);
                    item.titleOfDiary = cursor.getString(1);
                    item.createDateStr = cursor.getString(2);
                    item.address = cursor.getString(3);
                    item.locationX = cursor.getString(4);
                    item.locationY = cursor.getString(5);
                    item.picture = cursor.getString(6);
                    item.contents = cursor.getString(7);

                    items.add(item);

                } while (cursor.moveToNext());
            }
        }
        return items;
    }


    // Person Table 생성
//    @Override
//    public void onCreate(SQLiteDatabase db) {
//        db.execSQL("CREATE TABLE TABLE_NAME(name TEXT, Age INT, ADDR TEXT)");
//    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE TABLE_NAME( _id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, title TEXT, date TEXT, address TEXT, locationX TEXT, locationY TEXT, picture TEXT, contents TEXT)");
    }

    // Person Table Upgrade
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS TABLE_NAME");
        onCreate(db);
    }

    // Person Table 데이터 입력
    public void insert(String title, String date, String address, String locationX, String locationY, String picture, String contents) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("INSERT INTO TABLE_NAME (title, date, address, locationX, locationY, picture, contents) VALUES(" +
                "'"+ title + "', " +
                "'"+ date + "', " +
                "'"+ address + "', " +
                "'"+ " " + "', " +
                "'"+ " " + "', " +
                "'"+ picture + "', " +
                "'"+ contents + "')");
//        db.close();
    }

//    // Person Table 데이터 수정
//    public void Update(String name, int age, String Addr) {
//        SQLiteDatabase db = getWritableDatabase();
//        db.execSQL("UPDATE TABLE_NAME SET age = " + age + ", ADDR = '" + Addr + "'" + " WHERE NAME = '" + name + "'");
//        db.close();
//    }
//

    public void modifyNote(Note item) {
        if (item != null) {
            // update note
            SQLiteDatabase db = getWritableDatabase();
            String sql = "update " + "TABLE_NAME" +
                    " set " +
                    "   title = '" + item.titleOfDiary + "'" +
                    "   ,date = '" + item.createDateStr + "'" +
                    "   ,address = '" + item.address + "'" +
                    "   ,locationX = '" + "" + "'" +
                    "   ,locationY = '" + "" + "'" +
                    "   ,picture = '" + "" + "'" +
                    "   ,contents = '" + item.contents + "'" +
                    " where " +
                    "   _id = " + item._id;

            db.execSQL(sql);
        }
    }

    public void deleteNote(Note item) {

         if (item != null) {
             SQLiteDatabase db = getWritableDatabase();

             String sql = "delete from " + "TABLE_NAME" +
                     " where " +
                     "   _id = " + item._id;
             db.execSQL(sql);

    }
}

    // Person Table 조회
    public String getResult() {
        // 읽기가 가능하게 DB 열기
        SQLiteDatabase db = getReadableDatabase();
        String result = "";

        // DB에 있는 데이터를 쉽게 처리하기 위해 Cursor를 사용하여 테이블에 있는 모든 데이터 출력
        Cursor cursor = db.rawQuery("SELECT * FROM TABLE_NAME", null);
        while (cursor.moveToNext()) {
            result += " id: " + cursor.getInt(0)
                    + ", 제목: "
                    + cursor.getString(1)
                    + ", 날짜: "
                    + cursor.getString(2)
                    + ", 주소 : "
                    + cursor.getString(3)
                    + ", X값: "
                    + cursor.getString(4)
                    + ", Y값: "
                    + cursor.getString(5)
                    + ", 사진: "
                    + cursor.getString(6)
                    + ", 내용: "
                    + cursor.getString(7)
                    + "\n";
        }
        return result;
    }

}
