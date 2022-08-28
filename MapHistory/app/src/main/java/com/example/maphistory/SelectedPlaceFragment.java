package com.example.maphistory;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.fragment.app.Fragment;

public class SelectedPlaceFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //Inflate the layout for this fragment

        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_selected_place, container, false);

        ImageView btn_new_history = (ImageView) rootView.findViewById(R.id.btn_newhistory);

        btn_new_history.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), NewAndListActivity.class));
        });
        //btn_new_history.setOnClickListener(v->
                //startActivity(new Intent(getActivity(), NewAndListActivity.class)))

        return rootView;
    }
}