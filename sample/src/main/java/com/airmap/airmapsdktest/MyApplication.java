package com.airmap.airmapsdktest;

import android.app.Application;
import android.support.multidex.MultiDexApplication;

import com.airmap.airmapsdk.networking.services.AirMap;
import com.mapbox.mapboxsdk.Mapbox;

import static com.airmap.airmapsdk.util.Utils.getMapboxApiKey;

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
        Mapbox.getInstance(this, getMapboxApiKey());
    }
}
