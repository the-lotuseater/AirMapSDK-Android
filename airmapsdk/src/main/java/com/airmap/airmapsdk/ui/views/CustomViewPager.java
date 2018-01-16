package com.airmap.airmapsdk.ui.views;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Prevent ViewPager from consuming any touch events when isPagingEnabled is false. This prevents
 * buggy behavior when dragging annotations in the FreehandMap.
 */
public class CustomViewPager extends ViewPager {

    private boolean isPagingEnabled = false;

    public CustomViewPager(Context context) {
        super(context);
    }

    public CustomViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * @return True if the event was handled, false otherwise
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return isPagingEnabled && super.onTouchEvent(event);
    }

    /**
     * @return True if the event was handled, false otherwise
     */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        return isPagingEnabled && super.onInterceptTouchEvent(event);
    }

    /**
     * This method should be called to enable or disable swiping between pages in the viewpager
     *
     * @param enable Whether to enable or disable swiping
     */
    public void setPagingEnabled(boolean enable) {
        this.isPagingEnabled = enable;
    }
}
