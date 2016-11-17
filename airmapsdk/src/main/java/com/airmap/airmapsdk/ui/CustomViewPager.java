package com.airmap.airmapsdk.ui;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by Vansh Gandhi on 11/16/16.
 * Copyright © 2016 AirMap, Inc. All rights reserved.
 */

/**
 * Prevent ViewPager from consuming any touch events when isPagingEnabled is false. This prevents buggy behavior when
 * dragging annotations in the FreehandMap. However, this also prevents you in between screens
 * in the actual flight screens.
 */
public class CustomViewPager extends ViewPager {

    private boolean isPagingEnabled = false;

    public CustomViewPager(Context context) {
        super(context);
    }

    public CustomViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return this.isPagingEnabled && super.onInterceptTouchEvent(ev);
    }

    public void setPagingEnabled(boolean b) {
        this.isPagingEnabled = b;
    }
}
