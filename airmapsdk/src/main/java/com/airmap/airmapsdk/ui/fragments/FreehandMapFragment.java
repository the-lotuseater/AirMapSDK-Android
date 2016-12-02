package com.airmap.airmapsdk.ui.fragments;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.annotation.UiThread;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.airmap.airmapsdk.AdvisoriesAdapter;
import com.airmap.airmapsdk.AirMapException;
import com.airmap.airmapsdk.CircleContainer;
import com.airmap.airmapsdk.DrawingCallback;
import com.airmap.airmapsdk.LineContainer;
import com.airmap.airmapsdk.PointMath;
import com.airmap.airmapsdk.R;
import com.airmap.airmapsdk.Utils;
import com.airmap.airmapsdk.models.Coordinate;
import com.airmap.airmapsdk.models.PolygonContainer;
import com.airmap.airmapsdk.models.airspace.AirMapAirspace;
import com.airmap.airmapsdk.models.flight.AirMapFlight;
import com.airmap.airmapsdk.models.shapes.AirMapPath;
import com.airmap.airmapsdk.models.shapes.AirMapPolygon;
import com.airmap.airmapsdk.models.status.AirMapStatus;
import com.airmap.airmapsdk.models.status.AirMapStatusAdvisory;
import com.airmap.airmapsdk.networking.callbacks.AirMapCallback;
import com.airmap.airmapsdk.networking.services.AirMap;
import com.airmap.airmapsdk.ui.CustomButton;
import com.airmap.airmapsdk.ui.DrawingBoard;
import com.airmap.airmapsdk.ui.ImageViewSwitch;
import com.airmap.airmapsdk.ui.Scratchpad;
import com.airmap.airmapsdk.ui.activities.CreateFlightActivity;
import com.github.lzyzsd.jsbridge.BridgeWebView;
import com.github.lzyzsd.jsbridge.CallBackFunction;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerView;
import com.mapbox.mapboxsdk.annotations.MarkerViewOptions;
import com.mapbox.mapboxsdk.annotations.Polygon;
import com.mapbox.mapboxsdk.annotations.PolygonOptions;
import com.mapbox.mapboxsdk.annotations.PolylineOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import okhttp3.Call;

import static com.airmap.airmapsdk.PointMath.distanceBetween;
import static com.airmap.airmapsdk.Utils.getBufferPresets;

/**
 * Created by Vansh Gandhi on 11/13/16.
 * Copyright © 2016 AirMap, Inc. All rights reserved.
 */

public class FreehandMapFragment extends Fragment implements OnMapReadyCallback,
        DrawingCallback, MapboxMap.OnCameraChangeListener, AirMapCallback<AirMapStatus> {

    private static final String CIRCLE_TAG = "circle";
    private static final String PATH_TAG = "path";
    private static final String POLYGON_TAG = "polygon";
    private static final String MIDPOINT_TAG = "midpoint";
    private static final String CORNER_TAG = "corner";
    private static final String INTERSECTION_TAG = "intersection";

    private static Icon cornerIcon;
    private static Icon midpointIcon;
    private static Icon intersectionIcon;

    //Main layout views
//    private AppBarLayout appBarLayout;
//    private Toolbar toolbar;
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
    private CustomButton nextButton;

    //Bottom sheet layout views
    private CoordinatorLayout bottomSheetLayout;
    private RecyclerView recyclerView;

    private BottomSheetBehavior bottomSheetBehavior;

    private MapboxMap map;

    private CircleContainer circleContainer;
    private PolygonContainer polygonContainer;
    private LineContainer lineContainer;
    private List<MarkerView> corners;
    private List<MarkerView> midpoints;
    private List<MarkerView> intersections; //Store this in the Polygon class? since this is only relevant for polygon
    private List<Polygon> redPolygons;
    private Rect deleteCoordinates = new Rect();
    private float screenDensity;
    private AirMapStatus latestStatus;
    private Call airspaceCall;
    private Call statusCall;

    private OnFragmentInteractionListener mListener;

    private BridgeWebView webView;

    public FreehandMapFragment() {
        //Required empty constructor
    }

    public static FreehandMapFragment newInstance(Coordinate coordinate) {
        Bundle args = new Bundle();
        args.putSerializable(CreateFlightActivity.COORDINATE, coordinate);
        FreehandMapFragment fragment = new FreehandMapFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.airmap_fragment_freehand, container, false);
        initializeViews(view);
        setupSwitch();
        setupMap(savedInstanceState);
        setupButtons();
        setupBottomSheet();
        drawingBoard.setDoneDrawingCallback(this);

        circleContainer = new CircleContainer();
        lineContainer = new LineContainer();
        polygonContainer = new PolygonContainer();

        corners = new ArrayList<>();
        midpoints = new ArrayList<>();
        intersections = new ArrayList<>();
        redPolygons = new ArrayList<>();
        cornerIcon = IconFactory.getInstance(getContext()).fromResource(R.drawable.white_circle);
        midpointIcon = IconFactory.getInstance(getContext()).fromResource(R.drawable.gray_circle);
        intersectionIcon = IconFactory.getInstance(getContext()).fromResource(R.drawable.intersection_circle);

        screenDensity = getResources().getDisplayMetrics().density;

        webView = new BridgeWebView(getContext());
        webView.setWillNotDraw(true);
        webView.loadUrl("file:///android_asset/turf.html");

        return view;
    }

    private void initializeViews(View view) {
        seekBarContainer = (RelativeLayout) view.findViewById(R.id.seekbar_container);
        seekBar = (SeekBar) view.findViewById(R.id.seekbar);
        seekBarLabelTextView = (TextView) view.findViewById(R.id.label);
        seekBarValueTextView = (TextView) view.findViewById(R.id.seekbar_value);
        enableDrawingSwitch = (ImageViewSwitch) view.findViewById(R.id.action_button);
        deleteButton = (ImageView) view.findViewById(R.id.delete_button);
        tipTextView = (TextView) view.findViewById(R.id.tip_text);
        mapView = (MapView) view.findViewById(R.id.map);
        drawingBoard = (DrawingBoard) view.findViewById(R.id.drawFrame);
        scratchpad = (Scratchpad) view.findViewById(R.id.scratchpad);
        nextButton = (CustomButton) view.findViewById(R.id.next_button);

        bottomSheetLayout = (CoordinatorLayout) view.findViewById(R.id.bottom_sheet);
        recyclerView = (RecyclerView) view.findViewById(R.id.advisories_list);
    }

    private void setupMap(Bundle savedInstanceState) {
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
    }

    private void setupTabs() {
        tabLayout = mListener.getTabLayout();
        tabLayout.clearOnTabSelectedListeners();
        tabLayout.removeAllTabs();
        tabLayout.setVisibility(View.VISIBLE);
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
                    updateTip(R.string.airmap_freehand_tip_path);
                    drawingBoard.setPolygonMode(false); //Doesn't close the drawn path
                    drawingBoard.setVisibility(View.VISIBLE);
                    enableDrawingSwitch.setVisibility(View.VISIBLE);
                    enableDrawingSwitch.setChecked(false);
                    showDeleteButton(false);
                } else if (POLYGON_TAG.equals(tag)) {
                    hideSeekBar();
                    updateTip(R.string.airmap_freehand_tip_area);
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
                    updateTip(drawingBoard.isPolygonMode() ? R.string.airmap_draw_freehand_area : R.string.airmap_draw_freehand_path);
                } else {
                    updateTip(tabLayout.getSelectedTabPosition() == 2 ? R.string.airmap_freehand_tip_area : R.string.airmap_freehand_tip_path);
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
                    updateTip(R.string.airmap_freehand_tip_area);
                }
            }
        });

        nextButton.setDrawableClickListener(new CustomButton.DrawableClickListener() {
            @Override
            public void onDrawableClick() {
                if (latestStatus != null && !latestStatus.getAdvisories().isEmpty()) {
                    recyclerView.setAdapter(new AdvisoriesAdapter(latestStatus.getAdvisories()));
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                }
            }
        });

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (tabLayout.getSelectedTabPosition() == 0 && circleContainer.isValid()) { //Circle
                    if (mListener != null) {
                        mListener.getFlight().setCoordinate(new Coordinate(circleContainer.center));
                        mListener.getFlight().setBuffer(circleContainer.radius);
                        tabLayout.setVisibility(View.GONE);
                        mListener.freehandNextClicked();
                    }
                } else if (tabLayout.getSelectedTabPosition() == 1 && lineContainer.isValid()) { //Path
                    List<Coordinate> coordinates = new ArrayList<>();
                    for (LatLng latLng : lineContainer.line.getPoints()) {
                        coordinates.add(new Coordinate(latLng));
                    }
                    if (mListener != null) {
                        mListener.getFlight().setGeometry(new AirMapPath(coordinates));
                        mListener.getFlight().setBuffer(lineContainer.width);
                        tabLayout.setVisibility(View.GONE);
                        mListener.freehandNextClicked();
                    }
                } else if (tabLayout.getSelectedTabPosition() == 2 && polygonContainer.isValid()) { //Polygon
                    if (!PointMath.findIntersections(polygonContainer.polygon.getPoints()).isEmpty()) {
                        Toast.makeText(getContext(), R.string.airmap_error_overlap, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    List<Coordinate> coordinates = new ArrayList<>();
                    for (LatLng latLng : polygonContainer.polygon.getPoints()) {
                        coordinates.add(new Coordinate(latLng));
                    }
                    if (mListener != null) {
                        mListener.getFlight().setGeometry(new AirMapPolygon(coordinates));
                        tabLayout.setVisibility(View.GONE);
                        mListener.freehandNextClicked();
                    }
                }

            }
        });
    }

    private void setupBottomSheet() {
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetLayout);
        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (mListener != null) {
                    if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                        mListener.bottomSheetOpened();
                    } else if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                        mListener.bottomSheetClosed();
                    } else if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                        mListener.bottomSheetClosed();
                    } else if (newState == BottomSheetBehavior.STATE_DRAGGING) {
                        mListener.bottomSheetClosed();
                    }
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
            }
        });
    }

    public void clear() {
        cancelStatusCall();
        map.removeAnnotations();
        circleContainer.clear();
        lineContainer.clear();
        polygonContainer.clear();
        corners.clear();
        midpoints.clear();
        redPolygons.clear();
        updateButtonColor(null);
        latestStatus = null;
    }

    public void clearMidpoints() {
        if (midpoints != null) {
            map.removeAnnotations(midpoints);
            midpoints.clear();
        }
    }

    @Override
    public void doneDrawing(@NonNull List<PointF> points) {
        showDeleteButton(true);
        enableDrawingSwitch.setChecked(false);
        updateTip(R.string.airmap_done_drawing_tip);
        if (drawingBoard.isPolygonMode()) {
            drawPolygon(points);
        } else { //Path mode
            drawPath(points);
        }
    }

    private void drawCircle(LatLng center, double radius) {
        clear();
        List<LatLng> circlePoints = polygonCircleForCoordinate(center, radius);
        PolylineOptions polylineOptions = getDefaultPolylineOptions(getContext()).addAll(circlePoints).add(circlePoints.get(0));
        circleContainer.circle = map.addPolygon(getDefaultPolygonOptions(getContext()).addAll(circlePoints));
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
        PolylineOptions thinLine = getDefaultPolylineOptions(getContext());
        double width = getPathWidthFromSeekBar(seekBar.getProgress());
        List<LatLng> midPoints = getMidpointsFromLatLngs(getLatLngsFromPointFs(line));
        List<LatLng> points = getLatLngsFromPointFs(line);
        for (int i = 0; i < points.size(); i++) {
            LatLng point = points.get(i);
            thinLine.add(point);
            corners.add(map.addMarker(getDefaultMarkerOptions(point)));
            if (i < midPoints.size()) {
                LatLng midPoint = midPoints.get(i);
                midpoints.add(map.addMarker(getDefaultMidpointMarker(midPoint)));
            }
        }

        lineContainer.width = width;
        calculatePathBufferAndDisplayLineAndBuffer(thinLine.getPoints(), lineContainer.width);

        LatLngBounds bounds = new LatLngBounds.Builder().includes(thinLine.getPoints()).build();
        map.easeCamera(CameraUpdateFactory.newLatLngBounds(bounds, 150));
    }

    public void drawPolygon(List<PointF> pointsDrawn) {
        clear();
        PointF first = new PointF(pointsDrawn.get(0).x, pointsDrawn.get(0).y); //Making copy of the object so MapBox doesn't mess up the point
        PointF last = new PointF(pointsDrawn.get(pointsDrawn.size() - 1).x, pointsDrawn.get(pointsDrawn.size() - 1).y); //Making copies so MapBox doesn't mess up the point
        if (distanceBetween(first, last) < 100) { //Check if the two points are close, based on screen distance between points, rather than LatLng distance
            pointsDrawn.set(pointsDrawn.size() - 1, first); //Ignore the last drawn point if it was close to the starting point
        } else {
            pointsDrawn.add(first);
        }
        PolygonOptions polygonOptions = getDefaultPolygonOptions(getContext());
        PolylineOptions polylineOptions = getDefaultPolylineOptions(getContext());
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
        checkForIntersections(points); //Don't include the last point when checking for intersection, otherwise it will always say there is one
        checkPolygonStatus();
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

    private static double getPathWidthFromSeekBar(int progress) {
        return Utils.getBufferPresets()[progress].value.doubleValue();
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

        setupTabs();
        mapView.setOnTouchListener(new View.OnTouchListener() {
            //This onTouch code is a copy of the MapView#onSingleTapConfirmed code, except
            //I'm dragging instead of clicking, and it's being called for every touch event rather than just a tap
            //It also simplifies some of the selection logic
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event != null) {
                    if (event.getPointerCount() > 1) {
                        scratchpad.reset();
                        scratchpad.invalidate();
                        return false; //Don't drag if there are multiple fingers on screen
                    }
                    PointF tapPoint = new PointF(event.getX(), event.getY());
                    float toleranceSides = 4 * screenDensity;
                    float toleranceTopBottom = 10 * screenDensity;
                    float averageIconWidth = 42;
                    float averageIconHeight = 42;
                    RectF tapRect = new RectF(tapPoint.x - averageIconWidth / 2 - toleranceSides,
                            tapPoint.y - averageIconHeight / 2 - toleranceTopBottom,
                            tapPoint.x + averageIconWidth / 2 + toleranceSides,
                            tapPoint.y + averageIconHeight / 2 + toleranceTopBottom);
                    try {
                        Method method = mapView.getClass().getDeclaredMethod("getMarkersInRect", RectF.class); //Using reflection to access a Mapbox Package Private method
                        method.setAccessible(true);
                        Marker newSelectedMarker = null;
                        List<Marker> nearbyMarkers = (List<Marker>) method.invoke(mapView, tapRect);
                        List<Marker> selectedMarkers = map.getSelectedMarkers();
                        if (selectedMarkers.isEmpty() && nearbyMarkers != null && !nearbyMarkers.isEmpty()) {
                            Collections.sort(nearbyMarkers);
                            for (Marker marker : nearbyMarkers) {
                                if (marker instanceof MarkerView && !((MarkerView) marker).isVisible()) {
                                    continue; //Don't let user click on hidden midpoints
                                }
                                if (!marker.getTitle().equals(INTERSECTION_TAG)) {
                                    newSelectedMarker = marker;
                                    break;
                                }
                            }
                        } else if (!selectedMarkers.isEmpty()) {
                            newSelectedMarker = selectedMarkers.get(0);
                        }

                        if (newSelectedMarker != null && newSelectedMarker instanceof MarkerView) {
                            boolean doneDragging = event.getAction() == MotionEvent.ACTION_UP;
                            boolean deletePoint = false;
                            deleteButton.getDrawingRect(deleteCoordinates);
                            deleteCoordinates.left *= 2;
                            deleteCoordinates.right *= 2;
                            deleteCoordinates.top *= 2;
                            deleteCoordinates.bottom *= 2;
                            if (deleteCoordinates.contains((int) tapPoint.x, (int) tapPoint.y)) {
                                deleteButton.setImageResource(R.drawable.ic_delete_active);
                                if (doneDragging) {
                                    deleteButton.setImageResource(R.drawable.ic_delete);
                                    updateTip(tabLayout.getSelectedTabPosition() == 2 ? R.string.airmap_freehand_tip_area : R.string.airmap_freehand_tip_path);
                                    deletePoint = true;
                                }
                            } else {
                                deleteButton.setImageResource(R.drawable.ic_delete);
                            }
                            if (tabLayout.getSelectedTabPosition() != 0) { //We're not showing a tip for circle
                                if (doneDragging) {
                                    updateTip(tabLayout.getSelectedTabPosition() == 2 ? R.string.airmap_freehand_tip_area : R.string.airmap_freehand_tip_path);
                                } else {
                                    updateTip(R.string.airmap_delete_tip);
                                }
                            }

                            //DRAG!
                            //Trying to put most logic in the drag() function, this is pretty messy already
                            boolean isMidpoint = midpoints.contains(newSelectedMarker);
                            map.selectMarker(newSelectedMarker); //Use the marker selection state to prevent selecting another marker when dragging over it
                            drag(isMidpoint ? midpoints.indexOf(newSelectedMarker) : corners.indexOf(newSelectedMarker), map.getProjection().fromScreenLocation(tapPoint), isMidpoint, doneDragging, deletePoint);
                            if (doneDragging) {
                                map.deselectMarker(newSelectedMarker);
                            }
                            return true;
                        }
                    } catch (Exception e) {
                        //Probably a reflection error
                        e.printStackTrace();
                    }
                }
                scratchpad.reset();
                scratchpad.invalidate();
                return false;
            }
        });

        if (getArguments() != null) {
            Coordinate coordinate = (Coordinate) getArguments().getSerializable(CreateFlightActivity.COORDINATE);
            if (coordinate != null) {
                map.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(coordinate.getLatitude(), coordinate.getLongitude())), new MapboxMap.CancelableCallback() {
                    @Override
                    public void onCancel() {

                    }

                    @Override
                    public void onFinish() {
                        showSeekBarForCircle(); //Now circle will show at right place initially
                    }
                });
            }
        }
    }

    private void drag(int indexOfAnnotationToDrag, LatLng newLocation, boolean isMidpoint, boolean doneDragging, boolean deletePoint) {
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

        if (indexOfAnnotationToDrag == -1) {
            System.out.println("Index was null??");
            return;
        }

        if (tabLayout.getSelectedTabPosition() == 0) {
            dragCircle(indexOfAnnotationToDrag, newLocation, doneDragging);
        } else if (tabLayout.getSelectedTabPosition() == 1) {
            dragPointOnLine(indexOfAnnotationToDrag, newLocation, isMidpoint, doneDragging, deletePoint);
        } else if (tabLayout.getSelectedTabPosition() == 2) {
            dragPointOnPolygon(indexOfAnnotationToDrag, newLocation, isMidpoint, doneDragging, deletePoint);
        }
        if (doneDragging) {
            scratchpad.reset();
            scratchpad.invalidate();
            setMidpointVisibilities();
        }
    }

    private void dragCircle(int indexOfAnnotationToDrag, LatLng newLocation, boolean doneDragging) {
        map.removeAnnotation(circleContainer.circle);
        map.removeAnnotation(circleContainer.outline);
        corners.get(indexOfAnnotationToDrag).setPosition(newLocation); //Move the center point
        double radius = getBufferPresets()[seekBar.getProgress()].value.doubleValue(); //Move the circle polygon
        List<LatLng> circlePoints = polygonCircleForCoordinate(newLocation, radius);
        PolylineOptions polylineOptions = getDefaultPolylineOptions(getContext()).addAll(circlePoints).add(circlePoints.get(0));
        circleContainer.circle = map.addPolygon(getDefaultPolygonOptions(getContext()).addAll(circlePoints));
        circleContainer.outline = map.addPolyline(polylineOptions);
        circleContainer.radius = radius;
        circleContainer.center = newLocation;
        if (doneDragging) {
            zoomToCircle();
            checkCircleStatus();
        }
    }

    private void dragPointOnLine(int indexOfAnnotationToDrag, LatLng newLocation, boolean isMidpoint, boolean doneDragging, boolean deletePoint) {
        if (isMidpoint) {
            midpoints.get(indexOfAnnotationToDrag).setPosition(newLocation);
        } else {
            corners.get(indexOfAnnotationToDrag).setPosition(newLocation);
        }
        if (doneDragging) {
            List<LatLng> points = lineContainer.line.getPoints();
            if (deletePoint) {
                if (!isMidpoint) {
                    if (corners.size() > 2) {
                        points.remove(indexOfAnnotationToDrag);
                        map.removeAnnotation(corners.remove(indexOfAnnotationToDrag));
                    } else {
                        corners.get(indexOfAnnotationToDrag).setPosition(points.get(indexOfAnnotationToDrag));
                    }
                }
            } else if (isMidpoint) {
                //Add the midpoint as a new normal point
                points.add((indexOfAnnotationToDrag + 1) % corners.size(), newLocation);
                corners.add((indexOfAnnotationToDrag + 1) % corners.size(), map.addMarker(getDefaultMarkerOptions(newLocation)));
                //New midpoints are added when recalculating all midpoints
            } else {
                points.set(indexOfAnnotationToDrag, newLocation); //If not midpoint, then the index of the point to change on the line is the same as the index of the corner annotation
            }
            //Update the polyline (both the line and widthPolyline)
            calculatePathBufferAndDisplayLineAndBuffer(points, lineContainer.width);
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
                scratchpad.dragTo(map.getProjection().toScreenLocation(corners.get(indexOfPreviousAnnotation).getPosition()), map.getProjection().toScreenLocation(midpoints.get(indexOfAnnotationToDrag).getPosition()), map.getProjection().toScreenLocation(corners.get(indexOfNextAnnotation).getPosition()));
            } else {
                if (indexOfAnnotationToDrag == 0) { //If the annotation is the first, only 1 dotted line will be drawn
                    indexOfNextAnnotation = indexOfAnnotationToDrag + 1;
                    scratchpad.dragTo(map.getProjection().toScreenLocation(corners.get(indexOfNextAnnotation).getPosition()), map.getProjection().toScreenLocation(corners.get(indexOfAnnotationToDrag).getPosition()));
                } else if (indexOfAnnotationToDrag == corners.size() - 1) { //If the annotation is the last, only 1 dotted line will be drawn
                    indexOfPreviousAnnotation = indexOfAnnotationToDrag - 1;
                    scratchpad.dragTo(map.getProjection().toScreenLocation(corners.get(indexOfPreviousAnnotation).getPosition()), map.getProjection().toScreenLocation(corners.get(indexOfAnnotationToDrag).getPosition()));
                } else {
                    indexOfPreviousAnnotation = indexOfAnnotationToDrag - 1;
                    indexOfNextAnnotation = indexOfAnnotationToDrag + 1;
                    scratchpad.dragTo(map.getProjection().toScreenLocation(corners.get(indexOfPreviousAnnotation).getPosition()), map.getProjection().toScreenLocation(corners.get(indexOfAnnotationToDrag).getPosition()), map.getProjection().toScreenLocation(corners.get(indexOfNextAnnotation).getPosition()));
                }
            }
        }
    }

    private void dragPointOnPolygon(int indexOfAnnotationToDrag, LatLng newLocation, boolean isMidpoint, boolean doneDragging, boolean deletePoint) {
        if (isMidpoint) {
            midpoints.get(indexOfAnnotationToDrag).setPosition(newLocation);
        } else {
            corners.get(indexOfAnnotationToDrag).setPosition(newLocation);
        }
        if (doneDragging) {
            List<LatLng> points = polygonContainer.polygon.getPoints();
            if (deletePoint) {
                if (!isMidpoint) {
                    if (corners.size() > 3) {
                        points.remove(indexOfAnnotationToDrag);
                        map.removeAnnotation(corners.remove(indexOfAnnotationToDrag));
                        if (indexOfAnnotationToDrag == 0) {
                            points.set(points.size() - 1, points.get(0));
                        } else if (indexOfAnnotationToDrag == corners.size() - 1) {
                            points.set(0, points.get(points.size() - 1));
                        }
                    } else {
                        corners.get(indexOfAnnotationToDrag).setPosition(points.get(indexOfAnnotationToDrag));
                    }
                }
            } else if (isMidpoint) {
                //Add the midpoint as a new normal point
                points.add((indexOfAnnotationToDrag + 1) % corners.size(), newLocation);
                corners.add((indexOfAnnotationToDrag + 1) % corners.size(), map.addMarker(getDefaultMarkerOptions(newLocation)));
                if (indexOfAnnotationToDrag == midpoints.size() - 1) { //If the user picked the last midpoint, it affects the first point on the polygon
                    points.set(points.size() - 1, points.get(0)); //The last point need to be the same as the first
                }
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
            checkForIntersections(points); //Don't include the last point when checking for intersection, otherwise it will always say there is one
            checkPolygonStatus();
        } else {
            int indexOfPreviousAnnotation;
            int indexOfNextAnnotation;
            if (isMidpoint) {
                indexOfPreviousAnnotation = indexOfAnnotationToDrag;
                indexOfNextAnnotation = (indexOfAnnotationToDrag + 1) % corners.size();
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
                if (!fromUser) {
                    checkCircleStatus();
                    zoomToCircle();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                checkCircleStatus();
                zoomToCircle();
            }
        });
        seekBar.setProgress(18); //1000 ft
    }

    private void checkCircleStatus() {
        Coordinate coordinate = new Coordinate(circleContainer.center);
        map.removeAnnotations(redPolygons);
        redPolygons.clear();
        cancelStatusCall();
        statusCall = AirMap.checkCoordinate(coordinate, circleContainer.radius, null, null, false, null, this);
    }

    private void checkPathStatus() {
        List<Coordinate> coordinates = new ArrayList<>();
        for (LatLng point : lineContainer.line.getPoints()) {
            coordinates.add(new Coordinate(point));
        }
        map.removeAnnotations(redPolygons);
        redPolygons.clear();
        cancelStatusCall();
        statusCall = AirMap.checkFlightPath(coordinates, (int) lineContainer.width, coordinates.get(0), null, null, false, null, this);
    }

    private void checkPolygonStatus() {
        List<Coordinate> coordinates = new ArrayList<>();
        for (LatLng point : polygonContainer.polygon.getPoints()) {
            coordinates.add(new Coordinate(point));
        }
        map.removeAnnotations(redPolygons);
        redPolygons.clear();
        cancelStatusCall();
        statusCall = AirMap.checkPolygon(coordinates, getPolygonCentroid(coordinates), null, null, false, null, this);
    }

    Coordinate getPolygonCentroid(List<Coordinate> vertices) {
        LatLng centroid = new LatLng();
        double signedArea = 0;
        double x0 = 0.0; // Current vertex X
        double y0 = 0.0; // Current vertex Y
        double x1 = 0.0; // Next vertex X
        double y1 = 0.0; // Next vertex Y
        double a = 0.0;  // Partial signed area

        // For all vertices
        for (int i = 0; i < vertices.size(); ++i) {
            x0 = vertices.get(i).getLatitude();
            y0 = vertices.get(i).getLongitude();
            x1 = vertices.get((i + 1) % vertices.size()).getLatitude();
            y1 = vertices.get((i + 1) % vertices.size()).getLongitude();
            a = x0 * y1 - x1 * y0;
            signedArea += a;
            centroid.setLatitude(centroid.getLatitude() + (x0 + x1) * a);
            centroid.setLongitude(centroid.getLongitude() + (y0 + y1) * a);
        }

        signedArea *= 0.5;
        centroid.setLatitude(centroid.getLatitude() / (6.0 * signedArea));
        centroid.setLongitude(centroid.getLongitude() / (6.0 * signedArea));
        return new Coordinate(centroid.wrap());
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
                lineContainer.width = getPathWidthFromSeekBar(progress);
                if (lineContainer.line != null) {
                    calculatePathBufferAndDisplayLineAndBuffer(lineContainer.line.getPoints(), lineContainer.width);
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

    //Width is in meters
    public void calculatePathBufferAndDisplayLineAndBuffer(final List<LatLng> linePoints, double width) {

        try {
//            InputStream is = getAssets().open("turf.min.js");
//            byte[] bytes = new byte[is.available()];
//            is.read(bytes);
//            String js = new String(bytes);


//            String coordArray = "";
//            for (LatLng latLng : linePoints) {
//                coordArray += String.format(Locale.US, "[%f, %f],", latLng.getLatitude(), latLng.getLongitude());
//            }

            JSONArray coordinatesArray = new JSONArray();
            for (LatLng latLng : linePoints) {
                JSONArray point = new JSONArray();
                point.put(latLng.getLatitude());
                point.put(latLng.getLongitude());
                coordinatesArray.put(point);
            }

            JSONObject json = new JSONObject();
            json.put("points", coordinatesArray);
            json.put("buffer", width); // width/100000
            System.out.println(json.toString());
            final PolygonOptions options = getDefaultPolygonOptions(getContext());
            webView.send(json.toString(), new CallBackFunction() {
                @Override
                public void onCallBack(String data) {
                    System.out.println(data);
                    String[] split = data.split(",");
                    for (int i = 0; i < split.length; i += 2) {
                        options.add(new LatLng(Double.valueOf(split[i]), Double.valueOf(split[i+1])));
                    }
                    if (map != null) {
                        if (lineContainer.buffer != null) {
                            map.removeAnnotation(lineContainer.buffer);
                        }
                        if (lineContainer.line != null) {
                            map.removeAnnotation(lineContainer.line);
                        }
                        lineContainer.buffer = map.addPolygon(options); //Need to add buffer first for proper z ordering
                        lineContainer.line = map.addPolyline(getDefaultPolylineOptions(getContext()).addAll(linePoints));
                        checkPathStatus();
                        LatLngBounds bounds = new LatLngBounds.Builder().includes(lineContainer.buffer.getPoints()).build();
                        map.easeCamera(CameraUpdateFactory.newLatLngBounds(bounds, 150));
                    }
                }
            });
//            latch.await();
//            latch.await(10, TimeUnit.SECONDS);
//            System.out.println("point size: " + options.getPoints().size());
//            while (true) {
//                if (options.getPoints().size() != 0)
//                    break;
//            }
//            return options;







//            String jsBufferCall = ";var line1=turf.linestring([%s]); var buffered = turf.buffer(line1, %f, 'feet'); out.print(buffered);";
//            String call = String.format(Locale.US, jsBufferCall, coordArray, width);


//            org.mozilla.javascript.Context rhino = org.mozilla.javascript.Context.enter();
//            rhino.setOptimizationLevel(-1);
//            Scriptable scope = rhino.initStandardObjects();
//            InputStreamReader reader = new InputStreamReader(is);
//            rhino.evaluateReader(scope, reader, "turf.min.js", 0, null);
//            Object wrappedOut = org.mozilla.javascript.Context.javaToJS(System.out, scope);
//            ScriptableObject.putProperty(scope, "out", wrappedOut);
//            rhino.evaluateString(scope, call, null, 0, null);
//            Object obj = scope.get("turf.buffer", scope);
//            if (obj instanceof Function) {
//                Function jsFunction = (Function) obj;
//                Object[] params = new Object[] { "line1", width, "'feet'" };
//                // Call the function with params
//                Object jsResult = jsFunction.call(rhino, scope, scope, params);
//                // Parse the jsResult object to a String
//                String result = org.mozilla.javascript.Context.toString(jsResult);
//                System.out.println(result);
//            }


//            JsEvaluator jsEvaluator = new JsEvaluator(this);
//            jsEvaluator.setWebViewWrapper(new WebViewWrapper(this, jsEvaluator));
//            jsEvaluator.getWebViewWrapper().loadJavaScript(js);
//            jsEvaluator.evaluate(call, new JsCallback() {
//                @Override
//                public void onResult(String s) {
//                    System.out.println(s);
//                }
//            });

//            JSContext context = new JSContext();
//            JSObject value = (JSObject) context.evaluateScript(js);
//            System.out.println(value);

        } catch (Exception e) {
            e.printStackTrace();
        }
//        finally {
//            org.mozilla.javascript.Context.exit();
//        }


//        PolygonOptions options = new PolygonOptions();
//        options.alpha(0.66f);
//        options.fillColor(ContextCompat.getColor(this, R.color.colorFill));
//        options.addAll(linePoints);
//        return options;
    }

    @Override
    public void onCameraChange(CameraPosition position) {
        setMidpointVisibilities();
    }

    private void setMidpointVisibilities() {
        for (MarkerView midpoint1 : midpoints) {
            boolean continueOuterLoop = false;
            for (MarkerView corner : corners) {
                PointF screenLoc1 = map.getProjection().toScreenLocation(midpoint1.getPosition());
                PointF screenLoc2 = map.getProjection().toScreenLocation(corner.getPosition());
                double distance = distanceBetween(screenLoc1, screenLoc2);
                if (distance < 100) {
                    midpoint1.setVisible(false);
                    continueOuterLoop = true;
                    break;
                } else {
                    midpoint1.setVisible(true);
                }
            }

            if (continueOuterLoop) {
                continue;
            }

            for (MarkerView midpoint2 : midpoints) {
                if (midpoint1 == midpoint2) continue;
                PointF screenLoc1 = map.getProjection().toScreenLocation(midpoint1.getPosition());
                PointF screenLoc2 = map.getProjection().toScreenLocation(midpoint2.getPosition());
                double distance = distanceBetween(screenLoc1, screenLoc2);
                if (distance < 100) {
                    midpoint1.setVisible(false);
                    break;
                } else {
                    midpoint1.setVisible(true);
                }
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

    private void cancelStatusCall() {
        if (statusCall != null) {
            statusCall.cancel();
        }
        if (airspaceCall != null) {
            airspaceCall.cancel();
        }
    }

    @Override
    public void onSuccess(final AirMapStatus response) {
        latestStatus = response;
        if (nextButton != null) {
            nextButton.post(new Runnable() {
                @Override
                public void run() {
                    updateButtonColor(response != null ? response.getAdvisoryColor() : null);
                }
            });
        }
        List<String> redIds = new ArrayList<>();
        for (AirMapStatusAdvisory advisory : response.getAdvisories()) {
            if (advisory.getColor() == AirMapStatus.StatusColor.Red) {
                redIds.add(advisory.getId());
            }
        }

        airspaceCall = AirMap.getAirspace(redIds, new AirMapCallback<List<AirMapAirspace>>() {
            @Override
            public void onSuccess(List<AirMapAirspace> response) {
                System.out.println(response);
                for (AirMapAirspace airMapAirspace : response) {
                    AirMapPolygon geometry = ((AirMapPolygon) airMapAirspace.getPropertyBoundary());
                    if (geometry != null && geometry.getCoordinates() != null) {
                        List<Coordinate> coordinates = geometry.getCoordinates();
                        PolygonOptions options = getDefaultRedPolygonOptions();
                        for (Coordinate coordinate : coordinates) {
                            options.add(new LatLng(coordinate.getLatitude(), coordinate.getLongitude()));
                        }
                        if (map != null && redPolygons != null) {
                            redPolygons.add(map.addPolygon(options));
                        }
                    }
                }
            }

            @Override
            public void onError(AirMapException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void onError(AirMapException e) {
        e.printStackTrace();
        if (nextButton != null) {
            nextButton.post(new Runnable() {
                @Override
                public void run() {
                    updateButtonColor(null);
                }
            });
        }
    }

    @UiThread
    public void updateButtonColor(@Nullable AirMapStatus.StatusColor color) {
        if (nextButton != null) { //Called from callback, Activity might have been destroyed
            if (color == AirMapStatus.StatusColor.Red) {
                nextButton.getBackground().setColorFilter(Color.RED, PorterDuff.Mode.MULTIPLY);
                nextButton.setTextColor(Color.WHITE);
                for (Drawable drawable : nextButton.getCompoundDrawables()) {
                    if (drawable != null) {
                        drawable.setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
                    }
                }
            } else if (color == AirMapStatus.StatusColor.Yellow) {
                nextButton.getBackground().setColorFilter(Color.YELLOW, PorterDuff.Mode.MULTIPLY);
                nextButton.setTextColor(Color.BLACK);
                for (Drawable drawable : nextButton.getCompoundDrawables()) {
                    if (drawable != null) {
                        drawable.setColorFilter(Color.BLACK, PorterDuff.Mode.MULTIPLY);
                    }
                }
            } else if (color == AirMapStatus.StatusColor.Green) {
                nextButton.getBackground().setColorFilter(ContextCompat.getColor(getContext(), R.color.airmap_green), PorterDuff.Mode.MULTIPLY);
                nextButton.setTextColor(Color.WHITE);
                for (Drawable drawable : nextButton.getCompoundDrawables()) {
                    if (drawable != null) {
                        drawable.setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
                    }
                }
            } else {
                nextButton.getBackground().setColorFilter(ContextCompat.getColor(getContext(), R.color.colorPrimary), PorterDuff.Mode.MULTIPLY);
                nextButton.setTextColor(Color.WHITE);
                for (Drawable drawable : nextButton.getCompoundDrawables()) {
                    if (drawable != null) {
                        drawable.setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
                    }
                }
            }
        }
    }

    public static PolygonOptions getDefaultPolygonOptions(Context context) {
        PolygonOptions options = new PolygonOptions();
        options.fillColor(ContextCompat.getColor(context, R.color.airmap_colorFill));
        options.alpha(0.66f);
        return options;
    }

    private PolygonOptions getDefaultRedPolygonOptions() {
        PolygonOptions options = new PolygonOptions();
        options.fillColor(ContextCompat.getColor(getContext(), R.color.airmap_red));
        options.alpha(0.66f);
        return options;
    }

    public static PolylineOptions getDefaultPolylineOptions(Context context) {
        PolylineOptions options = new PolylineOptions();
        options.color(ContextCompat.getColor(context, R.color.colorPrimary));
        options.width(2);
        return options;
    }

    private static MarkerViewOptions getDefaultMarkerOptions(LatLng latLng) {
        MarkerViewOptions options = new MarkerViewOptions();
        options.position(latLng);
        options.icon(cornerIcon);
        options.title(CORNER_TAG);
        options.anchor(0.5f, 0.5f);
        return options;
    }

    private static MarkerViewOptions getDefaultMidpointMarker(LatLng latLng) {
        MarkerViewOptions options = new MarkerViewOptions();
        options.position(latLng);
        options.icon(midpointIcon);
        options.title(MIDPOINT_TAG);
        options.anchor(0.5f, 0.5f);
        return options;
    }

    private static MarkerViewOptions getIntersectionMarker(LatLng latLng) {
        MarkerViewOptions options = new MarkerViewOptions();
        options.position(latLng);
        options.icon(intersectionIcon);
        options.title(INTERSECTION_TAG);
        options.anchor(0.5f, 0.5f);
        return options;
    }

    //Emulate a circle as a polygon with a bunch of sides
    public static ArrayList<LatLng> polygonCircleForCoordinate(LatLng location, double radius) {
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

//    @Override
//    public void onBackPressed() {
//        if (bottomSheetBehavior.getState() != BottomSheetBehavior.STATE_COLLAPSED) {
//            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
//        } else {
//            super.onBackPressed();
//        }
//    }

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
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof FlightDetailsFragment.OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    public boolean onActivityBackPressed() {
        if (bottomSheetBehavior != null) {
            if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                return true;
            }
        }
        return false;
    }

    public interface OnFragmentInteractionListener {
        AirMapFlight getFlight();

        void freehandNextClicked();

        void bottomSheetOpened();

        void bottomSheetClosed();

        TabLayout getTabLayout();
    }
}
