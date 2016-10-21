package com.airmap.freehand;

import android.graphics.PointF;
import android.support.annotation.NonNull;

import java.util.List;

/**
 * Created by Vansh Gandhi on 9/24/16.
 * Copyright © 2016 AirMap, Inc. All rights reserved.
 *
 */

interface DrawingCallback {
    void doneDrawing(@NonNull List<PointF> shape);
}