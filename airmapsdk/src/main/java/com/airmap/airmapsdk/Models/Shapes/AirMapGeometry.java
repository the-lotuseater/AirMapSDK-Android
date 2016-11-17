package com.airmap.airmapsdk.models.shapes;

import com.airmap.airmapsdk.models.Coordinate;
import com.mapbox.mapboxsdk.geometry.LatLng;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Vansh Gandhi on 7/26/16.
 * Copyright © 2016 AirMap, Inc. All rights reserved.
 */
@SuppressWarnings("unused")
public abstract class AirMapGeometry implements Serializable {
    public static AirMapGeometry getGeometryFromGeoJSON(JSONObject geoJson) {
        if (geoJson != null) {
            String geoType = geoJson.optString("type").toLowerCase();
            if (geoType.equals("polygon")) {
                List<Coordinate> coordinates = new ArrayList<>();
                JSONArray coordArray = geoJson.optJSONArray("coordinates").optJSONArray(0);
                if (coordArray == null) return null;
                for (int i = 0; i < coordArray.length(); i++) {
                    JSONArray coordinate = coordArray.optJSONArray(i);
                    if (coordinate == null) continue;
                    LatLng temp = new LatLng(coordinate.optDouble(1), coordinate.optDouble(0)); //Comes in in lng,lat form
                    coordinates.add(new Coordinate(temp)); //This wraps the coordinate
                }
                return new AirMapPolygon(coordinates);
            } else if (geoType.equals("linestring")) {
                List<Coordinate> coordinates = new ArrayList<>();
                JSONArray coordArray = geoJson.optJSONArray("coordinates").optJSONArray(0);
                if (coordArray == null) return null;
                for (int i = 0; i < coordArray.length(); i++) {
                    JSONArray coordinate = coordArray.optJSONArray(i);
                    if (coordinate == null) continue;
                    LatLng temp = new LatLng(coordinate.optDouble(1), coordinate.optDouble(0)); //Comes in in lng,lat form
                    coordinates.add(new Coordinate(temp)); //This wraps the coordinate
                }
                return new AirMapPath(coordinates);
            } else if (geoType.equals("point")) {
                JSONArray coordinate = geoJson.optJSONArray("coordinates");
                if (coordinate == null) return null;
                LatLng temp = new LatLng(coordinate.optDouble(1), coordinate.optDouble(0)); //Comes in in lng,lat form
                return new AirMapPoint(new Coordinate(temp));
            }
        }
        return null;
    }
}
