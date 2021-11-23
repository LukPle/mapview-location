package com.example.location_mapview;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

/**
 * This Activity shows Google Maps via MapView and the coordinates of a user selected place.
 * Clicking on the map marks the certain place and reveals its latitude and longitude.
 * It is also possible to get the coordinates for the current location of the user.
 * This process demands for the permission of using the location and GPS.
 *
 * Necessary dependencies for the build.gradle(:app) file:
 * implementation 'com.google.android.gms:play-services-location:18.0.0'
 * implementation 'com.google.android.gms:play-services-maps:17.0.1'
 *
 * Necessary permissions in the AndroidManifest.xml file:
 * uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"
 * uses-permission android:name="android.permission.INTERNET"
 *
 * Using Google Maps requires an API:
 * Map API in values/map_api
 *
 * Layout File: activity_main.xml
 *
 * @author Lukas Plenk
 */
public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMapClickListener {

    // This constant is used for checking if the user allowed location services
    private final int LOCATION_PERMISSION_CODE = 1;

    // Important objects and views for the map
    private MapView mapView;
    private GoogleMap map;
    private Marker marker;

    // Objects for the location
    private FusedLocationProviderClient fusedLocationProviderClient;
    private Location location;
    private LatLng latLng;

    // UI components
    private TextView textLatitude, textLongitude;
    private Button button;

    /**
     * The method handles location service integration with FusedLocationProviderClient.
     * The current location should be acquainted if the user gave permission.
     * There is also a Dialog for asking for permission if it is not allowed yet.
     * The MapView gets initialized afterwards.
     * @param savedInstanceState is a standard parameter.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textLatitude = findViewById(R.id.text_latitude);
        textLongitude = findViewById(R.id.text_longitude);

        button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                getLocation();
            }
        });

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        if (getApplication().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            getLocation();
        }
        else {

            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_CODE);
        }

        mapView = findViewById(R.id.mapView);
        mapView.getMapAsync(this);
        mapView.onCreate(savedInstanceState);
    }

    /**
     * Method that searches for the location of the user.
     * If this process was successful, the TextViews show the coordinates and a marker marks the location.
     * If not, a Toast Message will appear.
     */
    @SuppressLint("MissingPermission")
    private void getLocation() {

        fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {

                location = task.getResult();

                if (location == null) {

                    Toast.makeText(MainActivity.this, "Location not found", Toast.LENGTH_LONG).show();
                }
                else {

                    latLng = new LatLng(location.getLatitude(), location.getLongitude());
                    textLatitude.setText("Latitude: " +latLng.latitude);
                    textLongitude.setText("Longitude: " +latLng.longitude);
                    marker.setPosition(latLng);
                }
            }
        });
    }

    /**
     * Method for checking the request on location services.
     * If the request was allowed, the program checks for GPS and shows a Toast to the user.
     * If not, the program shows a Toast and disables the Button for getting the user location.
     * @param requestCode is the request code from the dialog.
     * @param permissions is the permission that was needed.
     * @param grantResults is used for distinguishing between multiple requests.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // This means the request was successful
        if (requestCode == LOCATION_PERMISSION_CODE) {

            // The first and only permission of the request was granted
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                Toast.makeText(MainActivity.this, "Location Access ALLOWED", Toast.LENGTH_LONG).show();

                // Location services also need active GPS
                if (!isGPSEnabled()) {

                    Toast.makeText(MainActivity.this, "Please turn on GPS on your Device", Toast.LENGTH_LONG).show();
                }
            }
            else {

                Toast.makeText(MainActivity.this, "Location Access DENIED", Toast.LENGTH_LONG).show();
                button.setEnabled(false);
            }
        }
    }

    /**
     * Method for checking if the GPS is enabled.
     * The return value is true for GPS and false for no GPS.
     * @return isEnabled which gives information if the GPS is active or not.
     */
    private boolean isGPSEnabled() {

        LocationManager locationManager = null;
        boolean isEnabled = false;

        if (locationManager == null) {

            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        }

        isEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        return isEnabled;
    }

    /**
     * This method includes all steps that set up Google Maps on the MapView.
     * It also sets the marker on the map.
     * @param googleMap is a object of Google Maps.
     */
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {

        map = googleMap;
        map.setOnMapClickListener(this);

        latLng = new LatLng(0, 0);
        marker = map.addMarker(new MarkerOptions().position(latLng));
    }

    /**
     * Method for handling clicks on the map.
     * A click should change the current location.
     * This involves latitude and longitude as well as the position of the marker.
     * @param latLng are the coordinates of the click.
     */
    @Override
    public void onMapClick(@NonNull LatLng latLng) {

        this.latLng = latLng;
        textLatitude.setText("Latitude: " +latLng.latitude);
        textLongitude.setText("Longitude: " +latLng.longitude);
        marker.setPosition(latLng);
    }

    /**
     * Using MapView requires the overriding of all Activity lifecycle methods.
     */

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState, @NonNull PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
}