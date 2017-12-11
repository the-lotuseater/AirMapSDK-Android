package com.airmap.airmapsdk.controllers;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.airmap.airmapsdk.AirMapLog;
import com.airmap.airmapsdk.Analytics;
import com.airmap.airmapsdk.models.map.MapStyle;
import com.airmap.airmapsdk.networking.services.AirMap;
import com.airmap.airmapsdk.networking.services.MappingService;
import com.airmap.airmapsdk.ui.views.AirMapMapView;
import com.airmap.airmapsdk.util.AirMapConstants;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.style.layers.BackgroundLayer;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.sources.VectorSource;

import org.json.JSONException;

import static com.airmap.airmapsdk.networking.services.MappingService.AirMapMapTheme.Dark;
import static com.airmap.airmapsdk.networking.services.MappingService.AirMapMapTheme.Light;
import static com.airmap.airmapsdk.networking.services.MappingService.AirMapMapTheme.Satellite;
import static com.airmap.airmapsdk.networking.services.MappingService.AirMapMapTheme.Standard;

/**
 * Created by collin@airmap.com on 9/26/17.
 */
public class MapStyleController implements MapView.OnMapChangedListener {

    private static final String TAG = "MapStyleController";

    private AirMapMapView map;

    private MappingService.AirMapMapTheme currentTheme;
    private MapStyle mapStyle;
    private Callback callback;

    private SharedPreferences prefs;

    public MapStyleController(AirMapMapView map, Callback callback) {
        this.map = map;
        this.callback = callback;

        // use last used theme or Standard if none has be saved
        prefs = PreferenceManager.getDefaultSharedPreferences(map.getContext());
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
                    map.getMap().addSource(new VectorSource("airmap", "mapbox://mapbox.mapbox-terrain-v2,mapbox.mapbox-streets-v7"));
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

    private void loadStyleJSON() {
        map.getMap().setStyleUrl(AirMap.getMapStylesUrl(currentTheme));
    }

    public MapStyle getMapStyle() {
        return mapStyle;
    }

    public interface Callback {
        void onMapStyleLoaded();
        void onMapStyleReset();
    }
}
