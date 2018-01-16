package com.airmap.airmapsdk.controllers;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.airmap.airmapsdk.AirMapLog;
import com.airmap.airmapsdk.Analytics;
import com.airmap.airmapsdk.models.map.AirMapFillLayerStyle;
import com.airmap.airmapsdk.models.map.AirMapLayerStyle;
import com.airmap.airmapsdk.models.map.AirMapLineLayerStyle;
import com.airmap.airmapsdk.models.map.AirMapSymbolLayerStyle;
import com.airmap.airmapsdk.models.map.MapStyle;
import com.airmap.airmapsdk.networking.services.AirMap;
import com.airmap.airmapsdk.networking.services.MappingService;
import com.airmap.airmapsdk.ui.views.AirMapMapView;
import com.airmap.airmapsdk.util.AirMapConstants;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.style.layers.BackgroundLayer;
import com.mapbox.mapboxsdk.style.layers.FillLayer;
import com.mapbox.mapboxsdk.style.layers.Filter;
import com.mapbox.mapboxsdk.style.layers.Layer;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.sources.TileSet;
import com.mapbox.mapboxsdk.style.sources.VectorSource;

import org.json.JSONException;

import java.util.List;

import static com.airmap.airmapsdk.networking.services.MappingService.AirMapMapTheme.Dark;
import static com.airmap.airmapsdk.networking.services.MappingService.AirMapMapTheme.Light;
import static com.airmap.airmapsdk.networking.services.MappingService.AirMapMapTheme.Satellite;
import static com.airmap.airmapsdk.networking.services.MappingService.AirMapMapTheme.Standard;

public class MapStyleController implements MapView.OnMapChangedListener {

    private static final String TAG = "MapStyleController";

    private AirMapMapView map;

    private MappingService.AirMapMapTheme currentTheme;
    private MapStyle mapStyle;
    private Callback callback;

    public MapStyleController(AirMapMapView map, Callback callback) {
        this.map = map;
        this.callback = callback;

        // use last used theme or Standard if none has be saved
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(map.getContext());
        String savedTheme = prefs.getString(AirMapConstants.MAP_STYLE, MappingService.AirMapMapTheme.Standard.toString());
        currentTheme = MappingService.AirMapMapTheme.fromString(savedTheme);

        map.addOnMapChangedListener(this);
    }

    public void onMapReady() {
        loadStyleJSON();
    }

    @Override
    public void onMapChanged(int change) {
        switch (change) {
            case MapView.DID_FINISH_LOADING_STYLE: {
                // Adjust the background overlay opacity to improve map visually on Android
                BackgroundLayer backgroundLayer = map.getMap().getLayerAs("background-overlay");
                if (backgroundLayer != null) {
                    if (currentTheme == MappingService.AirMapMapTheme.Light || currentTheme == MappingService.AirMapMapTheme.Standard) {
                        backgroundLayer.setProperties(PropertyFactory.backgroundOpacity(0.95f));
                    } else if (currentTheme == MappingService.AirMapMapTheme.Dark) {
                        backgroundLayer.setProperties(PropertyFactory.backgroundOpacity(0.9f));
                    }
                }

                try {
                    mapStyle = new MapStyle(map.getMap().getStyleJson());
                } catch (JSONException e) {
                    AirMapLog.e(TAG, "Failed to parse style json", e);
                }

                callback.onMapStyleLoaded();
                break;
            }
        }
    }

    // Updates the map to use a custom style based on theme and selected layers
    public void rotateMapTheme() {
        MappingService.AirMapMapTheme theme = Standard;
        switch (currentTheme) {
            case Standard: {
                Analytics.logEvent(Analytics.Event.map, Analytics.Action.select, "Dark");
                theme = Dark;
                break;
            }
            case Dark: {
                Analytics.logEvent(Analytics.Event.map, Analytics.Action.select, "Light");
                theme = Light;
                break;
            }
            case Light: {
                Analytics.logEvent(Analytics.Event.map, Analytics.Action.select, "Satellite");
                theme = Satellite;
                break;
            }
            case Satellite: {
                Analytics.logEvent(Analytics.Event.map, Analytics.Action.select, "Standard");
                theme = Standard;
                break;
            }
        }

        updateMapTheme(theme);
    }

    public void updateMapTheme(MappingService.AirMapMapTheme theme) {
        callback.onMapStyleReset();

        currentTheme = theme;
        loadStyleJSON();

        PreferenceManager.getDefaultSharedPreferences(map.getContext()).edit().putString(AirMapConstants.MAP_STYLE, currentTheme.toString()).apply();
    }

    public void addMapLayers(String sourceId, List<String> layers) {
        if (map.getMap().getSource(sourceId) != null) {
            AirMapLog.e(TAG, "Source already added for: " + sourceId);
        } else {
            String urlTemplates = AirMap.getRulesetTileUrlTemplate(sourceId, layers);
            TileSet tileSet = new TileSet("2.2.0", urlTemplates);
            tileSet.setMaxZoom(15f);
            tileSet.setMinZoom(7f);
            VectorSource tileSource = new VectorSource(sourceId, tileSet);
            map.getMap().addSource(tileSource);
        }

        for (String sourceLayer : layers) {
            if (TextUtils.isEmpty(sourceLayer)) {
                continue;
            }

            for (AirMapLayerStyle layerStyle : mapStyle.getLayerStyles()) {
                if (layerStyle == null || !layerStyle.sourceLayer.equals(sourceLayer) || map.getMap().getLayer(layerStyle.id + "|" + sourceId + "|new") != null) {
                    continue;
                }

                Layer layer = map.getMap().getLayerAs(layerStyle.id);
                if (layerStyle instanceof AirMapFillLayerStyle) {
                    FillLayer newLayer = (FillLayer) layerStyle.toMapboxLayer(layer, sourceId);
                    if (newLayer.getId().contains("airmap|tfr")) {
                        addTfrFilter(newLayer);
                    } else if (newLayer.getId().contains("notam")) {
                        addNotamFilter(newLayer);
                    }
                    map.getMap().addLayerAbove(newLayer, layerStyle.id);
                } else if (layerStyle instanceof AirMapLineLayerStyle) {
                    LineLayer newLayer = (LineLayer) layerStyle.toMapboxLayer(layer, sourceId);
                    if (newLayer.getId().contains("airmap|tfr")) {
                        addTfrFilter(newLayer);
                    } else if (newLayer.getId().contains("notam")) {
                        addNotamFilter(newLayer);
                    }
                    map.getMap().addLayerAbove(newLayer, layerStyle.id);
                } else if (layerStyle instanceof AirMapSymbolLayerStyle) {
                    map.getMap().addLayerAbove(layerStyle.toMapboxLayer(layer, sourceId), layerStyle.id);
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

        if (sourceLayers == null || sourceLayers.isEmpty()) {
            return;
        }

        for (String sourceLayer : sourceLayers) {
            for (AirMapLayerStyle layerStyle : mapStyle.getLayerStyles()) {
                if (layerStyle != null && layerStyle.sourceLayer.equals(sourceLayer)) {
                    map.getMap().removeLayer(layerStyle.id + "|" + sourceId + "|new");
                }
            }
        }

        map.getMap().removeSource(sourceId);
    }

    private void loadStyleJSON() {
        map.getMap().setStyleUrl(AirMap.getMapStylesUrl(currentTheme));
    }

    public interface Callback {
        void onMapStyleLoaded();
        void onMapStyleReset();
    }
}
