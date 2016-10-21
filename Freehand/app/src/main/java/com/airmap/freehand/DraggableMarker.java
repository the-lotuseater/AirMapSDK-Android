package com.airmap.freehand;

import android.graphics.PointF;
import android.view.MotionEvent;

import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.geometry.ILatLng;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.Projection;

/**
 * Created by Vansh Gandhi on 10/11/16.
 * Copyright © 2016 AirMap, Inc. All rights reserved.
 */

public class DraggableMarker extends Marker {

    private boolean mIsDragged;
    private float mDx, mDy;

    public DraggableMarker(DraggableMarkerOptions options) {
        super(options);
        mIsDragged = false;
    }

    public boolean drag(MotionEvent event) {
        final int action = event.getActionMasked();
        if (action == MotionEvent.ACTION_DOWN) {
            Projection pj = mapboxMap.getProjection();
//            RectF bound = getDrawingBounds(pj, mTempRect);
//            if(bound.contains(event.getX(), event.getY())) {
            mIsDragged = true;
            PointF p = pj.toScreenLocation(getPosition());
            mDx = p.x - event.getX();
            mDy = p.y - event.getY();
//            }
        }
        if (mIsDragged) {
            if ((action == MotionEvent.ACTION_CANCEL) ||
                    (action == MotionEvent.ACTION_UP)) {
                mIsDragged = false;
            } else {
                Projection pj = mapboxMap.getProjection();
                ILatLng pos = pj.fromScreenLocation(new PointF(event.getX() + mDx, event.getY() + mDy));
                setPosition(new LatLng(pos.getLatitude(), pos.getLongitude()));
            }
        }

        return mIsDragged;
    }
}