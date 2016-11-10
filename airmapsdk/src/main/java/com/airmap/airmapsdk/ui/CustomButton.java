package com.airmap.airmapsdk.ui;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.Button;

/**
 * Created by Vansh Gandhi on 11/3/16.
 * Copyright © 2016 AirMap, Inc. All rights reserved.
 *
 * Custom Button to handle drawableLeft/Right/Top/Bottom clicks
 */

public class CustomButton extends Button {
    private Drawable drawableRight;
    private Drawable drawableLeft;
    private Drawable drawableTop;
    private Drawable drawableBottom;
    private Rect rBounds;
    DrawableClickListener drawableClickListener;

    public CustomButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public CustomButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomButton(Context context) {
        super(context);
    }

    public void setDrawableClickListener(DrawableClickListener drawableClickListener) {
        this.drawableClickListener = drawableClickListener;
    }

    @Override
    public void setCompoundDrawables(Drawable left, Drawable top,
                                     Drawable right, Drawable bottom) {
        if (left != null) {
            drawableLeft = left;
        }
        if (right != null) {
            drawableRight = right;
        }
        if (top != null) {
            drawableTop = top;
        }
        if (bottom != null) {
            drawableBottom = bottom;
        }
        super.setCompoundDrawables(left, top, right, bottom);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Rect bounds;
        if (event.getAction() == MotionEvent.ACTION_UP) {
            final int actionX = (int) event.getX();
            final int actionY = (int) event.getY();
            if (drawableBottom != null
                    && drawableBottom.getBounds().contains(actionX, actionY)) {
                if (drawableClickListener != null) {
                    drawableClickListener.onDrawableClick();
                    event.setAction(MotionEvent.ACTION_CANCEL);
                    setSelected(false);
                }
                return super.onTouchEvent(event);
            }

            if (drawableTop != null
                    && drawableTop.getBounds().contains(actionX, actionY)) {
                if (drawableClickListener != null) {
                    drawableClickListener.onDrawableClick();
                    event.setAction(MotionEvent.ACTION_CANCEL);
                    setSelected(false);
                }
                return super.onTouchEvent(event);
            }

            // this works for left since container shares 0,0 origin with bounds
            if (drawableLeft != null) {
                bounds = drawableLeft.getBounds();

                int x, y;
                int extraTapArea = (int) (13 * getResources().getDisplayMetrics().density + 0.5);

                x = actionX;
                y = actionY;

                if (!bounds.contains(actionX, actionY)) {
                    /** Gives the +20 area for tapping. */
                    x = (actionX - extraTapArea);
                    y = (actionY - extraTapArea);

                    if (x <= 0)
                        x = actionX;
                    if (y <= 0)
                        y = actionY;

                    /** Creates square from the smallest value */
                    if (x < y) {
                        y = x;
                    }
                }

                if (bounds.contains(x, y)) {
                    if (drawableClickListener != null) {
                        drawableClickListener.onDrawableClick();
                        event.setAction(MotionEvent.ACTION_CANCEL);
                        setSelected(false);
                    }
                    return false;

                }
            }

            if (drawableRight != null) {
                bounds = drawableRight.getBounds();
                int x, y;
                int extraTapArea = 13;
                /**
                 * IF USER CLICKS JUST OUT SIDE THE RECTANGLE OF THE DRAWABLE
                 * THAN ADD X AND SUBTRACT THE Y WITH SOME VALUE SO THAT AFTER
                 * CALCULATING X AND Y CO-ORDINATE LIES INTO THE DRAWBABLE
                 * BOUND. - this process help to increase the tappable area of
                 * the rectangle.
                 */
                x = (actionX + extraTapArea);
                y = (actionY - extraTapArea);

                /**Since this is right drawable subtract the value of x from the widthPolyline
                 * of view. so that widthPolyline - tappedarea will result in x co-ordinate in drawable bound.
                 */
                x = getWidth() - x;

                 /*x can be negative if user taps at x co-ordinate just near the widthPolyline.
                 * e.g views widthPolyline = 300 and user taps 290. Then as per previous calculation
                 * 290 + 13 = 303. So subtract X from getWidth() will result in negative value.
                 * So to avoid this add the value previous added when x goes negative.
                 */

                if (x <= 0) {
                    x += extraTapArea;
                }

                 /* If result after calculating for extra tappable area is negative.
                 * assign the original value so that after subtracting
                 * extratapping area value doesn't go into negative value.
                 */

                if (y <= 0)
                    y = actionY;

                /**If drawble bounds contains the x and y points then move ahead.*/
                if (bounds.contains(x, y)) {
                    if (drawableClickListener != null) {
                        drawableClickListener.onDrawableClick();
                        event.setAction(MotionEvent.ACTION_CANCEL);
                        setSelected(false);
                    }
                    return false;
                }
                return super.onTouchEvent(event);
            }
        }
        return super.onTouchEvent(event);
    }

    interface DrawableClickListener {
        void onDrawableClick();
    }
}