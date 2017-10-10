package com.airmap.airmapsdk.controllers;

import android.os.Handler;

import com.airmap.airmapsdk.AirMapException;
import com.airmap.airmapsdk.AirMapLog;
import com.airmap.airmapsdk.models.map.MapStyle;
import com.airmap.airmapsdk.networking.callbacks.AirMapCallback;
import com.airmap.airmapsdk.networking.services.AirMap;
import com.airmap.airmapsdk.networking.services.MappingService;
import com.airmap.airmapsdk.ui.views.AirMapMapView;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.style.layers.BackgroundLayer;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;

import org.json.JSONObject;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func2;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

/**
 * Created by collin@airmap.com on 9/26/17.
 */

public class MapStyleController implements MapView.OnMapChangedListener {

    private static final String TAG = "MapStyleController";

    private AirMapMapView map;

    private MappingService.AirMapMapTheme currentTheme;
    private MapStyle mapStyle;
    private PublishSubject<MapStyle> localMapStylePublishSubject;
    private PublishSubject<Boolean> mapboxMapStylePublishSubject;
    private Subscription mapStyleSubscription;

    private Callback callback;

    public MapStyleController(AirMapMapView map, Callback callback) {
        this.map = map;
        this.callback = callback;

        localMapStylePublishSubject = PublishSubject.create();
        mapboxMapStylePublishSubject = PublishSubject.create();

        currentTheme = MappingService.AirMapMapTheme.Standard;

        map.getMapView().addOnMapChangedListener(this);
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
                mapboxMapStylePublishSubject.onNext(true);
                break;
            }
        }
    }

    private void setupSubscription() {
        if (mapStyleSubscription != null) {
            mapStyleSubscription.unsubscribe();
        }

        mapStyleSubscription = Observable.combineLatest(localMapStylePublishSubject.asObservable(), mapboxMapStylePublishSubject.asObservable(),
                new Func2<MapStyle, Boolean, MapStyle>() {
                    @Override
                    public MapStyle call(MapStyle mapStyle, Boolean aBoolean) {
                        return mapStyle;
                    }
                })
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<MapStyle>() {
                    @Override
                    public void call(MapStyle mapStyle) {
                        callback.onMapStyleLoaded();
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        AirMapLog.e(TAG, "Error with combining latest map styles", throwable);
                    }
                });
    }

    // Updates the map to use a custom style based on theme and selected layers
    public void updateMapThemeAndLayers(MappingService.AirMapMapTheme theme) {
        if (map == null) {
            return;
        }
        currentTheme = theme;
        loadStyleJSON(3);

//        PreferenceManager.getDefaultSharedPreferences(this).edit().putString(Constants.MAP_STYLE, currentTheme.toString()).apply();
    }

    private void loadStyleJSON(final int retries) {
        setupSubscription();

        AirMap.getMapStylesJson(MappingService.AirMapMapTheme.Standard, new AirMapCallback<JSONObject>() {
            @Override
            public void onSuccess(JSONObject response) {
                AirMapLog.i(TAG, "map style loaded locally");
                mapStyle = new MapStyle(response);
                localMapStylePublishSubject.onNext(mapStyle);
            }

            @Override
            public void onError(AirMapException e) {
                AirMapLog.e(TAG, "Failed to load map style json", e);
                if (retries > 0) {
                    // wait half second before retrying
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            loadStyleJSON(retries - 1);
                        }
                    }, 500);
                }
            }
        });

        map.getMap().setStyleUrl(AirMap.getMapStylesUrl(MappingService.AirMapMapTheme.Standard));
    }

    public MapStyle getMapStyle() {
        return mapStyle;
    }

    public interface Callback {
        void onMapStyleLoaded();
    }
}
