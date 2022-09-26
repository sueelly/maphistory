package com.example.maphistory;

import static com.example.maphistory.AppConstants.X;
import static com.example.maphistory.AppConstants.Y;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.model.Marker;

public class SelectedPlaceFragment extends Fragment {

    ViewGroup rootView;
    public static String place_name;
    TextView selectedPlace;
    String place = null;

    public SelectedPlaceFragment(String place){
        this.place = place;
    }

    public SelectedPlaceFragment() {}
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //Inflate the layout for this fragment

        rootView = (ViewGroup) inflater.inflate(R.layout.fragment_selected_place, container, false);
        ImageView btn_new_history = (ImageView) rootView.findViewById(R.id.btn_newhistory);

        btn_new_history.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), NewAndListActivity.class));
        });

        return rootView;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void setLatLng(Marker marker) {
        X = marker.getPosition().longitude;
        Y = marker.getPosition().latitude;
    }

    public void initComponent() {
        selectedPlace = (TextView) rootView.findViewById(R.id.selectedPlaceName);
    }
    public void setPlace() {
        selectedPlace.setText(this.place);
    }

}