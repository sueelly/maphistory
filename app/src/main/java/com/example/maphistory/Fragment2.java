package com.example.maphistory;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.example.maphistory.database.DBManager;
import com.google.android.material.tabs.TabLayout;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class Fragment2 extends Fragment {

    RecyclerView recyclerView;
    NoteAdapter adapter;
    Context context;
    Button writeNewDiary;
    TabLayout.OnTabSelectedListener listener;
    SimpleDateFormat todayDateFormat;
    Fragment1 fragmentNew;


    public void onAttach(Context context) {

        super.onAttach(context);
        this.context = context;

        if(context instanceof TabLayout.OnTabSelectedListener) {
            listener = (TabLayout.OnTabSelectedListener) context;
        }
    }

    public void onDetach() {

        super.onDetach();
        if(context != null) {
            context = null;
            listener = null;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_2, container, false);

        recyclerView = rootView.findViewById(R.id.recyclerView);
        writeNewDiary = rootView.findViewById(R.id.writeNewDiary);
        writeNewDiary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment1 fragment1 = new Fragment1();
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, fragment1).commit();
            }
        });

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        adapter = new NoteAdapter(context);
        adapter.addItem(new Note(0, "제목1", "7월 1일", "주소", "37.56", "126.97",
                "String picture" , "즐거운 하루"
        ));
        adapter.addItem(new Note(1, "제목1", "7월 1일", "주소", "37.60", "126.97",
                "String picture" , "즐거운 하루"));
        adapter.addItem(new Note(2, "제목1", "7월 1일", "주소", "37.56", "126.100",
                "String picture" , "즐거운 하루"));



        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(new OnNoteItemClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onItemClick(NoteAdapter.ViewHolder holder, View view, int position) {
                Note item = adapter.getItem(position);

                Toast.makeText(getContext(), "선택: " +item.getContents(), Toast.LENGTH_SHORT).show();
                fragmentNew = new Fragment1();
                fragmentNew.setItem(item);


                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, fragmentNew).commit();

            }
        });

//        try {
////            loadNoteListData();
//        } catch (ParseException e) {
//            e.printStackTrace();
//        }
        return rootView;
    }

//    public int loadNoteListData() {
//
//        String sql = "select _id, WEATHER, ADDRESS, LOCATION_X, LOCATION_Y, CONTENTS, MOOD, PICTURE, CREATE_DATE, MODIFY_DATE from " + NoteDatabase.TABLE_NOTE + " order by CREATE_DATE desc";
//
//        int recordCount = -1;
//        NoteDatabase database = NoteDatabase.getInstance(context);
//        if (database != null) {
//            Cursor outCursor = database.rawQuery(sql);
//
//            recordCount = outCursor.getCount();
//            AppConstants.println("record count : " + recordCount + "\n");
//
//            ArrayList<Note> items = new ArrayList<Note>();
//
//            for (int i = 0; i < recordCount; i++) {
//                outCursor.moveToNext();
//
//                int _id = outCursor.getInt(0);
//                String weather = outCursor.getString(1);
//                String address = outCursor.getString(2);
//                String locationX = outCursor.getString(3);
//                String locationY = outCursor.getString(4);
//                String contents = outCursor.getString(5);
//                String mood = outCursor.getString(6);
//                String picture = outCursor.getString(7);
//                String dateStr = outCursor.getString(8);
//                String createDateStr = null;
//                if (dateStr != null && dateStr.length() > 10) {
//                    try {
//                        Date inDate = AppConstants.dateFormat4.parse(dateStr);
//
//                        if (todayDateFormat == null) {
//                            todayDateFormat = new SimpleDateFormat(getResources().getString(R.string.today_date_format));
//                        }
//                        createDateStr = todayDateFormat.format(inDate);
//                        AppConstants.println("currentDateString : " + createDateStr);
//                        //createDateStr = AppConstants.dateFormat3.format(inDate);
//                    } catch(Exception e) {
//                        e.printStackTrace();
//                    }
//                } else {
//                    createDateStr = "";
//                }
//
//                AppConstants.println("#" + i + " -> " + _id + ", " + weather + ", " +
//                        address + ", " + locationX + ", " + locationY + ", " + contents + ", " +
//                        mood + ", " + picture + ", " + createDateStr);
//
//                items.add(new Note(_id, weather, address, locationX, locationY, contents, mood, picture, createDateStr));
//            }
//
//            outCursor.close();
//
//            adapter.setItems(items);
//            adapter.notifyDataSetChanged();
//
//        }
//
//        return recordCount;
//    }




}