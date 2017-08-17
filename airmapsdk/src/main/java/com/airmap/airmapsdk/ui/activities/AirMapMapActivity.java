package com.airmap.airmapsdk.ui.activities;

import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;

import com.airmap.airmapsdk.models.Coordinate;
import com.airmap.airmapsdk.models.map.MapStyle;
import com.airmap.airmapsdk.models.shapes.AirMapGeometry;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;

import org.json.JSONObject;

import java.util.List;

/**
 * Created by collin@airmap.com on 5/15/17.
 */

public abstract class AirMapMapActivity extends AppCompatActivity {

    public abstract MapboxMap getMap();

    public abstract MapView getMapView();

    public abstract void createFlight(final Coordinate coordinate);

    public abstract void removeMapLayers(String sourceId, List<String> layerIds);

    public abstract void addMapLayers(String sourceId, List<String> layerIds);

    public abstract void onFlightGeometryChanged(Coordinate takeoffCoordinate, float buffer, JSONObject geometry);

    public abstract MapStyle getMapStyle();
}
