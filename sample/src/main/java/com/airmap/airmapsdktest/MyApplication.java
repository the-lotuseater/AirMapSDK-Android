package com.airmap.airmapsdktest;

import android.support.multidex.MultiDexApplication;

import com.airmap.airmapsdk.networking.services.AirMap;
import com.airmap.airmapsdk.util.AirMapConfig;
import com.mapbox.mapboxsdk.Mapbox;

/**
 * Created by Vansh Gandhi on 8/12/16.
 * Copyright © 2016 AirMap, Inc. All rights reserved.
 */
public class MyApplication extends MultiDexApplication {
    @Override
    public void onCreate() {
        super.onCreate();

        AirMap.init(this);
        AirMap.enableLogging(true);

        Mapbox.getInstance(this, AirMapConfig.getMapboxApiKey());
    }
}
