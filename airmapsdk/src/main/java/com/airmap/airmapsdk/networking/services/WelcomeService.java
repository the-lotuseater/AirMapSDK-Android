package com.airmap.airmapsdk.networking.services;

import android.content.Context;

import com.airmap.airmapsdk.models.Coordinate;
import com.airmap.airmapsdk.models.welcome.AirMapWelcome;
import com.airmap.airmapsdk.networking.callbacks.AirMapCallback;
import com.airmap.airmapsdk.networking.callbacks.GenericOkHttpCallback;
import com.airmap.airmapsdk.util.Utils;

import java.io.IOException;
import java.util.HashMap;
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

    //TODO: Remove context. only necessary since we're reading from assets to mock
    public static Call getWelcomeSummary(Context context, Coordinate coordinate, AirMapCallback<AirMapWelcome> listener) {
        try {
            Map<String, String> params = new HashMap<>();
            params.put("latitude", String.valueOf(coordinate.getLatitude()));
            params.put("longitude", String.valueOf(coordinate.getLongitude()));
            String json = Utils.readInputStreamAsString(context.getAssets().open("welcome_response_mock.json"));
            Request request = new Request.Builder().url(urlBodyFromMap(welcomeBaseUrl, params)).get().tag(welcomeBaseUrl).build();
            Callback callback = new GenericOkHttpCallback(listener, AirMapWelcome.class);
            callback.onResponse(null, new Response.Builder().code(200).body(ResponseBody.create(null, json)).protocol(Protocol.HTTP_2).request(request).build());
//            return AirMap.getClient().get(welcomeBaseUrl, params, new GenericOkHttpCallback(listener, AirMapWelcome.class));
        } catch (IOException e) {
            Utils.error(listener, e);
            e.printStackTrace();
        }
        return null;
    }


    //TODO: Get rid of this when api implemented
    private static HttpUrl urlBodyFromMap(String base, Map<String, String> map) {
        HttpUrl.Builder builder = HttpUrl.parse(base).newBuilder(base);
        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (entry.getValue() != null) {
                builder.addEncodedQueryParameter(entry.getKey(), entry.getValue());
            }
        }
        return builder.build();
    }
}
