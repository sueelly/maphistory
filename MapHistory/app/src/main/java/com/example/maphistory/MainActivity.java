package com.example.maphistory;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static com.example.maphistory.AppConstants.X;
import static com.example.maphistory.AppConstants.Y;
import static com.example.maphistory.SelectDateFragment.DATE;

import static java.lang.System.in;

import android.annotation.SuppressLint;
import android.app.Activity;
//import android.app.Fragment;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.ViewStub;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.maphistory.database.DBManager;
import com.example.maphistory.model.AutocompleteEditText;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
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
import com.google.android.gms.maps.model.BitmapDescriptor;
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

import java.io.File;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

@RequiresApi(api = Build.VERSION_CODES.N)
public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMapClickListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String MAP_FRAGMENT_TAG = "MAP";
    private GoogleMap map;
    private CameraPosition cameraPosition;
    private Place place;
    Note item, item2;
    ArrayList<Note> items = new ArrayList<Note>();
    private final List<Marker> markers = new ArrayList<Marker>();
    DBManager dbHelper;
    Fragment1 fragmentOpen= null;
    Bitmap photoBitmap;
    //History Fragment - fragment, textViews
    HistoryFragment historyFragment = null;
    TextView titleOfHst, contentsOfHst, placeOfHst;
    int itemNum;
    Marker marker;

    private View mapPanel;
    private MarkerOptions markerOption_clicked;
    private MarkerOptions markerOption_history;
    private Marker marker_clicked;
    private Marker marker_history;
    private LatLng coordinates;
    private Place selected_place;
    private LatLng history_latlng;

    // info fragment about places
    private SelectedPlaceFragment selectedPlaceFragment1 = null;
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
                        m1.showInfoWindow();

                        X = selected_place.getLatLng().longitude;
                        Y = selected_place.getLatLng().latitude;

                        String a = selected_place.getName();
                        addressField.setHint(a);

                        history_latlng = new LatLng(selected_place.getLatLng().latitude + 0.0012,
                                selected_place.getLatLng().longitude);

                        map.animateCamera(CameraUpdateFactory.newLatLngZoom(history_latlng, DEFAULT_ZOOM));

                        selectedPlaceFragment1 = new SelectedPlaceFragment(a);
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.fragment_container1, selectedPlaceFragment1)
                                .commit();
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

        dbHelper = new DBManager(getApplicationContext(), 1);
        items = dbHelper.loadNoteList();

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

        //zoom in/out 버튼 사용 가능
        UiSettings uiSettings = this.map.getUiSettings();
        uiSettings.setZoomControlsEnabled(true);
        //zoom control 위치 조정
        googleMap.setPadding(0,0,16,600);

        //marker image size setting
        int height = 200;
        int width = 200;
        Bitmap b = BitmapFactory.decodeResource(getResources(), R.drawable.marker_normal);
        Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false);
        BitmapDescriptor smallMarkerIcon = BitmapDescriptorFactory.fromBitmap(smallMarker);

        /**
         * 저장된 일기들에 마커 띄우기
         */
        for(Note i: items) {
            LatLng latlng = new LatLng(Double.parseDouble(i.getLocationY()), Double.parseDouble(i.getLocationX()));
            MarkerOptions markerOptions2 = new MarkerOptions();
            markerOptions2.position(latlng)
                    .title(i.address)
                    .snippet(i.titleOfDiary)
                    .icon(smallMarkerIcon);
//
//            try {
//                setPicture(i.getPicture(),10);
//            } catch (Exception E){
//                Toast.makeText(getApplicationContext(), "", Toast.LENGTH_SHORT).show();
//            }

            marker = map.addMarker(markerOptions2);
            marker.setTag(i);

            if( items.indexOf(i) == items.size() - 1 ) {
                itemNum = items.indexOf(i);
                marker.showInfoWindow();
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latlng.latitude+0.005, latlng.longitude), 13));
                currentLocation();
            }

            //1주, 1달, 3달, 6달 날짜 set
            final Calendar c = Calendar.getInstance();

            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            c.add(c.DATE, -7);
            int year_7 = c.get(Calendar.YEAR);
            int month_7 = c.get(Calendar.MONTH);
            int day_7 = c.get(Calendar.DAY_OF_MONTH);

            c.add(c.MONTH, -1);
            int year_1m = c.get(Calendar.YEAR);
            int month_1m = c.get(Calendar.MONTH);
            int day_1m = c.get(Calendar.DAY_OF_MONTH);

            c.add(c.MONTH, -3);
            int year_3m = c.get(Calendar.YEAR);
            int month_3m = c.get(Calendar.MONTH);
            int day_3m = c.get(Calendar.DAY_OF_MONTH);

            c.add(c.MONTH, -6);
            int year_6m = c.get(Calendar.YEAR);
            int month_6m = c.get(Calendar.MONTH);
            int day_6m = c.get(Calendar.DAY_OF_MONTH);

            String today = setToday(year, month, day);
            String a_week_ago = setToday(year_7, month_7, day_7);
            String a_month_ago = setToday(year_1m, month_1m, day_1m);
            String three_month_ago = setToday(year_3m, month_3m, day_3m);
            String six_month_ago = setToday(year_6m, month_6m, day_6m);

            // 일기 시점에 따라 투명도 설정으로 구분 (1주, 1달, 3달, 6달)
            Note note = (Note) marker.getTag();
            int dateOfNote = Integer.parseInt(note.createDateStr);
            if( dateOfNote > Integer.parseInt(a_week_ago) ) {
                marker.setAlpha(0.9f);
            }
            else if (dateOfNote > Integer.parseInt(a_month_ago)) {
                marker.setAlpha(0.7f);
            }
            else if( dateOfNote > Integer.parseInt(three_month_ago)) {
                marker.setAlpha(0.5f);
            }
            else if( dateOfNote > Integer.parseInt(six_month_ago) ) {
                marker.setAlpha(0.3f);
            }

            markers.add(marker);
        }

        map.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(@NonNull Marker marker) {
                item = (Note) marker.getTag();
                Toast.makeText(getApplicationContext(), item.titleOfDiary, Toast.LENGTH_SHORT).show();

                historyFragment = new HistoryFragment(item);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container2, historyFragment)
                        .commit();
            }
        });

        // marker click event -> info 뜨게
        map.setOnMarkerClickListener(marker -> {

            //일기 저장되지 않은 마커 클릭 -> 일기 추가 창
            if( marker.getTag() == null) {
                selectedPlaceFragment1 = new SelectedPlaceFragment();
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container1, selectedPlaceFragment1)
                        .commit();
                selectedPlaceFragment1.setLatLng(marker);
            }
            //일기 저장된 마커 클릭 -> 일기 창 띄우기
            else{
                marker.showInfoWindow();
            }

            return true;
        });

//        getDeviceLocation();

    }

    public String setToday(int year, int month, int day){

        String month_string = setMonthDay(month+1);
        String day_string = setMonthDay(day);
        String year_string = Integer.toString(year);
        String dateMessage = (year_string + "" + month_string +"" + day_string);

        return DATE = dateMessage;
    }

    private String setMonthDay(int num) {
        if(num <10)
            return "0" + num;
        else
            return Integer.toString(num);
    }

    /**
     *지도 클릭이벤트
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onMapClick(LatLng point) {
        // 나중에 클릭한 장소 정보(이름, 일기 쓰기 버튼 등 뜨게 고치기)
        Fragment fragment = new Fragment();

        //일기 추가 창이 떠 있으면 닫아주기
        if (historyFragment != null) {
            fragmentTransaction1.replace(R.id.fragment_container2, fragment).commit();
            historyFragment = null;
        }

        if (selectedPlaceFragment1 != null) {
            fragmentTransaction1.replace(R.id.fragment_container1, fragment).commit();
            selectedPlaceFragment1 = null;
        }
        markerOption_clicked.position(point)
                .alpha(0.8f)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));

        //Animating to zoom the marker
        LatLng latlng = new LatLng(point.latitude + 0.0012, point.longitude );
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(latlng, DEFAULT_ZOOM));        //Add marker
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

    private void setPicture(String picturePath, int sampleSize) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = sampleSize;
        photoBitmap = BitmapFactory.decodeFile(picturePath, options);
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
                        //Left Button -> 이전 History로 이동 index - 1
                        //일기 미리보기 창이 떠 있을 때만 ?

                        leftMarker(marker);

                        break;

                    case R.id.rightArrowButton:
                        //right Button -> 다음 History로 이동

                        rightMarker(marker);

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

    private void leftMarker(Marker marker) {

        if(itemNum !=0) {
            LatLng latlng2 = new LatLng(Double.parseDouble
                    (items.get(itemNum-1).getLocationY()),
                    Double.parseDouble(items.get(itemNum-1).getLocationX()));
            MarkerOptions markerOptions2 = new MarkerOptions();
            markerOptions2.position(latlng2)
                    .title(items.get(itemNum-1).address)
                    .snippet(items.get(itemNum-1).titleOfDiary);

            marker = map.addMarker(markerOptions2);
            marker.setTag(items.get(itemNum-1));

            map.setOnMarkerClickListener(marker2 -> {
                marker2.getTag();
                marker2.showInfoWindow();
                return true;
            });

            marker.setAlpha(0.0f);

            map.animateCamera(CameraUpdateFactory.newLatLngZoom(latlng2, 13));
            marker.showInfoWindow();

            itemNum -= 1;
        }
        else
            Toast.makeText(this, "가장 오래된 일기입니다.", Toast.LENGTH_SHORT).show();

    }

    private void rightMarker(Marker marker) {

        if(itemNum != items.size() -1) {
            LatLng latlng2 = new LatLng(Double.parseDouble
                    (items.get(itemNum+1).getLocationY()),
                    Double.parseDouble(items.get(itemNum+1).getLocationX()));

            MarkerOptions markerOptions2 = new MarkerOptions();
            markerOptions2.position(latlng2)
                    .title(items.get(itemNum+1).address)
                    .snippet(items.get(itemNum+1).titleOfDiary);

            marker = map.addMarker(markerOptions2);
            marker.setTag(items.get(itemNum+1));

            map.setOnMarkerClickListener(marker2 -> {
                marker2.getTag();
                marker2.showInfoWindow();
                return true;
            });

            marker.setAlpha(0.0f);

            map.animateCamera(CameraUpdateFactory.newLatLngZoom(latlng2, 13));
            marker.showInfoWindow();

            itemNum += 1;
        }
        else
            Toast.makeText(this, "가장 최근 일기입니다.", Toast.LENGTH_SHORT).show();

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
                        //현재 위치 위경도 저장 (default 값)
                        X = deviceLocation.longitude;
                        Y = deviceLocation.latitude;

                        // 맵 화면 이동
//                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(deviceLocation, 13));

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
                @SuppressLint("MissingPermission") Task<Location> locationResult = fusedLocationProviderClient.getLastLocation();

                locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onComplete(@NonNull Task<Location> task){
                        if(task.isSuccessful()) {
                            // camera 위치를 현재 위치로 설정
                            lastKnownLocation = task.getResult();
                            X = lastKnownLocation.getLongitude();
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
            Toast.makeText(this, " ", Toast.LENGTH_SHORT).show();
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
    @SuppressLint("MissingPermission")
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

    long time = 0;

    @Override
    public void onBackPressed() {
        if (System.currentTimeMillis() - time >= 1000) {
            time = System.currentTimeMillis();
            Toast.makeText(getApplicationContext(), "뒤로 가기 버튼을 한 번 더 누르면 종료됩니다.", Toast.LENGTH_SHORT).show();
        } else if (System.currentTimeMillis() - time < 1000) { // 뒤로 가기 한번 더 눌렀을때의 시간간격 텀이 1초
            finishAffinity();
            System.runFinalization();
            System.exit(0);
        }
    }

}

