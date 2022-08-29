package com.example.maphistory;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.ACCESS_COARSE_LOCATION;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.ViewStub;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.maphistory.model.AutocompleteEditText;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AddressComponent;
import com.google.android.libraries.places.api.model.AddressComponents;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.PlaceLikelihood;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMapClickListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String MAP_FRAGMENT_TAG = "MAP";
    private GoogleMap map;
    private CameraPosition cameraPosition;
    private Place place;

    private View mapPanel;
    private MarkerOptions markerOption_clicked;
    private MarkerOptions markerOption_history;
    private Marker marker_clicked;
    private Marker marker_history;
    private LatLng coordinates;
    private Place selected_place;

    // info fragment about places
    private SelectedPlaceFragment selectedPlaceFragment1;
    private FragmentManager fragmentManager1;
    private FragmentTransaction fragmentTransaction1;

    // The Buttons of MainActivity
    private Button btn_newHistory, btn_historyList, btn_mapHistory;
    private ImageButton btn_leftArrow, btn_rightArrow;

    // The entry point to the Places API
    private PlacesClient placesClient;
    private SupportMapFragment mapFragment;
    private AutocompleteEditText addressField;
    private LatLng deviceLocation;

    // A default location (SEOUL) and default zoom
    private final LatLng defaultLocation = new LatLng(37.56, 126.97);
    private static final int DEFAULT_ZOOM = 16;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean locationPermissionGranted;

    private Location lastKnownLocation;

    NoteAdapter adapter;
    boolean selected_frag = false;

    private FusedLocationProviderClient fusedLocationProviderClient;
    private static final double acceptedProximity = 150;
    private static int AUTOCOMPLETE_REQUEST_CODE = 1;
    final String apiKey = BuildConfig.MAPS_API_KEY;

    /**
     * 자동완성 기능 수행 intent, 이벤트 지정
     */
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

                        Marker m1 = map.addMarker(new MarkerOptions()
                                .position(selected_place.getLatLng())
                                .title(selected_place.getName())
                                .snippet(selected_place.getAddress())
                                .alpha(0.8f)
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));

                        String a = selected_place.getName();
                        addressField.setHint(a);

                        map.animateCamera(CameraUpdateFactory.newLatLngZoom(selected_place.getLatLng(), DEFAULT_ZOOM));

                        selectedPlaceFragment1 = new SelectedPlaceFragment();
                        fragmentTransaction1.add(R.id.fragment_container1, selectedPlaceFragment1).commit();
                    }
                } else if (result.getResultCode() == Activity.RESULT_CANCELED) {
                    Log.i(TAG, "User canceled autocomplete");
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Build the map.
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Initialization and setting the listener of buttons
        this.InitializeView();
        this.SetListener();

        // Places SDK Initialization
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), apiKey);
        }
        placesClient = Places.createClient(this);

        // Construct a FusedLocationProviderClient
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // 장소 검색 창
        addressField = (AutocompleteEditText) findViewById(R.id.autocomplete_address);
        addressField.setOnClickListener(v -> startAutocompleteIntent());

        //fragment manager for "selected_place_fragment"
        fragmentManager1 = getSupportFragmentManager();
        fragmentTransaction1 = fragmentManager1.beginTransaction();

    }

    /**
     * 지도 설정
     */
    @Override
    public void onMapReady(final GoogleMap googleMap) {

        this.map = googleMap;
        this.map.setOnMapClickListener(this);
        markerOption_clicked = new MarkerOptions();
        markerOption_history = new MarkerOptions();

        /**
         * map style 지정
         */
        try{
            boolean success = map.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(

                            this, R.raw.style_json));
            if (!success) {
                Log.e(TAG, "Style parsing failed.");
            }
        }catch (Resources.NotFoundException e) {
            Log.e(TAG, "Can't find style. Error: ", e);
        }

        // Info 창 설정 ... 나중에
        this.map.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
//            View window = getLayoutInflater().inflate(R.layout.map_info, null);
            @Nullable
            @Override
            public View getInfoContents(@NonNull Marker marker) {
//                Button btn_newHistoryMake = window.findViewById(R.id.btn_newHistoryMake);
//                btn_newHistoryMake.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        startActivity(new Intent(getApplicationContext(), NewHistoryActivity.class));
//                        finish();
//                    }
//                });
                return null;
            }
            @Nullable
            @Override
            public View getInfoWindow(@NonNull Marker marker) {
                return null;
            }
        });

        getLocationPermission();
        updateLocationUI();
        //getDeviceLocation();

        //zoom in/out 버튼 사용 가능
        UiSettings uiSettings = this.map.getUiSettings();
        uiSettings.setZoomControlsEnabled(true);
        //zoom control 위치 조정
        googleMap.setPadding(0,0,16,600);

        //처음 시작할 때 위치 설정 -> 가장 최근 History의 위치로
        markerOption_history.position(defaultLocation)
                .title("서울")
                .snippet("한국의 수도")
                .alpha(0.8f)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
        marker_history = map.addMarker(markerOption_history);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, DEFAULT_ZOOM));


        // marker click event -> info 뜨게
        map.setOnMarkerClickListener(marker -> {

            selectedPlaceFragment1 = new SelectedPlaceFragment();
            fragmentTransaction1.add(R.id.fragment_container1, selectedPlaceFragment1).commit();
            return true;
        });

    }

    /**
     *지도 클릭이벤트
     */
    @Override
    public void onMapClick(LatLng point) {
        //Remove all marker
        //map.clear();
        // 나중에 클릭한 장소 정보(이름, 일기 쓰기 버튼 등 뜨게 고치기)
        markerOption_clicked.position(point)
                .alpha(0.8f)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
        //Animating to zoom the marker
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(point, DEFAULT_ZOOM));
        //Add marker
        marker_clicked = map.addMarker(markerOption_clicked);
    }

    /**
     * Buttons initialization
     */
    public void InitializeView() {
        btn_newHistory = (Button) findViewById(R.id.newHistoryButton);
        btn_historyList = (Button) findViewById(R.id.historyListButton);
        btn_mapHistory = (Button) findViewById(R.id.mapHistoryButton);
        btn_leftArrow = (ImageButton) findViewById(R.id.leftArrowButton);
        btn_rightArrow = (ImageButton) findViewById(R.id.rightArrowButton);
    }


    /**
     * Buttons listener
     */
    public void SetListener() {
        View.OnClickListener Listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.newHistoryButton:
                        //New History Button -> New History Activity로 이동
                        startActivity(new Intent(getApplicationContext(), NewAndListActivity.class));
                        break;
                    case R.id.historyListButton:
                        //History List Button -> History List Activity로 이동
                        startActivity(new Intent(getApplicationContext(), NewAndListActivity.class));
                        break;
                    case R.id.mapHistoryButton:
                        //Map History Button -> Map History Activity로 이동
                        startActivity(new Intent(getApplicationContext(), MapHistoryActivity.class));
                        break;
                    case R.id.leftArrowButton:
                        //Left Button -> 이전 History로 이동
                        break;
                    case R.id.rightArrowButton:
                        //right Button -> 다음 History로 이동
                        break;
                }
            }
        };
        btn_newHistory.setOnClickListener(Listener);
        btn_historyList.setOnClickListener(Listener);
        btn_mapHistory.setOnClickListener(Listener);
        btn_leftArrow.setOnClickListener(Listener);
        btn_rightArrow.setOnClickListener(Listener);
    }

    /**
     * 디바이스 현재 위치 가져와서 보여주기
     */
    public void currentLocation() {
        if(map == null){
            return;
        }
        if(locationPermissionGranted) {
            // 반환할 데이터 타입 지정
            List<Place.Field> placeFields = Arrays.asList(Place.Field.NAME,
                    Place.Field.ADDRESS, Place.Field.LAT_LNG);
            // Use the builder to create a FindCurrentPlaceRequest.
            // 위에서 지정한 데이터 타입 전달
            FindCurrentPlaceRequest request =
                    FindCurrentPlaceRequest.newInstance(placeFields);

            // Get the likely places
            @SuppressWarnings("MissingPermission") final Task<FindCurrentPlaceResponse> placeResult =
                    placesClient.findCurrentPlace(request);
            placeResult.addOnCompleteListener(new OnCompleteListener<FindCurrentPlaceResponse>() {
                @Override
                public void onComplete(@NonNull Task<FindCurrentPlaceResponse> task) {
                    if(task.isSuccessful() && task.getResult() != null) {
                        FindCurrentPlaceResponse likelyPlaces = task.getResult();

                        deviceLocation = likelyPlaces.getPlaceLikelihoods().get(0).getPlace().getLatLng();
                        //현재 위치에 마커 추가.. 나중에

                        // 맵 화면 이동
                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(deviceLocation, DEFAULT_ZOOM));

                    }else {
                        Log.e(TAG, "Exception: $s", task.getException());
                    }
                }
            });
        }else {
            // The user has not granted permission.
            Log.i(TAG, "The user did not grant location permission.");

            // Add a default marker, because the user hasn't selected a place.
            map.addMarker(new MarkerOptions()
                    .title("서울")
                    .position(defaultLocation));
                    //.snippet(getString(R.string.default_info_snippet)));
            // Prompt the user for permission.
            getLocationPermission();
        }
    }

    /**
     * Prompt the user for permission to use the device location.
     */
    public void getLocationPermission() {
        if(ContextCompat.checkSelfPermission(this.getApplicationContext(),
                ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
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

                locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task){
                        if(task.isSuccessful()) {
                            // camera 위치를 현재 위치로 설정
                            lastKnownLocation = task.getResult();
                            if(lastKnownLocation != null){
                                map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                        new LatLng(lastKnownLocation.getLatitude(),
                                                lastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                            }
                        }else {
                            Log.d(TAG, "Current location is null. Using defaults");
                            Log.e(TAG, "Exception: %s", task.getException());
                            map.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, DEFAULT_ZOOM));
                            map.getUiSettings().setMyLocationButtonEnabled(false);
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
        if (map == null) {
            return;
        }
        try {
            if (locationPermissionGranted) {
                map.setMyLocationEnabled(true);
                map.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                map.setMyLocationEnabled(false);
                map.getUiSettings().setMyLocationButtonEnabled(false);
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
                .build(this);
        startAutocomplete.launch(intent);
    }

    private void showMap(Place place) {
        coordinates = place.getLatLng();

        // It isn't possible to set a fragment's id programmatically so we set a tag instead and
        // search for it using that.
        mapFragment = (SupportMapFragment)
                getSupportFragmentManager().findFragmentByTag(MAP_FRAGMENT_TAG);

        // We only create a fragment if it doesn't already exist.
        if (mapFragment == null) {
            mapPanel = ((ViewStub) findViewById(R.id.stub_map)).inflate();
            GoogleMapOptions mapOptions = new GoogleMapOptions();
            mapOptions.mapToolbarEnabled(false);

            // To programmatically add the map, we first create a SupportMapFragment.
            mapFragment = SupportMapFragment.newInstance(mapOptions);

            // Then we add it using a FragmentTransaction.
            getSupportFragmentManager()
                    .beginTransaction()
                    //.add(R.id.confirmation_map, mapFragment, MAP_FRAGMENT_TAG)
                    .commit();
            mapFragment.getMapAsync(this);
        } else {
            updateMap(coordinates);
        }
    }

    private void updateMap(LatLng latLng) {
        marker_clicked.setPosition(latLng);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f));
        if (mapPanel.getVisibility() == View.GONE) {
            mapPanel.setVisibility(View.VISIBLE);
        }
    }

    private void clearForm() {
        addressField.setText("");
        if (mapPanel != null) {
            mapPanel.setVisibility(View.GONE);
        }
        addressField.requestFocus();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

//        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
//            if (resultCode == RESULT_OK) {
//                place = Autocomplete.getPlaceFromIntent(data);
//
//                Log.i(TAG, "Place: " + place.getName() + ", " + place.getId());
//            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
//                //TODO: Handle the error.
//                Toast.makeText(getApplicationContext(), " dd", Toast.LENGTH_LONG).show();
//                Status status = Autocomplete.getStatusFromIntent(data);
//                Log.i(TAG, status.getStatusMessage());
//            } else if (resultCode == RESULT_CANCELED) {
//                // The user canceled the operation.
//            }
//            return;
//        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}

