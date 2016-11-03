package com.airmap.airmapsdk.ui.activities;

import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.annotation.UiThread;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.airmap.airmapsdk.AirMapException;
import com.airmap.airmapsdk.CircleContainer;
import com.airmap.airmapsdk.DrawingCallback;
import com.airmap.airmapsdk.LineContainer;
import com.airmap.airmapsdk.PointMath;
import com.airmap.airmapsdk.R;
import com.airmap.airmapsdk.Utils;
import com.airmap.airmapsdk.models.Coordinate;
import com.airmap.airmapsdk.models.PolygonContainer;
import com.airmap.airmapsdk.models.status.AirMapStatus;
import com.airmap.airmapsdk.networking.callbacks.AirMapCallback;
import com.airmap.airmapsdk.networking.services.AirMap;
import com.airmap.airmapsdk.ui.DrawingBoard;
import com.airmap.airmapsdk.ui.ImageViewSwitch;
import com.airmap.airmapsdk.ui.Scratchpad;
import com.mapbox.mapboxsdk.MapboxAccountManager;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerView;
import com.mapbox.mapboxsdk.annotations.MarkerViewOptions;
import com.mapbox.mapboxsdk.annotations.PolygonOptions;
import com.mapbox.mapboxsdk.annotations.PolylineOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.services.commons.geojson.LineString;
import com.mapbox.services.commons.models.Position;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.ButterKnife;

import static com.airmap.airmapsdk.Utils.getBufferPresets;

/**
 * Created by Vansh Gandhi on 11/3/16.
 * Copyright © 2016 AirMap, Inc. All rights reserved.
 */

public class FreehandMapActivity extends AppCompatActivity implements OnMapReadyCallback,
        DrawingCallback, MapboxMap.OnCameraChangeListener, AirMapCallback<AirMapStatus> {

    private static final String CIRCLE_TAG = "circle";
    private static final String PATH_TAG = "path";
    private static final String POLYGON_TAG = "polygon";
    private static final String MIDPOINT_TAG = "midpoint";
    private static final String CORNER_TAG = "corner";

    private static Icon draggerIcon;
    private static Icon addCircleIcon;
    private static Icon intersectionIcon;

    private Toolbar toolbar;
    private TabLayout tabLayout;
    private RelativeLayout seekBarContainer;
    private SeekBar seekBar;
    private TextView seekBarLabelTextView;
    private TextView seekBarValueTextView;
    private ImageViewSwitch enableDrawingSwitch;
    private ImageView deleteButton;
    private TextView tipTextView;
    private MapView mapView;
    private DrawingBoard drawingBoard;
    private Scratchpad scratchpad;
    private Button nextButton;

    private MapboxMap map;

    private CircleContainer circleContainer;
    private PolygonContainer polygonContainer;
    private LineContainer lineContainer;
    private List<MarkerView> corners;
    private List<MarkerView> midpoints;
    private List<MarkerView> intersections; //Store this in the Polygon class? since this is only relevant for polygon
    private double zoomAtDraw = -1;
    private float screenDensity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MapboxAccountManager.start(this, Utils.getMapboxApiKey());
        AirMap.init(this);
        setContentView(R.layout.airmap_activity_freehand);
        initializeViews();
        ButterKnife.bind(this);
        setupToolbar();
        setupSwitch();
        setupMap(savedInstanceState);
        setupButtons();
        drawingBoard.setDoneDrawingCallback(this);

        circleContainer = new CircleContainer();
        lineContainer = new LineContainer();
        polygonContainer = new PolygonContainer();

        corners = new ArrayList<>();
        midpoints = new ArrayList<>();
        intersections = new ArrayList<>();
        draggerIcon = IconFactory.getInstance(this).fromResource(R.drawable.white_circle);
        addCircleIcon = IconFactory.getInstance(this).fromResource(R.drawable.gray_circle);
        intersectionIcon = IconFactory.getInstance(this).fromResource(R.drawable.intersection_circle);

        screenDensity = getResources().getDisplayMetrics().density;
    }

    private void initializeViews() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        tabLayout = (TabLayout) findViewById(R.id.tabs);
        seekBarContainer = (RelativeLayout) findViewById(R.id.seekbar_container);
        seekBar = (SeekBar) findViewById(R.id.seekbar);
        seekBarLabelTextView = (TextView) findViewById(R.id.label);
        seekBarValueTextView = (TextView) findViewById(R.id.seekbar_value);
        enableDrawingSwitch = (ImageViewSwitch) findViewById(R.id.action_button);
        deleteButton = (ImageView) findViewById(R.id.delete_button);
        tipTextView = (TextView) findViewById(R.id.tip_text);
        mapView = (MapView) findViewById(R.id.map);
        drawingBoard = (DrawingBoard) findViewById(R.id.drawFrame);
        scratchpad = (Scratchpad) findViewById(R.id.scratchpad);
        nextButton = (Button) findViewById(R.id.next_button);
    }

    private void setupToolbar() {
        toolbar.setTitle(R.string.airmap_create_flight);
        setSupportActionBar(toolbar);
    }

    private void setupMap(Bundle savedInstanceState) {
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
    }

    private void setupTabs() {
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                clear();
                String tag = (String) tab.getTag();
                if (CIRCLE_TAG.equals(tag)) {
                    showSeekBarForCircle();
                    tipTextView.setVisibility(View.GONE);
                    drawingBoard.setPolygonMode(true); //Closes the drawn path
                    drawingBoard.setClickable(false);
                    drawingBoard.setVisibility(View.GONE);
                    enableDrawingSwitch.setVisibility(View.GONE);
                    deleteButton.setVisibility(View.GONE);
                    enableDrawingSwitch.setVisibility(View.GONE);
                } else if (PATH_TAG.equals(tag)) {
                    showSeekBarForPath();
                    updateTip(R.string.airmap_freehand_tip);
                    drawingBoard.setPolygonMode(false); //Doesn't close the drawn path
                    drawingBoard.setVisibility(View.VISIBLE);
                    enableDrawingSwitch.setVisibility(View.VISIBLE);
                    enableDrawingSwitch.setChecked(false);
                    showDeleteButton(false);
                } else if (POLYGON_TAG.equals(tag)) {
                    hideSeekBar();
                    updateTip(R.string.airmap_freehand_tip);
                    drawingBoard.setPolygonMode(true);
                    drawingBoard.setVisibility(View.VISIBLE);
                    enableDrawingSwitch.setChecked(false);
                    enableDrawingSwitch.setVisibility(View.VISIBLE);
                    showDeleteButton(false);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        //Add tabs after listener so that the callback is invoked
        tabLayout.addTab(getTab(tabLayout, R.string.airmap_circle_radius, R.drawable.ic_circle, CIRCLE_TAG));
        tabLayout.addTab(getTab(tabLayout, R.string.airmap_path_buffer, R.drawable.ic_path, PATH_TAG));
        tabLayout.addTab(getTab(tabLayout, R.string.airmap_polygon, R.drawable.ic_polygon, POLYGON_TAG));
    }

    private void setupSwitch() {
        enableDrawingSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                drawingBoard.setVisibility(isChecked ? View.VISIBLE : View.INVISIBLE);
                if (isChecked) {
                    if (drawingBoard.isPolygonMode()) {
                        updateTip(R.string.airmap_draw_freehand_shape);
                    } else {
                        updateTip(R.string.airmap_draw_freehand_path);
                    }
                } else {
                    updateTip(R.string.airmap_freehand_tip);
                }
            }
        });
    }

    private void setupButtons() {
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clear();
                showDeleteButton(false);
                if (tabLayout.getSelectedTabPosition() == 2) { //Reset in case we displayed the intersection error message
                    updateTip(R.string.airmap_freehand_tip);
                }
            }
        });

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (tabLayout.getSelectedTabPosition() == 0 && circleContainer.isValid()) {
//            circleContainer.center;
//            circleContainer.radius;
                } else if (tabLayout.getSelectedTabPosition() == 1 && lineContainer.isValid()) {
                    //Path
                    List<Position> positions = new ArrayList<>();
                    for (LatLng latLng : lineContainer.line.getPoints()) {
                        positions.add(Position.fromCoordinates(latLng.getLongitude(), latLng.getLatitude()));
                    }
                    LineString line = LineString.fromCoordinates(positions);
                    String json = line.toJson(); //TODO: Verify this is in the correct format
                } else if (tabLayout.getSelectedTabPosition() == 2 && polygonContainer.isValid()) {
                    if (!PointMath.findIntersections(polygonContainer.polygon.getPoints()).isEmpty()) {
                        Toast.makeText(FreehandMapActivity.this, R.string.airmap_error_overlap, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    List<List<Position>> positions = new ArrayList<>();
                    positions.add(new ArrayList<Position>());
                    for (LatLng latLng : polygonContainer.polygon.getPoints()) {
                        positions.get(0).add(Position.fromCoordinates(latLng.getLongitude(), latLng.getLatitude()));
                    }
                    com.mapbox.services.commons.geojson.Polygon polygon = com.mapbox.services.commons.geojson.Polygon.fromCoordinates(positions);
                    String json = polygon.toJson(); //TODO: Verify this is in the correct format
                }
            }
        });
    }

    public void clear() {
        zoomAtDraw = -1;
        map.removeAnnotations();
        circleContainer.clear();
        lineContainer.clear();
        polygonContainer.clear();
        updateButtonColor(null);
        if (corners != null && midpoints != null) {
            map.removeAnnotations(corners);
            map.removeAnnotations(midpoints);
            corners.clear();
            midpoints.clear();
        }
    }

    public void clearMidpoints() {
        if (midpoints != null) {
            map.removeAnnotations(midpoints);
            midpoints.clear();
        }
    }

    @Override
    public void doneDrawing(@NonNull List<PointF> points) {
        if (!points.isEmpty()) {
            PointF copy = new PointF(points.get(0).x, points.get(0).y);
            Coordinate coordinate = new Coordinate(map.getProjection().fromScreenLocation(copy));
            AirMap.checkCoordinate(coordinate, null, null, null, false, null, this);
        }
        showDeleteButton(true);
        enableDrawingSwitch.setChecked(false);
        updateTip(R.string.airmap_done_drawing_tip);
        if (drawingBoard.isPolygonMode()) {
            drawPolygon(points);
        } else { //Path mode
            drawPath(points);
        }
        zoomAtDraw = map.getCameraPosition().zoom;
    }

    private void drawCircle(LatLng center, double radius) {
        clear();
        List<LatLng> circlePoints = polygonCircleForCoordinate(center, radius);
        PolylineOptions polylineOptions = getDefaultPolylineOptions().addAll(circlePoints).add(circlePoints.get(0));
        circleContainer.circle = map.addPolygon(getDefaultPolygonOptions().addAll(circlePoints));
        circleContainer.outline = map.addPolyline(polylineOptions);
        circleContainer.radius = radius;
        circleContainer.center = center;
        corners.add(map.addMarker(getDefaultMarkerOptions(center))); //Treat the center of the circle as a "corner", cuz it's not a midpoint
    }

    private void zoomToCircle() {
        if (circleContainer.circle != null) {
            LatLngBounds bounds = new LatLngBounds.Builder().includes(circleContainer.circle.getPoints()).build();
            map.easeCamera(CameraUpdateFactory.newLatLngBounds(bounds, 200));
        }
    }

    public void drawPath(List<PointF> line) {
        clear();
        PolylineOptions thickLine = new PolylineOptions();
        PolylineOptions thinLine = getDefaultPolylineOptions();
        thickLine.color(ContextCompat.getColor(this, R.color.airmap_colorFill));
        thickLine.alpha(0.66f);
        thickLine.width(getPathWidth(seekBar.getProgress()));
        List<LatLng> midPoints = getMidpointsFromLatLngs(getLatLngsFromPointFs(line));
        List<LatLng> points = getLatLngsFromPointFs(line);
        for (int i = 0; i < points.size(); i++) {
            LatLng point = points.get(i);
            thickLine.add(point);
            thinLine.add(point);
            corners.add(map.addMarker(getDefaultMarkerOptions(point)));
            if (i < midPoints.size()) {
                LatLng midPoint = midPoints.get(i);
                midpoints.add(map.addMarker(getDefaultMidpointMarker(midPoint)));
            }
        }

        //TODO: Rounded corners at the end of the line
        lineContainer.width = map.addPolyline(thickLine);
        lineContainer.line = map.addPolyline(thinLine);

        LatLngBounds bounds = new LatLngBounds.Builder().includes(thickLine.getPoints()).build();
        map.easeCamera(CameraUpdateFactory.newLatLngBounds(bounds, 150));
    }

    public void drawPolygon(List<PointF> pointsDrawn) {
        clear();
        PointF first = new PointF(pointsDrawn.get(0).x, pointsDrawn.get(0).y); //Making copy of the object so MapBox doesn't mess up the point
        PointF last = new PointF(pointsDrawn.get(pointsDrawn.size() - 1).x, pointsDrawn.get(pointsDrawn.size() - 1).y); //Making copies so MapBox doesn't mess up the point
        if (PointMath.distanceBetween(first, last) < 100) { //Check if the two points are close, based on screen distance between points, rather than LatLng distance
            pointsDrawn.set(pointsDrawn.size() - 1, first); //Ignore the last drawn point if it was close to the starting point
        } else {
            pointsDrawn.add(first);
        }
        PolygonOptions polygonOptions = getDefaultPolygonOptions();
        PolylineOptions polylineOptions = getDefaultPolylineOptions();
        List<LatLng> midPoints = getMidpointsFromLatLngs(getLatLngsFromPointFs(pointsDrawn));
        List<LatLng> points = getLatLngsFromPointFs(pointsDrawn);
        //At this point, until MapBox fixes their fromScreenLocation bug, pointsDrawn has been tainted and is unusable
        for (int i = 0; i < points.size(); i++) {
            LatLng latLng = points.get(i);
            if (i < midPoints.size()) { //There is one less midpoint than total points
                LatLng midPoint = midPoints.get(i);
                midpoints.add(map.addMarker(getDefaultMidpointMarker(midPoint)));
            }
            polygonOptions.add(latLng);
            polylineOptions.add(latLng);
            if (i != points.size() - 1) { //Don't add the last point because it's the same as the first point
                corners.add(map.addMarker(getDefaultMarkerOptions(latLng)));
            }
        }
        polylineOptions.add(polylineOptions.getPoints().get(0)); //Close the polygon
        polygonContainer.polygon = map.addPolygon(polygonOptions);
        polygonContainer.outline = map.addPolyline(polylineOptions);

        LatLngBounds bounds = new LatLngBounds.Builder().includes(polylineOptions.getPoints()).build();
        map.easeCamera(CameraUpdateFactory.newLatLngBounds(bounds, 150));
        checkForIntersections(points.subList(0, points.size() - 2)); //Don't include the last point when checking for intersection, otherwise it will always say there is one
    }

    private void checkForIntersections(List<LatLng> pointsDrawn) {
        map.removeAnnotations(intersections);
        intersections.clear();
        List<LatLng> points = PointMath.findIntersections(pointsDrawn);
        if (!points.isEmpty()) {
            for (LatLng point : points) {
                intersections.add(map.addMarker(getIntersectionMarker(point)));
            }
            updateTip(R.string.airmap_error_overlap, R.drawable.rounded_corners_red);
        } else {
            updateTip(R.string.airmap_done_drawing_tip); //If no intersections, then reset to freehand message
        }
    }

    private List<LatLng> getLatLngsFromPointFs(List<PointF> points) {
        List<LatLng> latLngs = new ArrayList<>();
        for (PointF pointF : points) {
            LatLng latLng = map.getProjection().fromScreenLocation(pointF);
            latLngs.add(latLng);
        }
        return latLngs;
    }

    private static int getPathWidth(int progress) {
        //TODO: Turn feet into pixels on screen
        //TODO: As of right now Polyline only seems to support a static width. That is, when zooming in and out, the line remains the same width on screen, which is not what we want. We want the line to represent a measured width
        double feet = Utils.getBufferPresets()[progress].value.doubleValue();
        //Convert feet to width on screen
        return 3 * progress; //This is just returning a dummy value
    }

    private static List<LatLng> getMidpointsFromLatLngs(List<LatLng> shape) {
        List<LatLng> midpoints = new ArrayList<>();
        for (int i = 1; i < shape.size(); i++) {
            double lat = (shape.get(i - 1).getLatitude() + shape.get(i).getLatitude()) / 2;
            double lng = (shape.get(i - 1).getLongitude() + shape.get(i).getLongitude()) / 2;
            midpoints.add(new LatLng(lat, lng));
        }
        return midpoints;
    }

    @Override
    public void onMapReady(MapboxMap mapboxMap) {
        map = mapboxMap;
        map.setOnCameraChangeListener(this);
        map.setOnMarkerClickListener(new MapboxMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(@NonNull Marker marker) {
                return true; //This is simply to prevent opening the info window when selecting the marker from onTouch
            }
        });
        mapView.setOnTouchListener(new View.OnTouchListener() {
            //This onTouch code is a copy of the MapView#onSingleTapConfirmed code, except
            //I'm dragging instead of clicking, and it's being called for every touch event rather than just a tap
            //It also simplifies some of the selection logic
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event != null) {
                    PointF tapPoint = new PointF(event.getX(), event.getY());
                    float toleranceSides = 4 * screenDensity;
                    float toleranceTopBottom = 10 * screenDensity;
                    float averageIconWidth = 42;
                    float averageIconHeight = 42;
                    //TODO: Maybe increase this Rect size for a bigger touch target?
                    RectF tapRect = new RectF((tapPoint.x - averageIconWidth / 2 - toleranceSides) / screenDensity,
                            (tapPoint.y - averageIconHeight / 2 - toleranceTopBottom) / screenDensity,
                            (tapPoint.x + averageIconWidth / 2 + toleranceSides) / screenDensity,
                            (tapPoint.y + averageIconHeight / 2 + toleranceTopBottom) / screenDensity);
                    try {
                        Method method = mapView.getClass().getDeclaredMethod("getMarkersInRect", RectF.class); //Using reflection to access a Mapbox Package Private method
                        method.setAccessible(true);
                        Marker newSelectedMarker = null;
                        List<Marker> nearbyMarkers = (List<Marker>) method.invoke(mapView, tapRect);
                        List<Marker> selectedMarkers = map.getSelectedMarkers();
                        if (selectedMarkers.isEmpty() && nearbyMarkers != null && !nearbyMarkers.isEmpty()) {
                            Collections.sort(nearbyMarkers);
                            newSelectedMarker = nearbyMarkers.get(0);
                        } else if (!selectedMarkers.isEmpty()) {
                            newSelectedMarker = selectedMarkers.get(0);
                        }

                        if (newSelectedMarker != null && newSelectedMarker instanceof MarkerView) {
                            //TODO: Check if the the tapPoint is over the delete button
                            //DRAG!
                            //Trying to put most logic in the drag() function, this is pretty messy already
                            boolean isMidpoint = midpoints.contains(newSelectedMarker);
                            boolean doneDragging = event.getAction() == MotionEvent.ACTION_UP;
                            map.selectMarker(newSelectedMarker); //Use the marker selection state to prevent selecting another marker when dragging over it
                            drag(isMidpoint ? midpoints.indexOf(newSelectedMarker) : corners.indexOf(newSelectedMarker), map.getProjection().fromScreenLocation(tapPoint), isMidpoint, doneDragging);
                            if (doneDragging) {
                                map.deselectMarker(newSelectedMarker);
                            }
                            return true;
                        }
                    } catch (Exception e) {
                        //Reflection exception most likely
                        e.printStackTrace();
                    }
                }
                scratchpad.reset();
                scratchpad.invalidate();
                return false;
            }
        });

        setupTabs();
    }

    private void drag(int indexOfAnnotationToDrag, LatLng newLocation, boolean isMidpoint, boolean doneDragging) {
        //Don't show midpoints and corners when dragging
        for (MarkerView midpoint : midpoints) {
            midpoint.setVisible(doneDragging);
        }
        for (MarkerView corner : corners) {
            corner.setVisible(doneDragging);
        }
        for (MarkerView intersection : intersections) {
            intersection.setVisible(false); //Never want to show intersections when dragging another marker
        }

        if (tabLayout.getSelectedTabPosition() == 0) {
            dragCircle(indexOfAnnotationToDrag, newLocation, doneDragging);
        } else if (tabLayout.getSelectedTabPosition() == 1) {
            dragPointOnLine(indexOfAnnotationToDrag, newLocation, isMidpoint, doneDragging);
        } else if (tabLayout.getSelectedTabPosition() == 2) {
            dragPointOnPolygon(indexOfAnnotationToDrag, newLocation, isMidpoint, doneDragging);
        }
        if (doneDragging) {
            scratchpad.reset();
            scratchpad.invalidate();
        }
    }

    private void dragCircle(int indexOfAnnotationToDrag, LatLng newLocation, boolean doneDragging) {
        map.removeAnnotation(circleContainer.circle);
        map.removeAnnotation(circleContainer.outline);
        corners.get(indexOfAnnotationToDrag).setPosition(newLocation); //Move the center point
        double radius = getBufferPresets()[seekBar.getProgress()].value.doubleValue(); //Move the circle polygon
        List<LatLng> circlePoints = polygonCircleForCoordinate(newLocation, radius);
        PolylineOptions polylineOptions = getDefaultPolylineOptions().addAll(circlePoints).add(circlePoints.get(0));
        circleContainer.circle = map.addPolygon(getDefaultPolygonOptions().addAll(circlePoints));
        circleContainer.outline = map.addPolyline(polylineOptions);
        circleContainer.radius = radius;
        circleContainer.center = newLocation;
        if (doneDragging) {
            zoomToCircle();
        }
    }

    private void dragPointOnLine(int indexOfAnnotationToDrag, LatLng newLocation, boolean isMidpoint, boolean doneDragging) {
        List<MarkerView> listToDealWith = isMidpoint ? midpoints : corners;
        listToDealWith.get(indexOfAnnotationToDrag).setPosition(newLocation);
        if (doneDragging) {
            List<LatLng> points = lineContainer.line.getPoints();
            if (isMidpoint) {
                //Add the midpoint as a new normal point
                points.add((indexOfAnnotationToDrag + 1) % corners.size(), newLocation);
                corners.add((indexOfAnnotationToDrag + 1) % corners.size(), map.addMarker(getDefaultMarkerOptions(newLocation)));
                //New midpoints are added when recalculating all midpoints
            } else {
                points.set(indexOfAnnotationToDrag, newLocation); //If not midpoint, then the index of the point to change on the line is the same as the index of the corner annotation
            }
            //Update the polyline (both the line and width)
            lineContainer.line.setPoints(points);
            lineContainer.width.setPoints(points);
            //Update the Midpoints
            clearMidpoints();
            for (LatLng latLng : getMidpointsFromLatLngs(points)) {
                midpoints.add(map.addMarker(getDefaultMidpointMarker(latLng)));
            }
        } else {
            int indexOfPreviousAnnotation;
            int indexOfNextAnnotation;
            if (isMidpoint) {
                //If we look at midpoint 1, one corner will be at 1, the other will be at 2
                indexOfPreviousAnnotation = indexOfAnnotationToDrag;
                indexOfNextAnnotation = (indexOfAnnotationToDrag + 1) % corners.size();
                //TODO: Need to figure out the indices of corner annotations in the corners array based on the index of the midpoint in the midpoints array
                scratchpad.dragTo(map.getProjection().toScreenLocation(corners.get(indexOfPreviousAnnotation).getPosition()), map.getProjection().toScreenLocation(midpoints.get(indexOfAnnotationToDrag).getPosition()), map.getProjection().toScreenLocation(corners.get(indexOfNextAnnotation).getPosition()));
            } else {
                if (indexOfAnnotationToDrag == 0) { //If the annotation is the first, only 1 dotted line will be drawn
                    indexOfNextAnnotation = indexOfAnnotationToDrag + 1;
                    scratchpad.dragTo(map.getProjection().toScreenLocation(listToDealWith.get(indexOfNextAnnotation).getPosition()), map.getProjection().toScreenLocation(listToDealWith.get(indexOfAnnotationToDrag).getPosition()));
                } else if (indexOfAnnotationToDrag == listToDealWith.size() - 1) { //If the annotation is the last, only 1 dotted line will be drawn
                    indexOfPreviousAnnotation = indexOfAnnotationToDrag - 1;
                    scratchpad.dragTo(map.getProjection().toScreenLocation(listToDealWith.get(indexOfPreviousAnnotation).getPosition()), map.getProjection().toScreenLocation(listToDealWith.get(indexOfAnnotationToDrag).getPosition()));
                } else {
                    indexOfPreviousAnnotation = indexOfAnnotationToDrag - 1;
                    indexOfNextAnnotation = indexOfAnnotationToDrag + 1;
                    scratchpad.dragTo(map.getProjection().toScreenLocation(listToDealWith.get(indexOfPreviousAnnotation).getPosition()), map.getProjection().toScreenLocation(listToDealWith.get(indexOfAnnotationToDrag).getPosition()), map.getProjection().toScreenLocation(listToDealWith.get(indexOfNextAnnotation).getPosition()));
                }
            }
        }
    }

    private void dragPointOnPolygon(int indexOfAnnotationToDrag, LatLng newLocation, boolean isMidpoint, boolean doneDragging) {
        if (isMidpoint) {
            midpoints.get(indexOfAnnotationToDrag).setPosition(newLocation);
        } else {
            corners.get(indexOfAnnotationToDrag).setPosition(newLocation);
        }
        List<LatLng> points = polygonContainer.polygon.getPoints();
        if (doneDragging) {
            if (isMidpoint) {
                //Add the midpoint as a new normal point
                points.add((indexOfAnnotationToDrag + 1) % corners.size(), newLocation);
                corners.add((indexOfAnnotationToDrag + 1) % corners.size(), map.addMarker(getDefaultMarkerOptions(newLocation)));
                //New midpoints are added when recalculating all midpoints
            } else {
                points.set(indexOfAnnotationToDrag, newLocation); //If not midpoint, then the index of the point to change on the line is the same as the index of the corner annotation
                if (indexOfAnnotationToDrag == 0) {
                    points.set(points.size() - 1, newLocation); //First and last point both need to be set in polygon and line
                }
            }
            polygonContainer.polygon.setPoints(points);
            polygonContainer.outline.setPoints(points);
            clearMidpoints(); //Delete old midpoints
            for (LatLng latLng : getMidpointsFromLatLngs(points)) {
                midpoints.add(map.addMarker(getDefaultMidpointMarker(latLng))); //Compute new midpoints
            }
            checkForIntersections(points.subList(0, points.size() - 2)); //Don't include the last point when checking for intersection, otherwise it will always say there is one
        } else {
            int indexOfPreviousAnnotation;
            int indexOfNextAnnotation;
            if (isMidpoint) {
                indexOfPreviousAnnotation = indexOfAnnotationToDrag;
                indexOfNextAnnotation = (indexOfAnnotationToDrag + 1) % corners.size();
                //TODO: Need to figure out the indices of corner annotations in the corners array based on the index of the midpoint in the midpoints array
                scratchpad.dragTo(map.getProjection().toScreenLocation(corners.get(indexOfPreviousAnnotation).getPosition()), map.getProjection().toScreenLocation(midpoints.get(indexOfAnnotationToDrag).getPosition()), map.getProjection().toScreenLocation(corners.get(indexOfNextAnnotation).getPosition()));
            } else {
                indexOfNextAnnotation = (indexOfAnnotationToDrag + 1) % corners.size();
                indexOfPreviousAnnotation = (indexOfAnnotationToDrag - 1) < 0 ? corners.size() - 1 : indexOfAnnotationToDrag - 1; //Since we only ever decrease by 1, the most it can wrap around to is from the first element to the last element
                scratchpad.dragTo(map.getProjection().toScreenLocation(corners.get(indexOfPreviousAnnotation).getPosition()), map.getProjection().toScreenLocation(corners.get(indexOfAnnotationToDrag).getPosition()), map.getProjection().toScreenLocation(corners.get(indexOfNextAnnotation).getPosition()));
            }
        }
    }

    private void hideSeekBar() {
        seekBarContainer.setVisibility(View.GONE);
        seekBar.setOnSeekBarChangeListener(null);
        seekBar.setProgress(0);
    }

    private void showSeekBarForCircle() {
        seekBarContainer.setVisibility(View.VISIBLE);
        seekBarLabelTextView.setText(R.string.airmap_buffer);
        seekBar.setOnSeekBarChangeListener(null); //This is needed because when setting max, it might cause progress to change, which we don't want
        seekBar.setMax(getBufferPresets().length - 1);
        seekBar.setProgress(0);
        circleContainer.center = map.getCameraPosition().target;
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                seekBarValueTextView.setText(getBufferPresets()[progress].label);
                drawCircle(circleContainer.center, getBufferPresets()[progress].value.doubleValue());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                checkCircleStatus();
            }
        });
        seekBar.setProgress(18); //1000 ft
    }

    private void checkCircleStatus() {
        Coordinate coordinate = new Coordinate(circleContainer.center);
        AirMap.checkCoordinate(coordinate, circleContainer.radius, null, null, false, null, FreehandMapActivity.this);
        zoomToCircle();
    }

    private void checkPathStatus() {
        //TODO
    }

    private void checkPolygonStatus() {
        //TODO
    }

    private void showSeekBarForPath() {
        seekBarContainer.setVisibility(View.VISIBLE);
        seekBarLabelTextView.setText(R.string.airmap_width);
        seekBar.setOnSeekBarChangeListener(null); //This is needed because when setting max, it might cause progress to change, which we don't want since it would call the listener
        seekBar.setMax(Utils.getBufferPresets().length - 1);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                seekBarValueTextView.setText(getBufferPresets()[progress].label);
                if (lineContainer.width != null) {
                    lineContainer.width.setWidth(getPathWidth(progress));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        seekBar.setProgress(1); //50 ft
    }

    @Override
    public void onCameraChange(CameraPosition position) {
        //TODO: Improve this logic. Want to show and hide based on how close the the midpoint markers are to other markers, not just at static zoom levels
        if (zoomAtDraw > 0) {
            double zoomDiff = zoomAtDraw - position.zoom;
            for (MarkerView corner : corners) {
                corner.setVisible(zoomDiff < 2);
            }

            for (MarkerView midpoint : midpoints) {
                midpoint.setVisible(zoomDiff < 1);
            }
        }
    }

    private void updateTip(@StringRes int textId) {
        updateTip(textId, R.drawable.rounded_corners_gray);
    }

    private void updateTip(@StringRes int textId, @DrawableRes int drawable) {
        tipTextView.setVisibility(View.VISIBLE);
        tipTextView.setText(textId);
        tipTextView.setBackgroundResource(drawable);
    }

    private void showDeleteButton(boolean show) {
        deleteButton.setVisibility(show ? View.VISIBLE : View.GONE);
        enableDrawingSwitch.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onSuccess(final AirMapStatus response) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                updateButtonColor(response != null ? response.getAdvisoryColor() : null);
            }
        });
    }

    @Override
    public void onError(AirMapException e) {
        updateButtonColor(null);
    }

    @UiThread
    public void updateButtonColor(@Nullable AirMapStatus.StatusColor color) {
        if (nextButton != null) { //Called from callback, Activity might have been destroyed
            if (color == AirMapStatus.StatusColor.Red) {
                nextButton.getBackground().setColorFilter(Color.RED, PorterDuff.Mode.MULTIPLY);
                nextButton.setTextColor(Color.WHITE);
            } else if (color == AirMapStatus.StatusColor.Yellow) {
                nextButton.getBackground().setColorFilter(Color.YELLOW, PorterDuff.Mode.MULTIPLY);
                nextButton.setTextColor(Color.BLACK);
            } else {
                nextButton.getBackground().setColorFilter(ContextCompat.getColor(this, R.color.colorPrimary), PorterDuff.Mode.MULTIPLY);
                nextButton.setTextColor(Color.WHITE);
            }
        }
    }

    private PolygonOptions getDefaultPolygonOptions() {
        PolygonOptions options = new PolygonOptions();
        options.fillColor(ContextCompat.getColor(this, R.color.airmap_colorFill));
        options.alpha(0.66f);
        return options;
    }

    private PolylineOptions getDefaultPolylineOptions() {
        PolylineOptions options = new PolylineOptions();
        options.color(ContextCompat.getColor(this, R.color.colorPrimary));
        options.width(2);
        return options;
    }

    private static MarkerViewOptions getDefaultMarkerOptions(LatLng latLng) {
        MarkerViewOptions options = new MarkerViewOptions();
        options.position(latLng);
        options.icon(draggerIcon);
        options.title(CORNER_TAG);
        options.anchor(0.5f, 0.5f);
        return options;
    }

    private static MarkerViewOptions getDefaultMidpointMarker(LatLng latLng) {
        MarkerViewOptions options = new MarkerViewOptions();
        options.position(latLng);
        options.icon(addCircleIcon);
        options.title(MIDPOINT_TAG);
        options.anchor(0.5f, 0.5f);
        return options;
    }

    private static MarkerViewOptions getIntersectionMarker(LatLng latLng) {
        MarkerViewOptions options = new MarkerViewOptions();
        options.position(latLng);
        options.icon(intersectionIcon);
        options.anchor(0.5f, 0.5f);
        return options;
    }

    //Emulate a circle as a polygon with a bunch of sides
    private static ArrayList<LatLng> polygonCircleForCoordinate(LatLng location, double radius) {
        int degreesBetweenPoints = 2;
        int numberOfPoints = (int) Math.floor(360 / degreesBetweenPoints);
        double distRadians = radius / 6371000.0; // earth radius in meters
        double centerLatRadians = location.getLatitude() * Math.PI / 180;
        double centerLonRadians = location.getLongitude() * Math.PI / 180;
        ArrayList<LatLng> polygons = new ArrayList<>(); //array to hold all the points
        for (int index = 0; index < numberOfPoints; index++) {
            double degrees = index * degreesBetweenPoints;
            double degreeRadians = degrees * Math.PI / 180;
            double pointLatRadians = Math.asin(Math.sin(centerLatRadians) * Math.cos(distRadians) + Math.cos(centerLatRadians) * Math.sin(distRadians) * Math.cos(degreeRadians));
            double pointLonRadians = centerLonRadians + Math.atan2(Math.sin(degreeRadians) * Math.sin(distRadians) * Math.cos(centerLatRadians),
                    Math.cos(distRadians) - Math.sin(centerLatRadians) * Math.sin(pointLatRadians));
            double pointLat = pointLatRadians * 180 / Math.PI;
            double pointLon = pointLonRadians * 180 / Math.PI;
            LatLng point = new LatLng(pointLat, pointLon);
            polygons.add(point);
        }
        return polygons;
    }

    public static TabLayout.Tab getTab(TabLayout tabLayout, @StringRes int textId, @DrawableRes int iconId, String id) {
        TabLayout.Tab tab = tabLayout.newTab();
        tab.setText(textId);
        tab.setIcon(iconId);
        tab.setTag(id);
        return tab;
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }
}
