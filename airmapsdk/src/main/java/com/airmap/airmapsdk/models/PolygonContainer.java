package com.airmap.airmapsdk.models;

import com.mapbox.mapboxsdk.annotations.Polygon;
import com.mapbox.mapboxsdk.annotations.Polyline;

/**
 * Created by Vansh Gandhi on 10/30/16.
 * Copyright © 2016 AirMap, Inc. All rights reserved.
 */

public class PolygonContainer implements Container {
    public Polygon polygon;
    public Polyline outline;

    @Override
    public void clear() {
        polygon = null;
        outline = null;
    }

    @Override
    public boolean isValid() {
        return polygon != null && outline != null;
    }
}
