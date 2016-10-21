package com.airmap.freehand;

import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.PorterDuff;
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
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.airmap.airmapsdk.AirMapException;
import com.airmap.airmapsdk.Utils;
import com.airmap.airmapsdk.models.Coordinate;
import com.airmap.airmapsdk.models.status.AirMapStatus;
import com.airmap.airmapsdk.networking.callbacks.AirMapCallback;
import com.airmap.airmapsdk.networking.services.AirMap;
import com.mapbox.mapboxsdk.MapboxAccountManager;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.annotations.MarkerView;
import com.mapbox.mapboxsdk.annotations.MarkerViewOptions;
import com.mapbox.mapboxsdk.annotations.MultiPoint;
import com.mapbox.mapboxsdk.annotations.Polygon;
import com.mapbox.mapboxsdk.annotations.PolygonOptions;
import com.mapbox.mapboxsdk.annotations.Polyline;
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

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.airmap.airmapsdk.Utils.getBufferPresets;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, DrawingCallback, MapboxMap.OnCameraChangeListener, AirMapCallback<AirMapStatus> {

    private static final String CIRCLE_TAG = "circle";
    private static final String PATH_TAG = "path";
    private static final String POLYGON_TAG = "polygon";
    private static final String MIDPOINT_TAG = "midpoint";
    private static final String CORNER_TAG = "corner";

    static Icon draggerIcon;
    static Icon addCircleIcon;
    static Icon intersectionIcon;

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.tabs) TabLayout tabLayout;
    @BindView(R.id.seekbar_container) RelativeLayout seekBarContainer;
    @BindView(R.id.seekbar) SeekBar seekBar;
    @BindView(R.id.label) TextView seekBarLabelTextView;
    @BindView(R.id.seekbar_value) TextView seekBarValueTextView;
    @BindView(R.id.action_button) ImageViewSwitch enableDrawingSwitch;
    @BindView(R.id.delete_button) ImageView deleteButton;
    @BindView(R.id.tip_text) TextView tipTextView;
    @BindView(R.id.map) MapView mapView;
    @BindView(R.id.drawFrame) DrawingBoard drawingBoard;
    @BindView(R.id.next_button) Button nextButton;

    MapboxMap map;
    int indexOfThickLine;
    List<MultiPoint> shapesOnScreen;
    List<MarkerView> draggerAnnotations;
    double zoomAtDraw = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MapboxAccountManager.start(this, getString(R.string.access_token));
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setupToolbar();
        setupSwitch();
        setupMap(savedInstanceState);
        drawingBoard.setDoneDrawingCallback(this);
        indexOfThickLine = -1;
        shapesOnScreen = new ArrayList<>();
        draggerAnnotations = new ArrayList<>();
        draggerIcon = IconFactory.getInstance(this).fromResource(R.drawable.white_circle);
        addCircleIcon = IconFactory.getInstance(this).fromResource(R.drawable.gray_circle);
        intersectionIcon = IconFactory.getInstance(this).fromResource(R.drawable.intersection_circle);
        AirMap.init(this);
    }

    private void setupToolbar() {
        toolbar.setTitle(R.string.create_flight);
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
                    updateTip(R.string.freehand_tip);
                    drawingBoard.setPolygonMode(false); //Doesn't close the drawn path
                    drawingBoard.setVisibility(View.VISIBLE);
                    enableDrawingSwitch.setVisibility(View.VISIBLE);
                    enableDrawingSwitch.setChecked(false);
                    showDeleteButton(false);
                } else if (POLYGON_TAG.equals(tag)) {
                    hideSeekBar();
                    updateTip(R.string.freehand_tip);
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
        tabLayout.addTab(getTab(tabLayout, R.string.circle_radius, R.drawable.ic_circle, CIRCLE_TAG));
        tabLayout.addTab(getTab(tabLayout, R.string.path_buffer, R.drawable.ic_path, PATH_TAG));
        tabLayout.addTab(getTab(tabLayout, R.string.polygon, R.drawable.ic_polygon, POLYGON_TAG));
    }

    public static TabLayout.Tab getTab(TabLayout tabLayout, @StringRes int textId, @DrawableRes int iconId, String id) {
        TabLayout.Tab tab = tabLayout.newTab();
        tab.setText(textId);
        tab.setIcon(iconId);
        tab.setTag(id);
        return tab;
    }

    private void setupSwitch() {
        enableDrawingSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                drawingBoard.setVisibility(isChecked ? View.VISIBLE : View.INVISIBLE);
                if (isChecked) {
                    if (drawingBoard.isPolygonMode()) {
                        updateTip(R.string.draw_freehand_shape);
                    } else {
                        updateTip(R.string.draw_freehand_path);
                    }
                } else {
                    updateTip(R.string.freehand_tip);
                }
            }
        });
    }

    @OnClick(R.id.delete_button)
    public void deleteButtonClicked() {
        clear();
        showDeleteButton(false);
        if (tabLayout.getSelectedTabPosition() == 2) { //Reset in case we displayed the intersection error message
            updateTip(R.string.freehand_tip);
        }
    }

    public void clear() {
        zoomAtDraw = -1;
        map.removeAnnotations();
        shapesOnScreen.clear();
        updateButtonColor(null);
        if (draggerAnnotations != null && !draggerAnnotations.isEmpty()) {
            map.removeAnnotations(draggerAnnotations);
            draggerAnnotations.clear();
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
        updateTip(R.string.done_drawing_tip);
        if (drawingBoard.isPolygonMode()) {
            drawPolygon(points);
        } else { //Path mode
            drawPath(points);
        }
        zoomAtDraw = map.getCameraPosition().zoom;
    }

    private void drawCircle(double radius) {
        clear();
        LatLng center = map.getCameraPosition().target;
        List<LatLng> circlePoints = polygonCircleForCoordinate(center, radius);
        PolylineOptions polylineOptions = getDefaultPolylineOptions().addAll(circlePoints).add(circlePoints.get(0));
        shapesOnScreen.add(map.addPolygon(getDefaultPolygonOptions().addAll(circlePoints)));
        shapesOnScreen.add(map.addPolyline(polylineOptions));
        draggerAnnotations.add(map.addMarker(getDefaultMarkerOptions(center)));
    }

    private void zoomToCircle() {
        for (MultiPoint multiPoint : shapesOnScreen) {
            if (multiPoint.getPoints().size() == 90) { //45 sides to the circle
                LatLngBounds bounds = new LatLngBounds.Builder().includes(multiPoint.getPoints()).build();
                map.easeCamera(CameraUpdateFactory.newLatLngBounds(bounds, 150));
                break;
            }
        }
    }

    public void drawPath(List<PointF> line) {
        clear();
        PolylineOptions thickLine = new PolylineOptions();
        PolylineOptions thinLine = getDefaultPolylineOptions();
        thickLine.color(getColor(R.color.colorFill));
        thickLine.alpha(0.66f);
        thickLine.width(getPathWidth(seekBar.getProgress()));
        List<LatLng> midPoints = getLatLngsFromPointFs(getMidpoints(line));
        List<LatLng> points = getLatLngsFromPointFs(line);
        for (int i = 0; i < points.size(); i++) {
            LatLng point = points.get(i);
            thickLine.add(point);
            thinLine.add(point);
            draggerAnnotations.add(map.addMarker(getDefaultMarkerOptions(point)));
            if (i < midPoints.size()) {
                LatLng midPoint = midPoints.get(i);
                draggerAnnotations.add(map.addMarker(getDefaultAddPointMarker(midPoint)));
            }
        }

        //TODO: Rounded corners at the end of the line
        shapesOnScreen.add(map.addPolyline(thickLine));
        indexOfThickLine = shapesOnScreen.size() - 1;
        shapesOnScreen.add(map.addPolyline(thinLine));

        LatLngBounds bounds = new LatLngBounds.Builder().includes(thickLine.getPoints()).build();
        map.easeCamera(CameraUpdateFactory.newLatLngBounds(bounds, 150));
    }

    public void drawPolygon(List<PointF> pointsDrawn) {
        clear();
        checkForIntersections(pointsDrawn);
        PointF first = new PointF(pointsDrawn.get(0).x, pointsDrawn.get(0).y); //Making copy of the object so MapBox doesn't mess up the point
        PointF last = new PointF(pointsDrawn.get(pointsDrawn.size() - 1).x, pointsDrawn.get(pointsDrawn.size() - 1).y); //Making copies so MapBox doesn't mess up the point
        if (drawingBoard.isPolygonMode()) {
            double distance = PointMath.distanceBetween(first, last);
            if (distance < 100) { //Check if the two points are close, based on screen distance between points, rather than LatLng distance
                pointsDrawn.set(pointsDrawn.size() - 1, first); //Ignore the last drawn point if it was close to the starting point
            } else {
                pointsDrawn.add(first);
            }
        }
        PolygonOptions polygonOptions = getDefaultPolygonOptions();
        PolylineOptions polylineOptions = getDefaultPolylineOptions();
        List<LatLng> midPoints = getLatLngsFromPointFs(getMidpoints(pointsDrawn));
        List<LatLng> points = getLatLngsFromPointFs(pointsDrawn);
        //At this point, until MapBox fixes their fromScreenLocation bug, pointsDrawn has been tainted and is unusable
        for (int i = 0; i < points.size(); i++) {
            LatLng latLng = points.get(i);
            if (i < midPoints.size()) { //There is one less midpoint than total points
                LatLng midPoint = midPoints.get(i);
                draggerAnnotations.add(map.addMarker(getDefaultAddPointMarker(midPoint)));
            }
            polygonOptions.add(latLng);
            polylineOptions.add(latLng);
            if (i != points.size() - 1) { //Don't add the last point because it's the same as the first point
                draggerAnnotations.add(map.addMarker(getDefaultMarkerOptions(latLng)));
            }
        }
        polylineOptions.add(polylineOptions.getPoints().get(0)); //Close the polygon
        shapesOnScreen.add(map.addPolygon(polygonOptions));
        shapesOnScreen.add(map.addPolyline(polylineOptions));

        LatLngBounds bounds = new LatLngBounds.Builder().includes(polylineOptions.getPoints()).build();
        map.easeCamera(CameraUpdateFactory.newLatLngBounds(bounds, 150));
    }

    private void checkForIntersections(List<PointF> pointsDrawn) {
        List<PointF> intersections = PointMath.findIntersections(pointsDrawn);
        if (!intersections.isEmpty()) {
            for (PointF i : intersections) {
                LatLng intersection = map.getProjection().fromScreenLocation(i);
                map.addMarker(getIntersectionMarker(intersection));
            }
            updateTip(R.string.error_overlap, R.drawable.rounded_corners_red);
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

    private static List<PointF> getMidpoints(List<PointF> shape) {
        List<PointF> midpoints = new ArrayList<>();
        for (int i = 1; i < shape.size(); i++) {
            float x = (shape.get(i - 1).x + shape.get(i).x) / 2;
            float y = (shape.get(i - 1).y + shape.get(i).y) / 2;
            midpoints.add(new PointF(x, y));
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
                System.out.println(marker);
                return true;
            }
        });
//        mapView.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                if (event == null) {
//                    return false;
//                }
//                for (DraggableMarker marker : draggerAnnotations) {
//                    PointF mPos = map.getProjection().toScreenLocation(marker.getPosition());
//                    PointF tPos = new PointF(event.getX(), event.getY());
//                    if (PointMath.distanceBetween(mPos, tPos) < 35) {
//                        marker.drag(event);
//                        //TODO: Dashed line
//                        //Also is this really the best way to do this?
//                        return true; //Only want to drag one marker
//                    }
//                }
//                return false;
//            }
//        });
        setupTabs();
    }

    private void hideSeekBar() {
        seekBarContainer.setVisibility(View.GONE);
        seekBar.setOnSeekBarChangeListener(null);
        seekBar.setProgress(0);
    }

    private void showSeekBarForCircle() {
        seekBarContainer.setVisibility(View.VISIBLE);
        seekBarLabelTextView.setText(R.string.radius);
        seekBar.setOnSeekBarChangeListener(null); //This is needed because when setting max, it might cause progress to change, which we don't want
        seekBar.setMax(getBufferPresets().length - 1);
        seekBar.setProgress(0);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                seekBarValueTextView.setText(getBufferPresets()[progress].label);
                drawCircle(getBufferPresets()[progress].value.doubleValue());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Coordinate coordinate = new Coordinate(map.getCameraPosition().target);
                AirMap.checkCoordinate(coordinate, null, null, null, false, null, MainActivity.this);
                zoomToCircle();
            }
        });
        seekBar.setProgress(18); //1000 ft
    }

    private void showSeekBarForPath() {
        seekBarContainer.setVisibility(View.VISIBLE);
        seekBarLabelTextView.setText(R.string.width);
        seekBar.setOnSeekBarChangeListener(null); //This is needed because when setting max, it might cause progress to change, which we don't want since it would call the listener
        seekBar.setMax(Utils.getBufferPresets().length - 1);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                seekBarValueTextView.setText(getBufferPresets()[progress].label);
                if (indexOfThickLine != -1 && !shapesOnScreen.isEmpty()) {
                    Polyline polyline = (Polyline) shapesOnScreen.get(indexOfThickLine);
                    polyline.setWidth(getPathWidth(progress));
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
            for (MarkerView marker : draggerAnnotations) {
                if (marker.getTitle().equals(MIDPOINT_TAG)) {
                    marker.setVisible(zoomDiff < 1);
                } else {
                    marker.setVisible(zoomDiff < 2);
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
        enableDrawingSwitch.setVisibility(show ? View.GONE: View.VISIBLE);
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

    @OnClick(R.id.next_button)
    public void onNextButtonClick() {
        if (shapesOnScreen == null || shapesOnScreen.isEmpty()) {
            return;
        }
        for (MultiPoint multiPoint : shapesOnScreen) {
            if (tabLayout.getSelectedTabPosition() == 0 && multiPoint instanceof Polygon) {
                //Circle //TODO: Store the center and radius of circle. Don't base it on the polygon
            } else if (tabLayout.getSelectedTabPosition() == 1 && multiPoint instanceof Polyline) {
                //Path
                List<Position> positions = new ArrayList<>();
                for (LatLng latLng : multiPoint.getPoints()) {
                    positions.add(Position.fromCoordinates(latLng.getLongitude(), latLng.getLatitude()));
                }
                LineString line = LineString.fromCoordinates(positions);
                String json = line.toJson(); //TODO: Verify this is in the correct format
            } else if (tabLayout.getSelectedTabPosition() == 2 && multiPoint instanceof Polygon) {
                List<List<Position>> positions = new ArrayList<>();
                positions.add(new ArrayList<Position>());
                for (LatLng latLng : multiPoint.getPoints()) {
                    positions.get(0).add(Position.fromCoordinates(latLng.getLongitude(), latLng.getLatitude()));
                }
                com.mapbox.services.commons.geojson.Polygon polygon = com.mapbox.services.commons.geojson.Polygon.fromCoordinates(positions);
                String json = polygon.toJson(); //TODO: Verify this is in the correct format
            }
        }
    }

    private PolygonOptions getDefaultPolygonOptions() {
        PolygonOptions options = new PolygonOptions();
        options.fillColor(getColor(R.color.colorFill));
        options.alpha(0.66f);
        return options;
    }

    private PolylineOptions getDefaultPolylineOptions() {
        PolylineOptions options = new PolylineOptions();
        options.color(getColor(R.color.colorPrimary));
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

    private static MarkerViewOptions getDefaultAddPointMarker(LatLng latLng) {
        MarkerViewOptions options = new MarkerViewOptions();
        options.position(latLng);
        options.icon(addCircleIcon);
        options.title(MIDPOINT_TAG);
        options.anchor(0.5f, 0.5f);
        return options;
    }

    private static MarkerOptions getIntersectionMarker(LatLng latLng) {
        MarkerOptions options = new MarkerOptions();
        options.position(latLng);
        options.icon(intersectionIcon);
        return options;
    }

    //Emulate a circle as a polygon with a bunch of sides
    private static ArrayList<LatLng> polygonCircleForCoordinate(LatLng location, double radius) {
        int degreesBetweenPoints = 4; //45 sides
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
