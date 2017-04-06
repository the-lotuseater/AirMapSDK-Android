package com.airmap.airmapsdk.networking.services;

import com.airmap.airmapsdk.models.Coordinate;
import com.airmap.airmapsdk.models.welcome.AirMapWelcomeResult;
import com.airmap.airmapsdk.networking.callbacks.AirMapCallback;
import com.airmap.airmapsdk.networking.callbacks.GenericListOkHttpCallback;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;

/**
 * Created by Vansh Gandhi on 1/12/17.
 * Copyright © 2016 AirMap, Inc. All rights reserved.
 */

public class WelcomeService extends BaseService {

    public static Call getWelcomeSummary(Coordinate coordinate, AirMapCallback<List<AirMapWelcomeResult>> listener) {
        Map<String, String> params = new HashMap<>();
        params.put("latitude", String.valueOf(coordinate.getLatitude()));
        params.put("longitude", String.valueOf(coordinate.getLongitude()));
        return AirMap.getClient().get(welcomeUrl, params, new GenericListOkHttpCallback(listener, AirMapWelcomeResult.class));
    }
}
