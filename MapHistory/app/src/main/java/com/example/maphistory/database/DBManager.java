package com.example.maphistory.database;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import com.example.maphistory.Note;

import java.util.ArrayList;

public class DBManager extends SQLiteOpenHelper {
    static final String DATABASE_NAME = "test.db";

    // DBHelper 생성자
    public DBManager(Context context, int version) {
        super(context, DATABASE_NAME, null, version);
    }

    public ArrayList<Note> loadNoteList() {
        ArrayList<Note> items = new ArrayList<>();

        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM Person", null);
        while (cursor.moveToNext()) {
            Note item = new Note();
            item.titleOfDiary = cursor.getString(1);
            item.contents = cursor.getString(2);
            item.address = cursor.getString(3);

            items.add(item);

        }

        return items;
    }



    // Person Table 생성
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE Person(name TEXT, Age INT, ADDR TEXT)");
    }

    // Person Table Upgrade
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS Person");
        onCreate(db);
    }

    // Person Table 데이터 입력
    public void insert(String name, int age, String Addr) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("INSERT INTO Person VALUES('" + name + "', " + age + ", '" + Addr + "')");
//        db.close();
    }

    // Person Table 데이터 수정
    public void Update(String name, int age, String Addr) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("UPDATE Person SET age = " + age + ", ADDR = '" + Addr + "'" + " WHERE NAME = '" + name + "'");
        db.close();
    }

    // Person Table 데이터 삭제
    public void Delete(String name) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DELETE Person WHERE NAME = '" + name + "'");
        db.close();
    }

    // Person Table 조회
    public String getResult() {
        // 읽기가 가능하게 DB 열기
        SQLiteDatabase db = getReadableDatabase();
        String result = "";

        // DB에 있는 데이터를 쉽게 처리하기 위해 Cursor를 사용하여 테이블에 있는 모든 데이터 출력
        Cursor cursor = db.rawQuery("SELECT * FROM Person", null);
        while (cursor.moveToNext()) {
            result += " 이름 : " + cursor.getString(0)
                    + ", 나이 : "
                    + cursor.getInt(1)
                    + ", 주소 : "
                    + cursor.getString(2)
                    + "\n";
        }

        return result;
    }



}
