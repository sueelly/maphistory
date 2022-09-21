package com.example.maphistory;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

import static com.example.maphistory.AppConstants.X;
import static com.example.maphistory.AppConstants.Y;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.maphistory.model.AutocompleteEditText;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;

import com.example.maphistory.MainActivity;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executor;

@RequiresApi(api = Build.VERSION_CODES.N)
public class WritePlaceFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMapClickListener{

    private static final String TAG = MainActivity.class.getSimpleName();
    private GoogleMap map1;

    View view;
    SupportMapFragment mapFragment1;
    MainActivity act;

    // The entry point to the Places API
    private PlacesClient placesClient;
    private SupportMapFragment mapFragment;
    private AutocompleteEditText addressField;
    private Place selected_place;
    private SelectedPlaceFragment selectedPlaceFragment1;
    final String apiKey = BuildConfig.MAPS_API_KEY;
    

    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean locationPermissionGranted;
    private Location lastKnownLocation;
    private FusedLocationProviderClient fusedLocationProviderClient;

    private static final int DEFAULT_ZOOM = 16;

    private final ActivityResultLauncher<Intent> startAutocomplete = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            (ActivityResultCallback<ActivityResult>) result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent intent = result.getData();
                    if (intent != null) {

                        //여기에 검색 결과 클릭시 이벤트 작성
                        //마커 추가, 카메라 이동, 장소에 대한 정보창 fragment 띄우기
                        selected_place = Autocomplete.getPlaceFromIntent(intent);

                        Log.d(TAG, "Place: " + selected_place.getName() +"," + selected_place.getId() +
                                "," + selected_place.getAddress());

                        Marker m1 = map1.addMarker(new MarkerOptions()
                                .position(selected_place.getLatLng())
                                .title(selected_place.getName())
                                .snippet(selected_place.getAddress())
                                .alpha(0.8f)
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));

                        X = selected_place.getLatLng().longitude;
                        Y = selected_place.getLatLng().latitude;

                        String a = selected_place.getName();
                        addressField.setHint(a);

                        map1.animateCamera(CameraUpdateFactory.newLatLngZoom(selected_place.getLatLng(), DEFAULT_ZOOM));

                        selectedPlaceFragment1 = new SelectedPlaceFragment();
                        //getFragmentManager().beginTransaction().replace(R.id.fragment_container1, selectedPlaceFragment1).commit();
                        selectedPlaceFragment1.place_name = a;
                    }
                } else if (result.getResultCode() == Activity.RESULT_CANCELED) {
                    Log.i(TAG, "User canceled autocomplete");
                }
            });

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        //Initialize view
        view = inflater.inflate(R.layout.fragment_write_place, container, false);

        //Initialize map fragment
        mapFragment1 = (SupportMapFragment)
                getChildFragmentManager().findFragmentById(R.id.map1);

        //Async map
        mapFragment1.getMapAsync(this);

        //Initialize a FusedLocationProviderClient
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());

        // Places SDK Initialization
        placesClient = Places.createClient(getActivity());

        addressField = (AutocompleteEditText) view.findViewById(R.id.autocomplete_address1);
        addressField.setOnClickListener(v -> startAutocompleteIntent());



        //Return view
        return view;
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {

        this.map1 = googleMap;
        this.map1.setOnMapClickListener(this);

        /**
         * map style 지정
         */
        try{
            boolean success = map1.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            getActivity(), R.raw.style_json));
            if (!success) {
                Log.e(TAG, "Style parsing failed.");
            }
        }catch (Resources.NotFoundException e) {
            Log.e(TAG, "Can't find style. Error: ", e);
        }

        getLocationPermission();
        updateLocationUI();
        //현재 위치에서 시작
        getDeviceLocation();

        //UI Setting
        UiSettings uiSettings = this.map1.getUiSettings();
        uiSettings.setZoomControlsEnabled(true);
        //zoom control 위치 조정
        googleMap.setPadding(0,0,16,600);

        googleMap.setOnMarkerClickListener(marker ->{
            X = marker.getPosition().longitude;
            Y = marker.getPosition().latitude;
            SelectedPlaceFragment selectedPlaceFragment1 = new SelectedPlaceFragment();

            return true;
        });
    }

    @Override
    public void onMapClick(@NonNull LatLng latLng) {
        //When clicked on map
        //Initialize marker options
        MarkerOptions markerOptions1 = new MarkerOptions();
        //Set position of marker
        markerOptions1.position(latLng);
        //Remove all marker
        map1.clear();
        //Animating to zoom the marker on the map
        map1.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM));
        //Add marker
        map1.addMarker(markerOptions1);
    }

    /**
     * Prompt the user for permission to use the device location.
     */
    public void getLocationPermission() {
        if(ContextCompat.checkSelfPermission(this.getContext(),
                ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    /**
     * Gets the current location of the device, and positions the map's camera.
     */
    public void getDeviceLocation() {
        /**
         * 가장 최근의 디바이스 위치 가져온다. 위치 이용 불가할 때 null
         */
        try{
            if (locationPermissionGranted) {
                Task<Location> locationResult = fusedLocationProviderClient.getLastLocation();

                locationResult.addOnCompleteListener(getActivity(), new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task){
                        if(task.isSuccessful()) {
                            // camera 위치를 현재 위치로 설정
                            lastKnownLocation = task.getResult();
                            if(lastKnownLocation != null){
                                map1.animateCamera(CameraUpdateFactory.newLatLngZoom(
                                        new LatLng(lastKnownLocation.getLatitude(),
                                                lastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                            }
                        }else {
                            Log.d(TAG, "Current location is null. Using defaults");
                            Log.e(TAG, "Exception: %s", task.getException());
                            map1.getUiSettings().setMyLocationButtonEnabled(false);
                        }
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage(), e);
        }
    }

    /**
     * Handles the result of the request for location permissions.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        locationPermissionGranted = false;
        if (requestCode
                == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {// If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                locationPermissionGranted = true;
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
        updateLocationUI();
    }

    /**
     * Updates the map's UI settings based on whether the user has granted location permission.
     */
    public void updateLocationUI() {
        if (map1 == null) {
            return;
        }
        try {
            if (locationPermissionGranted) {
                map1.setMyLocationEnabled(true);
                map1.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                map1.setMyLocationEnabled(false);
                map1.getUiSettings().setMyLocationButtonEnabled(false);
                lastKnownLocation = null;
                getLocationPermission();
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    /**
     * 자동완성 intent
     */
    private void startAutocompleteIntent() {

        // Set the fields to specify which types of place data to
        // return after the user has made a selection.
        List<Place.Field> fields = Arrays.asList(Place.Field.ADDRESS_COMPONENTS,
                Place.Field.LAT_LNG, Place.Field.VIEWPORT, Place.Field.NAME);

        // Build the autocomplete intent with field, county, and type filters applied
        Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
                .setHint("장소를 검색하세요")
                .setCountry("KR")
                .build(getActivity());
        startAutocomplete.launch(intent);
    }
}