package com.example.maphistory;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;

public class SelectedPlaceFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //Inflate the layout for this fragment

        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.selected_place_fragment, container, false);

        Button btn_new_history = (Button) rootView.findViewById(R.id.btn_new_history);

        btn_new_history.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), NewHistoryActivity.class);
                startActivity(intent);

                startActivity(new Intent(getActivity(), NewHistoryActivity.class));

                btn_new_history.setText("왜 안되냐");
                int a = 3;
            }
        });

        return rootView;
    }
}
