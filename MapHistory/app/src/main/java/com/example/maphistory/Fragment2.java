package com.example.maphistory;
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

//        try {
////            loadNoteListData();
//        } catch (ParseException e) {
//            e.printStackTrace();
//        }

        return rootView;

    }

}