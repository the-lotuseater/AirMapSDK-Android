package com.airmap.airmapsdktest;

import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.UiThread;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.airmap.airmapsdk.AirMapException;
import com.airmap.airmapsdk.Models.Coordinate;
import com.airmap.airmapsdk.Models.Flight.AirMapFlight;
import com.airmap.airmapsdk.Models.Traffic.AirMapTraffic;
import com.airmap.airmapsdk.Networking.Callbacks.AirMapCallback;
import com.airmap.airmapsdk.Networking.Callbacks.AirMapTrafficListener;
import com.airmap.airmapsdk.Networking.Services.AirMap;
import com.airmap.airmapsdk.Networking.Services.TrafficService;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;

import java.util.ArrayList;
import java.util.List;

public class TrafficAlertActivity extends AppCompatActivity implements OnMapReadyCallback, AirMapTrafficListener, AirMapCallback<AirMapFlight> {

    MapView mapView;
    MapboxMap map;
    List<MarkerOptions> markers;
    TrafficService service;
    IconFactory factory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_traffic_alert);
        markers = new ArrayList<>();
        mapView = (MapView) findViewById(R.id.mapview);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        factory = IconFactory.getInstance(this);
    }

    @UiThread
    @Override
    public void onMapReady(MapboxMap mapboxMap) {
        this.map = mapboxMap;
        map.resetNorth();
        AirMap.getCurrentFlight(this);
        service = AirMap.getAirMapTrafficService();
        service.addListener(this);
        service.connect();

    }

    @Override
    public void onAddTraffic(List<AirMapTraffic> added) {
        for (final AirMapTraffic traffic : added) {

            final MarkerOptions marker = new MarkerOptions().
                    position(getLatLng(traffic.getCoordinate())).
                    title(traffic.getId()).
                    icon(getIcon(traffic));
            markers.add(marker);

            runOnUiThread(new Runnable() {
                public void run() {
                    Log.v("TrafficAlertActivity", "Trying to add new marker");
                    Log.v("TrafficAlertActivity", traffic.getCoordinate().getLatitude() + ", " + traffic.getCoordinate().getLongitude());
                    map.addMarker(marker);
                }
            });
        }
    }

    @Override
    public void onUpdateTraffic(List<AirMapTraffic> updated) {
        for (AirMapTraffic traffic : updated) {
            final MarkerOptions options = searchForId(traffic.getId());
            if (options == null) {
                return;
            }
            final LatLng old = options.getPosition();
            markers.remove(options);
            options.position(getLatLng(traffic.getCoordinate()));
            options.icon(getIcon(traffic));
            markers.add(options);
            runOnUiThread(new Runnable() {
                public void run() {
                    Marker marker = options.getMarker();
                    ValueAnimator markerAnimator = ObjectAnimator.ofObject(marker, "position", new LatLngEvaluator(), old, marker.getPosition());
                    markerAnimator.setDuration(1100);
                    markerAnimator.start();
//                    map.updateMarker(marker);
                }
            });
        }
    }

    @Override
    public void onRemoveTraffic(List<AirMapTraffic> removed) {
        for (AirMapTraffic traffic : removed) {
            final MarkerOptions options = searchForId(traffic.getId());
            if (options == null) {
                return;
            }
            markers.remove(options);
            runOnUiThread(new Runnable() {
                public void run() {
                    map.removeMarker(options.getMarker());
                }
            });
        }
    }

    private MarkerOptions searchForId(String id) {
        for (MarkerOptions options : markers) {
            if (options.getTitle().equals(id)) {
                return options;
            }
        }
        return null;
    }

    private LatLng getLatLng(Coordinate coordinate) {
        return new LatLng(coordinate.getLatitude(), coordinate.getLongitude());
    }

    private Icon getIcon(AirMapTraffic traffic) {
        Drawable iconDrawable = ContextCompat.getDrawable(this, R.drawable.airmap_flight_marker);
        if (traffic == null) {
            //Don't do anything
        } else if (traffic.getTrafficType() == AirMapTraffic.TrafficType.Alert) {
            int id = getResources().getIdentifier("traffic_marker_icon_" + directionFromBearing(traffic.getTrueHeading()), "drawable", "com.airmap.airmapsdktest");
            iconDrawable = ContextCompat.getDrawable(this, id);
        } else if (traffic.getTrafficType() == AirMapTraffic.TrafficType.SituationalAwareness) {
            int id = getResources().getIdentifier("sa_traffic_marker_icon_" + directionFromBearing(traffic.getTrueHeading()), "drawable", "com.airmap.airmapsdktest");
            iconDrawable = ContextCompat.getDrawable(this, id);
        }
        return factory.fromDrawable(iconDrawable);
    }

    public String[] getCompassDirections() {
        return new String[]{"n", "nne", "ne", "ene", "e", "ese", "se", "sse", "s", "ssw", "sw", "wsw", "w", "wnw", "nw", "nnw"};
    }

    public String directionFromBearing(double bearing) {
        int index = (int) ((bearing / 22.5) + 0.5) % 16;
        return getCompassDirections()[index];

    }

    private static class LatLngEvaluator implements TypeEvaluator<LatLng> {
        // Method is used to interpolate the marker animation.
        private LatLng latLng = new LatLng();
        @Override
        public LatLng evaluate(float fraction, LatLng startValue, LatLng endValue) {
            latLng.setLatitude(startValue.getLatitude() + ((endValue.getLatitude() - startValue.getLatitude()) * fraction));
            latLng.setLongitude(startValue.getLongitude() + ((endValue.getLongitude() - startValue.getLongitude()) * fraction));
            return latLng;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        AirMap.suspend();
        mapView.onPause();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @UiThread
    @Override
    public void onSuccess(final AirMapFlight response) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                map.addMarker(new MarkerOptions()
                        .position(new LatLng(response.getCoordinate().getLatitude(), response.getCoordinate().getLongitude()))
                        .title("Current Flight")
                        .icon(getIcon(null)));
            }
        });

    }

    @Override
    public void onError(AirMapException e) {
        Log.e("TrafficAlertActivity", "Error getting current flight: " + e.getMessage());
    }
}