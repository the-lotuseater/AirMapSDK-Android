package com.airmap.airmapsdk.ui.views;

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

public class ClickableDrawableButton extends Button {
    private Drawable drawableRight;
    private Drawable drawableLeft;
    private Drawable drawableTop;
    private Drawable drawableBottom;
    private DrawableClickListener drawableClickListener;

    public ClickableDrawableButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public ClickableDrawableButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ClickableDrawableButton(Context context) {
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

            //Check drawable bottom click
            if (drawableBottom != null && drawableBottom.getBounds().contains(actionX, actionY)) {
                if (drawableClickListener != null) {
                    drawableClickListener.onDrawableClick();
                    event.setAction(MotionEvent.ACTION_CANCEL);
                    setSelected(false);
                }
                return true;
            }

            //Check drawable top click
            if (drawableTop != null && drawableTop.getBounds().contains(actionX, actionY)) {
                if (drawableClickListener != null) {
                    drawableClickListener.onDrawableClick();
                    event.setAction(MotionEvent.ACTION_CANCEL);
                    setSelected(false);
                }
                return true;
            }

            //Check drawable left click
            if (drawableLeft != null) {
                int extraTapArea = (int) (13 * getResources().getDisplayMetrics().density + 0.5);
                int x, y;
                x = actionX;
                y = actionY;
                bounds = drawableLeft.getBounds();

                if (!bounds.contains(actionX, actionY)) {
                    x = (actionX - extraTapArea);
                    y = (actionY - extraTapArea);
                    if (x <= 0) x = actionX;
                    if (y <= 0) y = actionY;
                    if (x < y) y = x;
                }

                if (bounds.contains(x, y)) {
                    if (drawableClickListener != null) {
                        drawableClickListener.onDrawableClick();
                        event.setAction(MotionEvent.ACTION_CANCEL);
                        setSelected(false);
                    }
                    return true;
                }
            }

            //Drawable right click
            if (drawableRight != null) {
                bounds = drawableRight.getBounds();
                int x, y;
                int extraTapArea = 13;
                //Increase the tappable area
                x = (actionX + extraTapArea);
                y = (actionY - extraTapArea);
                x = getWidth() - x;
                if (x <= 0) x += extraTapArea;
                if (y <= 0) y = actionY;

                if (bounds.contains(x, y)) {
                    if (drawableClickListener != null) {
                        drawableClickListener.onDrawableClick(); //Handle the click event
                        event.setAction(MotionEvent.ACTION_CANCEL);
                        setSelected(false);
                    }
                    return true;
                }
            }
        }

        return super.onTouchEvent(event);
    }

    public interface DrawableClickListener {
        /**
         * Called when user clicks on one of the buttons drawables
         * TODO: Pass which drawable was clicked
         */
        void onDrawableClick();
    }
}