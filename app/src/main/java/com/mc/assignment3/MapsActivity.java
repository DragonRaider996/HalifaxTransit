package com.mc.assignment3;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.transit.realtime.GtfsRealtime;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.InfoWindowAdapter {

    private final Timer timer = new Timer();
    private final String LOCATION = "location";
    private final String FILENAME = "map_details";
    private final String BUSES = "buses";
    protected ArrayList<Bus> busData;
    private Toolbar toolbar;
    private FloatingActionButton floatingActionButton;
    private boolean activeLocation = false;
    private boolean cameraMoved = false;
    private MarkerOptions currentLocationMarkerOption = null;
    private GoogleMap mMap;
    private int MY_PERMISSION_ACCESS_COARSE_LOCATION = 100;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private double prevLat, prevLong = 0;
    private float prevZoom = 0;
    private Set<String> busesToShow = null;
    private boolean centerLocation = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        floatingActionButton = findViewById(R.id.floatingActionButtonMap);
        busData = new ArrayList<>();
        toolbar = findViewById(R.id.toolbarMaps);
        setSupportActionBar(toolbar);
        //Creating location request to request for current location.
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        //Checking if any Map Camera state has been saved or not.
        SharedPreferences sharedPreferences = getSharedPreferences(FILENAME, MODE_PRIVATE);
        String map = sharedPreferences.getString("map", null);
        if (map != null) {
            prevLat = Double.longBitsToDouble(sharedPreferences.getLong("lat", Double.doubleToLongBits(0)));
            prevLong = Double.longBitsToDouble(sharedPreferences.getLong("long", Double.doubleToLongBits(0)));
            prevZoom = sharedPreferences.getFloat("zoom", 0);
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {

                    //Creating marker for current location.
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();
                    LatLng latLng = new LatLng(latitude, longitude);
                    Bitmap bitmapCurrentLocation = new BitMapMaker().makeBitMap(MapsActivity.this, R.drawable.ic_my_location_black_24dp, LOCATION);
                    currentLocationMarkerOption = new MarkerOptions().icon(BitmapDescriptorFactory.fromBitmap(bitmapCurrentLocation)).title("Your Location").position(latLng);
                    if (centerLocation) {
                        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLng(latLng);
                        mMap.animateCamera(cameraUpdate);
                    }
                    if (!cameraMoved) {
                        CameraUpdate cameraZoom = CameraUpdateFactory.newLatLngZoom(latLng,16);
                        mMap.animateCamera(cameraZoom);
                        cameraMoved = true;
                    }
                    addData();
                }
                super.onLocationResult(locationResult);
            }
        };

        checkBuses();
        checkLocation();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Halifax if the app begins for the first time.
        if (prevLat == 0) {
            LatLng halifax = new LatLng(44.651070, -63.582687);
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(halifax, 13));
        } else {
            LatLng halifax = new LatLng(prevLat, prevLong);
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(halifax, prevZoom));
        }
        //Executing Async part to fetch the bus data.
        handleData();
    }

    public void handleData() {
        new AsyncData().execute();
        //Fetching the data from the server after every 15 secs.
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                new AsyncData().execute();
            }
        }, 0, 15000);
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        //To place the snippet on each bus marker.
        LinearLayout info = new LinearLayout(this);
        info.setOrientation(LinearLayout.VERTICAL);

        TextView title = new TextView(this);
        title.setTextColor(Color.BLACK);
        title.setGravity(Gravity.CENTER);
        title.setTypeface(null, Typeface.BOLD);
        title.setText(marker.getTitle());

        TextView snippet = new TextView(this);
        snippet.setTextColor(Color.GRAY);
        snippet.setText(marker.getSnippet());

        info.addView(title);
        info.addView(snippet);

        return info;
    }

    public void addData() {
        //This function will add all the markers in the maps.
        mMap.clear();
        for (Bus bus : busData) {
            if (busesToShow != null) {
                if (busesToShow.contains(bus.getRouteId())) {
                    LatLng latLng = new LatLng(bus.getLatitude(), bus.getLongitude());
                    Bitmap bitmap = new BitMapMaker().makeBitMap(this, R.drawable.ic_directions_bus_black_24dp, bus.getRouteId());
                    mMap.addMarker(new MarkerOptions()
                            .position(latLng)
                            .title(bus.getRouteId())
                            .snippet("RouteId: " + bus.getRouteId() + "\n" + "Congestion: " + bus.getCongestion() + "\n" + "Status: " + bus.getStatus() + "\n" + "Occupancy: " + bus.getOccupancy() + "\n" + "Scheduled: " + bus.getScheduled())
                            .icon(BitmapDescriptorFactory.fromBitmap(bitmap)));
                    mMap.setInfoWindowAdapter(this);
                }
            } else {
                LatLng latLng = new LatLng(bus.getLatitude(), bus.getLongitude());
                Bitmap bitmap = new BitMapMaker().makeBitMap(this, R.drawable.ic_directions_bus_black_24dp, bus.getRouteId());
                mMap.addMarker(new MarkerOptions()
                        .position(latLng)
                        .title(bus.getRouteId())
                        .snippet("RouteId: " + bus.getRouteId() + "\n" + "Congestion: " + bus.getCongestion() + "\n" + "Status: " + bus.getStatus() + "\n" + "Occupancy: " + bus.getOccupancy() + "\n" + "Scheduled: " + bus.getScheduled())
                        .icon(BitmapDescriptorFactory.fromBitmap(bitmap)));
                mMap.setInfoWindowAdapter(this);
            }


        }
        if (activeLocation && currentLocationMarkerOption != null) {
            mMap.addMarker(currentLocationMarkerOption);
        }
    }

    public void onLocationClick(View view) {
        //When the location button is clicked.
        if (activeLocation) {
            floatingActionButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorWhite)));
            activeLocation = false;
            removeCurrentLocation();
        } else {
            floatingActionButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
            activeLocation = true;
            setCurrentLocation();
        }
    }

    public void removeCurrentLocation() {
        if (mMap != null) {
            cameraMoved = false;
            if (fusedLocationClient != null) {
                fusedLocationClient.removeLocationUpdates(locationCallback);
            }
            addData();
        }
    }


    public void setCurrentLocation() {
        //Will check if location permission is provided or not and if provided will fetch the current location.
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSION_ACCESS_COARSE_LOCATION
            );
        } else {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == MY_PERMISSION_ACCESS_COARSE_LOCATION && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Granted", Toast.LENGTH_SHORT).show();
            setCurrentLocation();
        } else {
            floatingActionButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorWhite)));
            activeLocation = false;
            Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public void checkLocation() {
        //Will check if the user has allowed the app to track the location or not.
        SharedPreferences sharedPreferences = getSharedPreferences(LOCATION, MODE_PRIVATE);
        if (sharedPreferences.getBoolean("getLocation", false)) {
            floatingActionButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
            activeLocation = true;
            centerLocation = true;
            setCurrentLocation();
        } else {
            floatingActionButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorWhite)));
            activeLocation = false;
            centerLocation = false;
            cameraMoved = false;
            removeCurrentLocation();
        }
    }

    public void checkBuses() {
        //To check if the user has selected amount of bus to be shown.
        SharedPreferences sharedPreferencesBuses = getSharedPreferences(BUSES, MODE_PRIVATE);
        Set<String> storedBuses = sharedPreferencesBuses.getStringSet(BUSES, null);
        if (storedBuses == null) {
            busesToShow = null;
        } else {
            busesToShow = new HashSet<>();
            busesToShow = storedBuses;
        }
    }

    @Override
    protected void onRestart() {
        checkLocation();
        checkBuses();
        addData();
        super.onRestart();
    }

    @Override
    protected void onStop() {
        //Saving camera state to the shared preference.
        CameraPosition cameraPosition = mMap.getCameraPosition();
        double longitude = cameraPosition.target.longitude;
        double latitude = cameraPosition.target.latitude;
        float zoom = cameraPosition.zoom;

        SharedPreferences sharedPreferences = getSharedPreferences(FILENAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("map", "Map");
        editor.putLong("lat", Double.doubleToLongBits(latitude));
        editor.putLong("long", Double.doubleToLongBits(longitude));
        editor.putFloat("zoom", zoom);

        editor.apply();

        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.setting) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            Animatoo.animateSlideLeft(this);
        }
        return super.onOptionsItemSelected(item);
    }

    //Async Task
    private class AsyncData extends AsyncTask<Void, Void, List<GtfsRealtime.FeedEntity>> {
        @Override
        protected List<GtfsRealtime.FeedEntity> doInBackground(Void... voids) {
            URL url = null;
            GtfsRealtime.FeedMessage feed = null;
            List<GtfsRealtime.FeedEntity> feedEntities = null;
            try {
                url = new URL("http://gtfs.halifax.ca/realtime/Vehicle/VehiclePositions.pb");
                feed = GtfsRealtime.FeedMessage.parseFrom(url.openStream());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (feed != null) {
                feedEntities = feed.getEntityList();
            }
            return feedEntities;
        }

        @Override
        protected void onPostExecute(List<GtfsRealtime.FeedEntity> feedEntities) {
            //Storing the data in array list format.
            super.onPostExecute(feedEntities);
            busData = new ArrayList<>();
            if (feedEntities != null) {
                for (GtfsRealtime.FeedEntity entity : feedEntities) {
                    if (entity.hasVehicle()) {
                        String routeId = entity.getVehicle().getTrip().getRouteId();
                        double longitude = entity.getVehicle().getPosition().getLongitude();
                        double latitude = entity.getVehicle().getPosition().getLatitude();
                        String congestion = entity.getVehicle().getCongestionLevel().getValueDescriptor().toString();
                        String status = entity.getVehicle().getCurrentStatus().getValueDescriptor().toString();
                        String occupancy = entity.getVehicle().getOccupancyStatus().toString();
                        String schedule = entity.getVehicle().getTrip().getScheduleRelationship().toString();
                        Bus bus = new Bus(routeId, latitude, longitude, congestion, status, occupancy, schedule);
                        busData.add(bus);
                    }
                }
            }

            addData();
        }
    }
}
