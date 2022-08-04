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
import android.widget.ImageButton;

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

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
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

    private View mapPanel;
    private Marker marker;
    private LatLng coordinates;

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

    private FusedLocationProviderClient fusedLocationProviderClient;
    private static final double acceptedProximity = 150;
    private static int AUTOCOMPLETE_REQUEST_CODE = 1;
    final String apiKey = BuildConfig.MAPS_API_KEY;

    private final ActivityResultLauncher<Intent> startAutocomplete = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            (ActivityResultCallback<ActivityResult>) result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent intent = result.getData();
                    if (intent != null) {
                        Place place = Autocomplete.getPlaceFromIntent(intent);

                        Log.d(TAG, "Place: " + place.getAddressComponents());

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
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
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
    }

    /**
     * 지도 설정
     */
    @Override
    public void onMapReady(final GoogleMap googleMap) {

        this.map = googleMap;
        this.map.setOnMapClickListener(this);

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
            View window = getLayoutInflater().inflate(R.layout.map_info, null);
            @Nullable
            @Override
            public View getInfoContents(@NonNull Marker marker) {
                Button btn_newHistoryMake = window.findViewById(R.id.btn_newHistoryMake);
                btn_newHistoryMake.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivity(new Intent(getApplicationContext(), NewHistoryActivity.class));
                        finish();
                    }
                });
                return window;
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

        //처음 시작할 때 위치 설정 -> 가장 최근 History의 위치로

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(defaultLocation);
        markerOptions.title("서울");
        markerOptions.snippet("한국의 수도");
        marker = map.addMarker(markerOptions);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, DEFAULT_ZOOM));
    }

    /**
     *지도 클릭이벤트
     */
    @Override
    public void onMapClick(LatLng point) {
        // 나중에 클릭한 장소 정보(이름, 일기 쓰기 버튼 등 뜨게 고치기)
        MarkerOptions markerO = new MarkerOptions();
        markerO.position(point);
        markerO.title(point.toString());
        marker = map.addMarker(markerO);
        marker.showInfoWindow();
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(point, DEFAULT_ZOOM));
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
                        startActivity(new Intent(getApplicationContext(), HistoryListActivity.class));

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
    private void getDeviceLocation() {
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
    private void updateLocationUI() {
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
                Place.Field.LAT_LNG, Place.Field.VIEWPORT);

        // Build the autocomplete intent with field, county, and type filters applied
        Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
                .setHint("장소를 검색하세요")
                .setCountry("KR")
                .build(this);
//        startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);
        startAutocomplete.launch(intent);
    }

    private void fillInAddress(Place place) {
        AddressComponents components = place.getAddressComponents();
        StringBuilder address1 = new StringBuilder();

        addressField.setText(address1.toString());

        // Add a map for visual confirmation of the address
        showMap(place);
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
        marker.setPosition(latLng);
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

    @SuppressLint("MissingPermission")
    private void getAndCompareLocations() {
        // TODO: Detect and handle if user has entered or modified the address manually and update
        // the coordinates variable to the Lat/Lng of the manually entered address. May use
        // Geocoding API to convert the manually entered address to a Lat/Lng.
        LatLng enteredLocation = coordinates;
        map.setMyLocationEnabled(true);

        // [START maps_solutions_android_location_get]
        FusedLocationProviderClient fusedLocationClient =
                LocationServices.getFusedLocationProviderClient(this);

        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            // Got last known location. In some rare situations this can be null.
            if (location == null) {
                return;
            }

            deviceLocation = new LatLng(location.getLatitude(), location.getLongitude());
            // [START_EXCLUDE]
            Log.d(TAG, "device location = " + deviceLocation.toString());
            Log.d(TAG, "entered location = " + enteredLocation.toString());

        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = Autocomplete.getPlaceFromIntent(data);
                Log.i(TAG, "Place: " + place.getName() + ", " + place.getId());
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                //TODO: Handle the error.
                Status status = Autocomplete.getStatusFromIntent(data);
                Log.i(TAG, status.getStatusMessage());
            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
