package com.airmap.airmapsdk.models;

import com.mapbox.mapboxsdk.annotations.Polygon;
import com.mapbox.mapboxsdk.annotations.Polyline;
import com.mapbox.mapboxsdk.geometry.LatLng;

/**
 * Created by Vansh Gandhi on 10/30/16.
 * Copyright © 2016 AirMap, Inc. All rights reserved.
 */

public class CircleContainer implements Container {
    public Polygon circle;
    public Polyline outline;
    public LatLng center;
    public double radius;

    @Override
    public void clear() {
        circle = null;
        outline = null;
    }

    @Override
    public boolean isValid() {
        return circle != null && outline != null;
    }
}
