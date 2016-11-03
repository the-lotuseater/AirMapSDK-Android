package com.airmap.airmapsdk;

import com.mapbox.mapboxsdk.annotations.Polyline;

/**
 * Created by Vansh Gandhi on 10/30/16.
 * Copyright © 2016 AirMap, Inc. All rights reserved.
 */

public class LineContainer implements Container {
    public Polyline line;
    public Polyline width;

    @Override
    public void clear() {
        line = null;
        width = null;
    }

    @Override
    public boolean isValid() {
        return line != null && width != null;
    }
}
