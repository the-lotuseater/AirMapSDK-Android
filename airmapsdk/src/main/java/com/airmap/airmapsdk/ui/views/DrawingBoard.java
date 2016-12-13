package com.airmap.airmapsdk.ui.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.airmap.airmapsdk.DrawingCallback;
import com.airmap.airmapsdk.util.PointMath;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Vansh Gandhi on 9/22/16.
 * Copyright © 2016 AirMap, Inc. All rights reserved.
 *
 * This View allows the user to draw a shape. Once the user is done drawing, it notifies the
 * DrawingCallback, if one has been set
 */

public class DrawingBoard extends View {

    private Path drawPath;
    private Paint drawPaint;
    private Canvas drawCanvas;
    private List<PointF> points;
    private DrawingCallback callback;
    private boolean isPolygonMode;

    public DrawingBoard(Context context) {
        super(context);
    }

    public DrawingBoard(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DrawingBoard(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    //Static initializer block. It is called after the constructor has run
    {
        drawPath = new Path();
        drawPaint = new Paint();
        drawPaint.setColor(0xFF000000);
        drawPaint.setAntiAlias(true);
        drawPaint.setStrokeWidth(7);
        drawPaint.setStyle(Paint.Style.STROKE);
        drawPaint.setPathEffect(new DashPathEffect(new float[]{25, 30}, 0));
        drawPaint.setStrokeJoin(Paint.Join.ROUND);
        drawCanvas = new Canvas();
        points = new ArrayList<>();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawPath(drawPath, drawPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float touchX = event.getX();
        float touchY = event.getY();
        PointF point = new PointF(touchX, touchY);
        boolean doneDrawing = false;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                drawPath.reset();
                points.clear();
                drawPath.moveTo(touchX, touchY);
                points.add(point);
                break;
            case MotionEvent.ACTION_MOVE:
                drawPath.lineTo(touchX, touchY);
                points.add(point);
                break;
            case MotionEvent.ACTION_UP:
                drawCanvas.drawPath(drawPath, drawPaint);
                drawPath.reset();
                doneDrawing = true;
                break;
            default:
                return false;
        }
        if (doneDrawing) {
            simplifyPathAndInvokeCallback();
        }
        invalidate();
        return true;
    }

    private void simplifyPathAndInvokeCallback() {
        List<PointF> reduced = PointMath.simplify(points, 50);
        if (reduced != null && !reduced.isEmpty() && callback != null) {
            //Sometimes the path will have duplicate points. We want to get rid of them, otherwise Mapbox spazs out
            List<PointF> noDuplicates = removeDuplicates(reduced);
            if (noDuplicates != null && noDuplicates.size() >= (isPolygonMode() ? 3 : 2)) {
                callback.doneDrawing(removeDuplicates(reduced));
            }
        }
    }

    /**
     * Set the callback to be invoked after the user finishes drawing a shape
     * @param callback
     */
    public void setDoneDrawingCallback(DrawingCallback callback) {
        this.callback = callback;
    }

    /**
     * Set whether the user will be drawing a polygon or a line
     * @param isPolygon Whether the user will be drawing a polygon
     */
    public void setPolygonMode(boolean isPolygon) {
        isPolygonMode = isPolygon;
    }

    /**
     * @return Whether the user is drawing polygons or paths
     */
    public boolean isPolygonMode() {
        return isPolygonMode;
    }

    /**
     * Remove duplicate points, otherwise MapBox Polygons spaz out
     *
     * @param duplicates List with duplicates
     * @return List without duplicates
     */
    private List<PointF> removeDuplicates(List<PointF> duplicates) {
        List<PointF> noDuplicates = new ArrayList<>();
        for (PointF point : duplicates) {
            if (!noDuplicates.contains(point)) {
                noDuplicates.add(point);
            }
        }
        return noDuplicates;
    }
}