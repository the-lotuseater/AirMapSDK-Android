package com.airmap.airmapsdk.ui.views;

import android.Manifest;
import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.OnLifecycleEvent;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresPermission;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.FrameLayout;

import com.airmap.airmapsdk.AirMapLog;
import com.airmap.airmapsdk.controllers.MapDataController;
import com.airmap.airmapsdk.controllers.MapStyleController;
import com.airmap.airmapsdk.models.status.AirMapAdvisory;
import com.airmap.airmapsdk.models.status.AirMapAirspaceStatus;
import com.airmap.airmapsdk.models.map.AirMapFillLayerStyle;
import com.airmap.airmapsdk.models.map.AirMapLayerStyle;
import com.airmap.airmapsdk.models.map.AirMapLineLayerStyle;
import com.airmap.airmapsdk.models.map.AirMapSymbolLayerStyle;
import com.airmap.airmapsdk.models.rules.AirMapRuleset;
import com.airmap.airmapsdk.networking.services.AirMap;
import com.airmap.airmapsdk.util.Utils;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.plugins.locationlayer.LocationLayerPlugin;
import com.mapbox.mapboxsdk.style.layers.FillLayer;
import com.mapbox.mapboxsdk.style.layers.Filter;
import com.mapbox.mapboxsdk.style.layers.Layer;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.sources.TileSet;
import com.mapbox.mapboxsdk.style.sources.VectorSource;
import com.mapbox.services.commons.geojson.Feature;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by collin@airmap.com on 9/21/17.
 */

public class AirMapMapView extends MapView implements MapView.OnMapChangedListener, MapboxMap.OnMapClickListener, LifecycleObserver {

    private static final String TAG = "AirMapMapView";

    private MapboxMap map;

    private MapStyleController mapStyleController;
    private MapDataController mapDataController;

    // optional callbacks
    private OnMapLoadListener mapLoadListener;
    private OnMapChangedListener mapChangedListener;
    private OnAdvisoryClickListener advisoryClickListener;

    private Set<AirMapRuleset> selectedRulesets;

    public AirMapMapView(@NonNull Context context) {
        super(context);
        init();
    }

    public AirMapMapView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AirMapMapView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        selectedRulesets = new HashSet<>();

        mapDataController = new MapDataController(this, new MapDataController.Callback() {
            @Override
            public void onRulesetsUpdated(List<AirMapRuleset> availableRulesets, List<AirMapRuleset> selectedRulesets) {
                if (mapChangedListener != null) {
                    mapChangedListener.onRulesetsChanged(availableRulesets, selectedRulesets);
                }

                setLayers(selectedRulesets);
            }

            @Override
            public void onAdvisoryStatusUpdated(AirMapAirspaceStatus advisoryStatus) {
                if (mapChangedListener != null) {
                    mapChangedListener.onAdvisoryStatusChanged(advisoryStatus);
                }
            }
        });

        mapStyleController = new MapStyleController(this, new MapStyleController.Callback() {
            @Override
            public void onMapStyleLoaded() {
                mapDataController.onMapLoaded(getMap().getCameraPosition().target);

                mapLoadListener.onMapLoaded();
            }
        });

        addOnMapChangedListener(this);
    }

    public void setRulesets(List<String> preferred, List<String> unpreferred) {
        mapDataController.setRulesets(preferred, unpreferred);
    }

    @Override
    public void getMapAsync(final OnMapReadyCallback callback) {
        super.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(MapboxMap mapboxMap) {
                map = mapboxMap;
                mapStyleController.onMapReady();

                map.setOnMapClickListener(AirMapMapView.this);

                if (callback != null) {
                    callback.onMapReady(mapboxMap);
                }
            }
        });
    }

    @Override
    public void onMapChanged(int change) {
        switch (change) {
            // check if map failed
            case MapView.DID_FAIL_LOADING_MAP: {
                if (mapLoadListener != null) {
                    MapFailure failure = MapFailure.UNKNOWN_FAILURE;

                    /**
                     *  Devices with an inaccurate date/time will not be able to load the mapbox map
                     *  If the "automatic date/time" is disabled on the device and the map fails to load, recommend the user enable it
                     */
                    int autoTimeSetting = Settings.Global.getInt(getContext().getContentResolver(), Settings.Global.AUTO_TIME, 0);
                    if (autoTimeSetting == 0) {
                        failure = MapFailure.INACCURATE_DATE_TIME_FAILURE;
                    } else if (!Utils.isNetworkConnected(getContext())) {
                        failure = MapFailure.NETWORK_CONNECTION_FAILURE;
                    }

                    mapLoadListener.onMapFailed(failure);
                }
                break;
            }
            case MapView.REGION_DID_CHANGE:
            case MapView.REGION_DID_CHANGE_ANIMATED: {
                if (mapDataController != null) {
                    mapDataController.onMapMoved(getMap().getCameraPosition().target);
                }
                break;
            }
            default: {
                break;
            }
        }
    }

    @Override
    public void onMapClick(@NonNull LatLng point) {
        // Launch the Advisory Details Activity

        PointF clickPoint = map.getProjection().toScreenLocation(point);
        RectF clickRect = new RectF(clickPoint.x - 10, clickPoint.y - 10, clickPoint.x + 10, clickPoint.y + 10);
        RectF mapRect = new RectF(getLeft(), getTop(), getRight(), getBottom());
        Filter.Statement filter = Filter.has("airspace_id");
        List<Feature> allFeatures = map.queryRenderedFeatures(mapRect, filter);

        if (allFeatures.size() > 400) {
            //TODO:
//            Toast.makeText(MainActivity.this, R.string.zoom_for_advisory_details, Toast.LENGTH_SHORT).show();
            return;
        }

        List<Feature> selectedFeatures = map.queryRenderedFeatures(clickRect, filter);
        List<AirMapAdvisory> allAdvisories = mapDataController.getCurrentAdvisories();
        if (allAdvisories == null) {
            return;
        }

        Set<AirMapAdvisory> filteredAdvisories = new HashSet<>(); // Include only those with features on map
        AirMapAdvisory clickedAdvisory = null;
        for (AirMapAdvisory advisory : allAdvisories) {
            for (Feature feature : selectedFeatures) {
                if (feature.hasProperty("airspace_id") && advisory.getId().equals(feature.getStringProperty("airspace_id"))) {
                    clickedAdvisory = advisory;
                    break;
                }
            }

            for (Feature feature : allFeatures) {
                if (feature.getStringProperty("airspace_id").equals(advisory.getId())) {
                    filteredAdvisories.add(advisory);
                }
            }

        }

        if (clickedAdvisory != null) {
            advisoryClickListener.onAdvisoryClicked(clickedAdvisory);
        }
    }

    private void setLayers(List<AirMapRuleset> newRulesets) {
        for (AirMapRuleset oldRuleset : selectedRulesets) {
            if (!newRulesets.contains(oldRuleset)) {
                removeMapLayers(oldRuleset.getId(), oldRuleset.getLayers());
            }
        }

        for (AirMapRuleset newRuleset : newRulesets) {
            if (!selectedRulesets.contains(newRuleset)) {
                addMapLayers(newRuleset.getId(), newRuleset.getLayers());
            }
        }

        selectedRulesets = new HashSet<>(newRulesets);
    }

    private void addMapLayers(String sourceId, List<String> layers) {
        //TODO: shouldn't need to add layers if source exists, see removeMapLayers
        if (getMap().getSource(sourceId) != null) {
            AirMapLog.e(TAG, "source id isn't null for: " + sourceId);
        } else {
            String urlTemplates = AirMap.getRulesetTileUrlTemplate(sourceId, layers);
            TileSet tileSet = new TileSet("2.2.0", urlTemplates);
            tileSet.setMaxZoom(15f);
            tileSet.setMinZoom(7f);
            VectorSource tileSource = new VectorSource(sourceId, tileSet);
            getMap().addSource(tileSource);
        }

        for (String sourceLayer : layers) {
            if (TextUtils.isEmpty(sourceLayer)) {
                continue;
            }

            for (AirMapLayerStyle layerStyle : mapStyleController.getMapStyle().getLayerStyles()) {
                if (layerStyle == null || !layerStyle.sourceLayer.equals(sourceLayer) || getMap().getLayer(layerStyle.id + "|" + sourceId + "|new") != null) {
                    continue;
                }

                Layer layer = getMap().getLayerAs(layerStyle.id);
                if (layerStyle instanceof AirMapFillLayerStyle) {
                    FillLayer newLayer = (FillLayer) layerStyle.toMapboxLayer(layer, sourceId);
                    if (newLayer.getId().contains("airmap|tfr")) {
                        addTfrFilter(newLayer);
                    } else if (newLayer.getId().contains("notam")) {
                        addNotamFilter(newLayer);
                    }
                    getMap().addLayerAbove(newLayer, layerStyle.id);
                } else if (layerStyle instanceof AirMapLineLayerStyle) {
                    LineLayer newLayer = (LineLayer) layerStyle.toMapboxLayer(layer, sourceId);
                    if (newLayer.getId().contains("airmap|tfr")) {
                        addTfrFilter(newLayer);
                    } else if (newLayer.getId().contains("notam")) {
                        addNotamFilter(newLayer);
                    }
                    getMap().addLayerAbove(newLayer, layerStyle.id);
                } else if (layerStyle instanceof AirMapSymbolLayerStyle) {
                    getMap().addLayerAbove(layerStyle.toMapboxLayer(layer, sourceId), layerStyle.id);
                }
            }
        }
    }

    private void addTfrFilter(Layer layer) {
        long now = System.currentTimeMillis() / 1000;
        long in4Hrs = now + (4 * 60 * 60);
        Filter.Statement validNowFilter = Filter.all(Filter.lt("start", now), Filter.gt("end", now));
        Filter.Statement startsSoonFilter = Filter.all(Filter.gt("start", now), Filter.lt("start", in4Hrs));
        Filter.Statement permanent = Filter.eq("permanent", "true");
        Filter.Statement hasNoEnd = Filter.all(Filter.notHas("end"), Filter.notHas("base"));
        Filter.Statement filter = Filter.any(validNowFilter, startsSoonFilter, permanent, hasNoEnd);
        if (layer instanceof FillLayer) {
            ((FillLayer) layer).setFilter(filter);
        } else if (layer instanceof LineLayer) {
            ((LineLayer) layer).setFilter(filter);
        }
    }

    private void addNotamFilter(Layer layer) {
        long now = System.currentTimeMillis() / 1000;
        long in4Hrs = now + (4 * 60 * 60);
        Filter.Statement validNowFilter = Filter.all(Filter.lt("start", now), Filter.gt("end", now));
        Filter.Statement startsSoonFilter = Filter.all(Filter.gt("start", now), Filter.lt("start", in4Hrs));
        Filter.Statement permanent = Filter.eq("permanent", "true");
        Filter.Statement hasNoEnd = Filter.all(Filter.notHas("end"), Filter.notHas("base"));
        Filter.Statement filter = Filter.any(validNowFilter, startsSoonFilter, permanent, hasNoEnd);
        if (layer instanceof FillLayer) {
            ((FillLayer) layer).setFilter(filter);
        } else if (layer instanceof LineLayer) {
            ((LineLayer) layer).setFilter(filter);
        }
    }

    public void removeMapLayers(String sourceId, List<String> sourceLayers) {
        //TODO: file bug against mapbox, remove source doesn't seem to be working or at least not after just adding source
        //TODO: to reproduce, open app w/ active flight. adds map layers, removes layers, adds flight map layers
        AirMapLog.e(TAG, "remove source: " + sourceId + " layers: " + TextUtils.join(",", sourceLayers));
        getMap().removeSource(sourceId);

        if (sourceLayers == null || sourceLayers.isEmpty()) {
            return;
        }

        for (String sourceLayer : sourceLayers) {
            for (AirMapLayerStyle layerStyle : mapStyleController.getMapStyle().getLayerStyles()) {
                if (layerStyle != null && layerStyle.sourceLayer.equals(sourceLayer)) {
                    getMap().removeLayer(layerStyle.id + "|" + sourceId + "|new");
                }
            }
        }
    }

    public MapboxMap getMap() {
        return map;
    }

    public void onRulesetSelected(AirMapRuleset ruleset) {
        mapDataController.onRulesetSelected(ruleset);
    }

    public void onRulesetDeselected(AirMapRuleset ruleset) {
        mapDataController.onRulesetDeselected(ruleset);
    }

    public void onRulesetSwitched(AirMapRuleset fromRuleset, AirMapRuleset toRuleset) {
        mapDataController.onRulesetSwitched(fromRuleset, toRuleset);
    }

    // callbacks
    public void setOnMapLoadListener(OnMapLoadListener listener) {
        mapLoadListener = listener;
    }

    public void setOnMapChangedListener(OnMapChangedListener listener) {
        mapChangedListener = listener;
    }

    public void setOnAdvisoryClickListener(OnAdvisoryClickListener listener) {
        advisoryClickListener = listener;
    }

    public interface OnMapLoadListener {
        void onMapLoaded();
        void onMapFailed(MapFailure reason);
    }

    public interface OnMapChangedListener {
        void onRulesetsChanged(List<AirMapRuleset> availableRulesets, List<AirMapRuleset> selectedRulesets);
        void onAdvisoryStatusChanged(AirMapAirspaceStatus status);
    }

    public interface OnAdvisoryClickListener {
        void onAdvisoryClicked(AirMapAdvisory advisory);
    }

    public enum MapFailure {
        INACCURATE_DATE_TIME_FAILURE, NETWORK_CONNECTION_FAILURE, UNKNOWN_FAILURE
    }
}
