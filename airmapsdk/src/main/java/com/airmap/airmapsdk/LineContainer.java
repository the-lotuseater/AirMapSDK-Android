package com.airmap.airmapsdk;

import com.mapbox.mapboxsdk.annotations.Polyline;

/**
 * Created by Vansh Gandhi on 10/30/16.
 * Copyright © 2016 AirMap, Inc. All rights reserved.
 */

public class LineContainer implements Container {
    public Polyline line;
    public Polyline widthPolyline;
    public int width;


    @Override
    public void clear() {
        line = null;
        widthPolyline = null;
        width = -1;
    }

    @Override
    public boolean isValid() {
        return line != null && widthPolyline != null && width > 0;
    }
}
