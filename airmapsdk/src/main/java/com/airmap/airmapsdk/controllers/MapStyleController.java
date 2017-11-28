package com.airmap.airmapsdk.controllers;

import com.airmap.airmapsdk.AirMapLog;
import com.airmap.airmapsdk.models.map.MapStyle;
import com.airmap.airmapsdk.networking.services.AirMap;
import com.airmap.airmapsdk.networking.services.MappingService;
import com.airmap.airmapsdk.ui.views.AirMapMapView;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.style.layers.BackgroundLayer;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.sources.VectorSource;

import org.json.JSONException;

/**
 * Created by collin@airmap.com on 9/26/17.
 */
public class MapStyleController implements MapView.OnMapChangedListener {

    private static final String TAG = "MapStyleController";

    private AirMapMapView map;

    private MappingService.AirMapMapTheme currentTheme;
    private MapStyle mapStyle;
    private Callback callback;

    public MapStyleController(AirMapMapView map, Callback callback) {
        this.map = map;
        this.callback = callback;

        currentTheme = MappingService.AirMapMapTheme.Standard;

        map.addOnMapChangedListener(this);
    }

    public void onMapReady() {
        loadStyleJSON(3);
    }

    @Override
    public void onMapChanged(int change) {
        switch (change) {
            case MapView.DID_FINISH_LOADING_MAP:
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

                if (map.getMap().getSource("airmap") == null) {
                    map.getMap().addSource(new VectorSource("airmap", "www.airmap.com"));
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
    public void updateMapThemeAndLayers(MappingService.AirMapMapTheme theme) {
        if (map == null) {
            return;
        }
        currentTheme = theme;
        loadStyleJSON(3);
    }

    private void loadStyleJSON(final int retries) {
        map.getMap().setStyleUrl(AirMap.getMapStylesUrl(MappingService.AirMapMapTheme.Standard));
    }

    public MapStyle getMapStyle() {
        return mapStyle;
    }

    public interface Callback {
        void onMapStyleLoaded();
    }
}
