package com.example.maphistory;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

public class HistoryFragment extends Fragment {
    ViewGroup rootView;
    Note item;
    TextView titleOfHst, dateOfHst, placeOfHst, contentsOfHst;

    public HistoryFragment(Note item){
        this.item = item;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //Inflate the layout for this fragment

        rootView = (ViewGroup) inflater.inflate(R.layout.fragment_history, container, false);

        initComponent();

        setHistory();

        return rootView;
    }

    public void initComponent() {
        titleOfHst = (TextView) rootView.findViewById(R.id.titleOfHistory);
        dateOfHst = (TextView) rootView.findViewById(R.id.dateOfHistory);
        placeOfHst = (TextView) rootView.findViewById(R.id.placeOfHistory);
        contentsOfHst = (TextView) rootView.findViewById(R.id.contentsOfHistory);
    }

    /**
     * textView -> 일기 내용 설정하기
     */
    public void setHistory() {
        titleOfHst.setText(item.titleOfDiary);
        dateOfHst.setText(item.createDateStr);
        placeOfHst.setText(item.address);
        contentsOfHst.setText(item.contents);
    }

}
