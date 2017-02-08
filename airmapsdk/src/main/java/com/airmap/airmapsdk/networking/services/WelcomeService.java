package com.airmap.airmapsdk.networking.services;

import android.content.Context;

import com.airmap.airmapsdk.models.Coordinate;
import com.airmap.airmapsdk.models.welcome.AirMapWelcome;
import com.airmap.airmapsdk.models.welcome.AirMapWelcomeResult;
import com.airmap.airmapsdk.networking.callbacks.AirMapCallback;
import com.airmap.airmapsdk.networking.callbacks.GenericListOkHttpCallback;
import com.airmap.airmapsdk.networking.callbacks.GenericOkHttpCallback;
import com.airmap.airmapsdk.util.Utils;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Created by Vansh Gandhi on 1/12/17.
 * Copyright © 2016 AirMap, Inc. All rights reserved.
 */

public class WelcomeService extends BaseService {

    public static Call getWelcomeSummary(Coordinate coordinate, AirMapCallback<List<AirMapWelcomeResult>> listener) {
        Map<String, String> params = new HashMap<>();
        params.put("latitude", String.valueOf(coordinate.getLatitude()));
        params.put("longitude", String.valueOf(coordinate.getLongitude()));
        return AirMap.getClient().get(welcomeBaseUrl, params, new GenericListOkHttpCallback(listener, AirMapWelcomeResult.class));
    }
}
