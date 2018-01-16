package com.airmap.airmapsdktest;

import android.support.multidex.MultiDexApplication;

import com.airmap.airmapsdk.networking.services.AirMap;
import com.airmap.airmapsdk.util.AirMapConfig;
import com.mapbox.mapboxsdk.Mapbox;

public class MyApplication extends MultiDexApplication {
    @Override
    public void onCreate() {
        super.onCreate();

        AirMap.init(this);
        AirMap.enableLogging(true);

        Mapbox.getInstance(this, AirMapConfig.getMapboxApiKey());
    }
}
