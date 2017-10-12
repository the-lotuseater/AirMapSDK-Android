package com.airmap.airmapsdk.ui.activities;

import android.Manifest;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.airmap.airmapsdk.AirMapLog;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapboxMap;

/**
 * Created by collin@airmap.com on 9/22/17.
 */

public abstract class MyLocationMapActivity extends AppCompatActivity implements LocationListener {

    private static final String TAG = "MyLocationMapActivity";

    private static final int REQUEST_TURN_ON_LOCATION = 2945;
    private static final int REQUEST_LOCATION_PERMISSION = 9747;

    private LocationManager locationManager;
    private GoogleApiClient googleApiClient;

    private MapboxMap map;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
    }


    @Override
    protected void onStop() {
        super.onStop();

        if (locationManager != null) {
            locationManager.removeUpdates(this);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (googleApiClient != null) {
            googleApiClient.disconnect();
        }
    }

    /**
     *  Must be called after the activity or fragment setups the map
     *
     *  @param mapboxMap - Mapbox map
     */
    public void onMapReady(MapboxMap mapboxMap) {
        this.map = mapboxMap;

        enableLocation(true);
    }

    public void goToMyLocation() {
        enableLocation(true);
    }

    private void enableLocation(boolean forceLocationSettings) {
        // If we have the last location of the user, we can move the camera to that position.
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestLocationPermissionIfNeeded(forceLocationSettings);
            return;
        }

        Location lastLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        if (lastLocation != null) {
            AirMapLog.i(TAG, "Last known location: " + lastLocation);
            animateCameraToPosition(map, new LatLng(lastLocation));
        } else {
            // if user doesn't have
            turnOnLocation();

            locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, this, Looper.getMainLooper());
        }

        // Enable or disable the location layer on the map
        if (map != null) {
            map.setMyLocationEnabled(true);
            map.getMyLocationViewSettings().setAccuracyAlpha(0);
        }
    }

    protected void requestLocationPermissionIfNeeded(boolean forceLocationSettings) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
        } else {
            enableLocation(forceLocationSettings);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            for (int i = 0; i < permissions.length; i++) {
                if (permissions[i].equals(Manifest.permission.ACCESS_FINE_LOCATION) && grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    enableLocation(false);
                }
            }
        }
    }

    /**
     * This turns on Wifi/cell location tracking using Google Play services
     * It shows a dismissable dialog for users that don't have location already enabled
     */
    public void turnOnLocation() {
        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(com.google.android.gms.location.LocationServices.API).build();
        googleApiClient.connect();

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setNumUpdates(1);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);

        PendingResult<LocationSettingsResult> result = com.google.android.gms.location.LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(@NonNull LocationSettingsResult result) {
                final Status status = result.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        try {
                            status.startResolutionForResult(MyLocationMapActivity.this, REQUEST_TURN_ON_LOCATION);
                        } catch (IntentSender.SendIntentException e) {
                            Log.i(TAG, "PendingIntent unable to execute request.");
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        break;
                }
            }
        });
    }

    public void animateCameraToPosition(MapboxMap map, LatLng position) {
        if (position != null && map != null) {
            map.animateCamera(CameraUpdateFactory.newCameraPosition(
                    new CameraPosition.Builder()
                            .target(position)
                            .zoom(14)
                            .build()));
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            animateCameraToPosition(map, new LatLng(location));
            locationManager.removeUpdates(this);
        }
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {
    }

    @Override
    public void onProviderEnabled(String s) {
        AirMapLog.i(TAG, "onProviderEnabled: " + s);
    }

    @Override
    public void onProviderDisabled(String s) {
        AirMapLog.e(TAG, "onProviderDisabled: " + s);
    }
}