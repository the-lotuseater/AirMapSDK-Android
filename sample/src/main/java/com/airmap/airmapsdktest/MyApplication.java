package com.airmap.airmapsdktest;

import android.app.Application;

import com.airmap.airmapsdk.Networking.Services.AirMap;
import com.airmap.airmapsdk.Utils;
import com.mapbox.mapboxsdk.MapboxAccountManager;

/**
 * Created by Vansh Gandhi on 8/12/16.
 * Copyright © 2016 AirMap, Inc. All rights reserved.
 */
public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        AirMap.init(this);
        MapboxAccountManager.start(this, Utils.getMapboxApiKey());
    }
}
