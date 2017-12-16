package com.airmap.airmapsdk.ui.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.airmap.airmapsdk.AirMapLog;
import com.airmap.airmapsdk.Analytics;
import com.airmap.airmapsdk.R;
import com.airmap.airmapsdk.ui.views.AirMapMapView;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.plugins.locationlayer.LocationLayerMode;
import com.mapbox.mapboxsdk.plugins.locationlayer.LocationLayerPlugin;
import com.mapbox.services.android.telemetry.location.GoogleLocationEngine;
import com.mapbox.services.android.telemetry.location.LocationEngine;
import com.mapbox.services.android.telemetry.location.LocationEngineListener;
import com.mapbox.services.android.telemetry.location.LocationEnginePriority;

/**
 * Created by collin@airmap.com on 11/27/17.
 */

public abstract class MyLocationMapActivity extends AppCompatActivity implements LocationEngineListener, AirMapMapView.OnMapLoadListener {

    private static final String TAG = "MyLocationMapActivity";

    private static final int REQUEST_LOCATION_PERMISSION = 7737;
    private static final int REQUEST_TURN_ON_LOCATION = 8849;

    private LocationLayerPlugin locationLayerPlugin;
    private LocationEngine locationEngine;

    private boolean hasLoadedMyLocation;

    @Override
    public void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        if (getMapView() != null) {
            getMapView().addOnMapLoadListener(this);
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onStart() {
        super.onStart();

        if (locationLayerPlugin != null && requestLocationPermissionIfNeeded()) {
            locationLayerPlugin.onStart();
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        if (locationLayerPlugin != null) {
            locationLayerPlugin.onStop();
        }

        if (getMapView() != null) {
            getMapView().removeOnMapLoadListener(this);
        }
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_TURN_ON_LOCATION: {
                if (resultCode == Activity.RESULT_OK) {
                    AirMapLog.d(TAG, "Location setting turned on by user");
                    goToLastLocation(false,3);
                }
                break;
            }
        }
    }

    @Override
    public void onConnected() {
        AirMapLog.d(TAG, "LocationEngine onConnected");

        goToLastLocation(false);
    }


    @Override
    public void onLocationChanged(Location location) {
        AirMapLog.d(TAG, "LocationEngine onLocationChanged: " + location);
        zoomTo(location, false);
    }

    @SuppressLint("MissingPermission")
    public void goToLastLocation(boolean force) {
        if (requestLocationPermissionIfNeeded()) {
            if (locationEngine.getLastLocation() != null) {
                zoomTo(locationEngine.getLastLocation(), force);
            } else {
                turnOnLocation();
            }
        }
    }

    private void goToLastLocation(final boolean force, final int retries) {
        int delay = 2500 * Math.max((3 - retries), 1);

        new Handler().postDelayed(new Runnable() {
            @SuppressLint("MissingPermission")
            @Override
            public void run() {
                AirMapLog.e(TAG, "goToLastLocation w/ " + retries + " retries");
                if (locationEngine.getLastLocation() != null) {
                    zoomTo(locationEngine.getLastLocation(), false);
                } else if (retries > 0) {
                    goToLastLocation(force, retries - 1);
                }
            }
        }, delay);
    }

    private void zoomTo(Location location, boolean force) {
        // only zoom to user's location once
        if (!hasLoadedMyLocation || force) {
            AirMapLog.e(TAG, "zoomTo: " + location);
            getMapView().getMap().easeCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 13));
            hasLoadedMyLocation = true;
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapLoaded() {
        AirMapLog.d(TAG, "onMapLoaded");
        locationEngine = new GoogleLocationEngine(MyLocationMapActivity.this);
        locationEngine.addLocationEngineListener(MyLocationMapActivity.this);
        locationEngine.setPriority(LocationEnginePriority.BALANCED_POWER_ACCURACY);
        locationEngine.activate();

        locationLayerPlugin = new LocationLayerPlugin(getMapView(), getMapView().getMap(), locationEngine);
        locationLayerPlugin.applyStyle(R.style.CustomLocationLayer);

        if (requestLocationPermissionIfNeeded()) {
            locationLayerPlugin.setLocationLayerEnabled(LocationLayerMode.TRACKING);
        }
    }

    @Override
    public void onMapFailed(AirMapMapView.MapFailure failure) {
        switch (failure) {
            case INACCURATE_DATE_TIME_FAILURE:
                // record issue in firebase & logs
                Analytics.report(new Exception("Mapbox map failed to load due to invalid date/time"));
                AirMapLog.e(TAG, "Mapbox map failed to load due to invalid date/time");

                // ask user to enable "Automatic Date/Time"
                new AlertDialog.Builder(this)
                        .setTitle(R.string.error_loading_map_title)
                        .setMessage(R.string.error_loading_map_message)
                        .setPositiveButton(R.string.error_loading_map_button, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // open settings and kill this activity
                                startActivity(new Intent(Settings.ACTION_DATE_SETTINGS));
                                finish();
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, null)
                        .show();
                break;
            case NETWORK_CONNECTION_FAILURE:
                // record issue in firebase & logs
                Analytics.report(new Exception("Mapbox map failed to load due to no network connection"));
                AirMapLog.e(TAG, "Mapbox map failed to load due to no network connection");

                // ask user to turn on wifi/LTE
                new AlertDialog.Builder(this)
                        .setTitle(R.string.error_loading_map_title)
                        .setMessage(R.string.error_loading_map_network_message)
                        .setPositiveButton(R.string.error_loading_map_network_button, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // open settings and kill this activity
                                startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                                finish();
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, null)
                        .show();

                break;
            case UNKNOWN_FAILURE:
            default:
                // record issue in firebase & logs
                Analytics.report(new Exception("Mapbox map failed to load due to unknown reason"));
                AirMapLog.e(TAG, "Mapbox map failed to load due to unknown reason");

                //TODO: show generic error to user?
                break;
        }
    }

    private boolean requestLocationPermissionIfNeeded() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_LOCATION_PERMISSION);
            return false;
        }

        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            for (int i = 0; i < permissions.length; i++) {
                if ((permissions[i].equals(Manifest.permission.ACCESS_FINE_LOCATION) || permissions[i].equals(Manifest.permission.ACCESS_COARSE_LOCATION))
                        && grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    goToLastLocation(false);
                }
            }
        }
    }

    /**
     * This turns on Wifi/cell location tracking using Google Play services
     * It shows a dismissable dialog for users that don't have location already enabled
     */
    public void turnOnLocation() {
        GoogleApiClient googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(com.google.android.gms.location.LocationServices.API).build();
        googleApiClient.connect();

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setNumUpdates(1);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);

        PendingResult<LocationSettingsResult> result = com.google.android.gms.location.LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @SuppressLint("MissingPermission")
            @Override
            public void onResult(@NonNull LocationSettingsResult result) {
                final Status status = result.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        Log.e(TAG, "LocationSettings success: " + locationEngine.getLastLocation());
                        goToLastLocation(false, 3);
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        Log.e(TAG, "LocationSettings resolution required");
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

    protected abstract AirMapMapView getMapView();

    public void setMapView(AirMapMapView mapView) {
        mapView.addOnMapLoadListener(this);
    }
}
