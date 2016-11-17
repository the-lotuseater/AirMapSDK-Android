package com.airmap.airmapsdk.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by Vansh Gandhi on 10/30/16.
 * Copyright © 2016 AirMap, Inc. All rights reserved.
 *
 * This view is used to draw the dotted lines when adjusting points
 */

public class Scratchpad extends View {

    private Canvas canvas;
    private Paint paint;
    private Path path;

    public Scratchpad(Context context) {
        super(context);
    }

    public Scratchpad(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public Scratchpad(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    {
        paint = new Paint();
        paint.setColor(0xFF000000);
        paint.setAntiAlias(true);
        paint.setStrokeWidth(7);
        paint.setStyle(Paint.Style.STROKE);
        paint.setPathEffect(new DashPathEffect(new float[]{25, 30}, 0));
        paint.setStrokeJoin(Paint.Join.ROUND);
        canvas = new Canvas();
        path = new Path();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawPath(path, paint);
    }

    public void dragTo(PointF start, PointF middle, PointF end) {
        reset();
        path.moveTo(start.x, start.y);
        path.lineTo(middle.x, middle.y);
        path.lineTo(end.x, end.y);
        canvas.drawPath(path, paint);
        invalidate();
    }

    public void dragTo(PointF start, PointF end) {
        reset();
        path.moveTo(start.x, start.y);
        path.lineTo(end.x, end.y);
        canvas.drawPath(path, paint);
        invalidate();
    }

    public void reset() {
        path.reset();
    }
}
