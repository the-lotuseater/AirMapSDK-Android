package com.airmap.airmapsdk.models;

import com.mapbox.mapboxsdk.annotations.Polygon;
import com.mapbox.mapboxsdk.annotations.Polyline;

/**
 * Created by Vansh Gandhi on 10/30/16.
 * Copyright © 2016 AirMap, Inc. All rights reserved.
 */

public class LineContainer implements Container {
    public Polyline line;
    public Polygon buffer;
    public double width;


    @Override
    public void clear() {
        line = null;
        buffer = null;
        width = -1;
    }

    @Override
    public boolean isValid() {
        return line != null && buffer != null && width > 0;
    }
}
