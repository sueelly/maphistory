package android.scroll.tlllllll;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.tabs.TabLayout;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class Fragment2 extends Fragment {

    RecyclerView recyclerView;
    NoteAdapter adapter;
    Context context;
    TabLayout.OnTabSelectedListener listener;


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
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        adapter = new NoteAdapter();
        adapter.addItem(new Note(0, "제목1", "7월 1일", "주소", "x", "y",
                "String picture" , "즐거운 하루"
    ));
        adapter.addItem(new Note(0, "제목1", "7월 1일", "주소", "x", "y",
                "String picture" , "즐거운 하루"));
        adapter.addItem(new Note(0, "제목1", "7월 1일", "주소", "x", "y",
                "String picture" , "즐거운 하루"));

        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(new OnNoteItemClickListener() {
            @Override
            public void onItemClick(NoteAdapter.ViewHolder holder, View view, int position) {
                Note item = adapter.getItem(position);
                Toast.makeText(getContext(), "아이템 선택됨" +item.getContents(), Toast.LENGTH_SHORT).show();
            }
        });

        try {
            loadNoteListData();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return rootView;

    }

    @SuppressLint("Range")
    public int loadNoteListData() throws ParseException {

        String sql = "select _id, ADDRESS, LOCATION_X, LOCATION_Y, CONTENTS, PICTURE, CREATE_DATE, MODIFY_DATE from "
                + NoteDatabase.TABLE_NOTE + " order by CREATE_DATE desc";

        int recordCount =-1;
        NoteDatabase database = NoteDatabase.getInstance( context);
        if(database != null) {
            Cursor outCursor = database.rawQuery(sql);

            recordCount = outCursor.getCount();
            Toast.makeText(getContext(), recordCount +"", Toast.LENGTH_SHORT).show();

            ArrayList<Note> items = new ArrayList<>();

            for(int i =0; i<recordCount; i++) {
                outCursor.moveToNext();

                int _id = outCursor.getInt(0);
                String titleOfDiary =outCursor.getString(1);
                String address = outCursor.getString(2);
                String locationX = outCursor.getString(3);
                String locationY = outCursor.getString(4);
                String picture = outCursor.getString(5);
                String contents = outCursor.getString(6);
                String dateStr = outCursor.getString(7);

//                _id, String titleOfDiary, String createDateStr, String address, String locationX, String locationY, String picture , String contents

                String createDateStr = null;
                if(dateStr != null && dateStr.length() >10) {
                    try {
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                            Date inDate = AppConstants.dateFormat4.parse(dateStr);
                            //createDateStr = AppConstants.dateFormat3.format(inDate);
                        }


                    } catch (Exception e ) {
                        e.printStackTrace();
                    }
                } else {
                    createDateStr ="";
                }

                items.add(new Note(_id,titleOfDiary, dateStr, address, locationX, locationY, picture, contents));
            }

            outCursor.close();

            adapter.setItems(items);
            adapter.notifyDataSetChanged();

        }

        return recordCount;
    }






}