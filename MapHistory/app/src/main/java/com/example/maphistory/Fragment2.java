package com.example.maphistory;

import static com.example.maphistory.AppConstants.SAVE_MODIFY;
import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
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
//                NewAndListActivity a = (NewAndListActivity) getActivity();

            }
        });

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        adapter = new NoteAdapter(context);
        adapter.addItem(new Note(0, "오늘의 하루는 어땠나요?", "2022.08.18", "주소", "x", "y",
                "" , "당신의 매일을 남겨보세요."
        ));

        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(new OnNoteItemClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onItemClick(NoteAdapter.ViewHolder holder, View view, int position) {
                Note item = adapter.getItem(position);

                Toast.makeText(getContext(), "선택: " +item.getContents(), Toast.LENGTH_SHORT).show();
                fragmentNew = new Fragment1();
                fragmentNew.setItem(item);

                NewAndListActivity ad= (NewAndListActivity) getActivity();
                ad.bottomNavigation.setSelectedItemId(R.id.tab1);

                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, fragmentNew).commit();


            }
        });

        return rootView;
    }
}