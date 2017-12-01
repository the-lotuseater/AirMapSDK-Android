package com.airmap.airmapsdk.models;

import com.mapbox.mapboxsdk.annotations.Polygon;
import com.mapbox.mapboxsdk.annotations.Polyline;
import com.mapbox.mapboxsdk.annotations.PolylineOptions;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Vansh Gandhi on 10/30/16.
 * Copyright © 2016 AirMap, Inc. All rights reserved.
 */

public class LineContainer implements Container {
    public Polyline line;
    public List<Polygon> buffers;
    public double width;


    @Override
    public void clear() {
        line = new PolylineOptions().getPolyline();
        buffers = new ArrayList<>();
        width = -1;
    }

    @Override
    public boolean isValid() {
        return line != null && buffers != null && !buffers.isEmpty() && width > 0;
    }
}
