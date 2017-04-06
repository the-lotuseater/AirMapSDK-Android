package com.airmap.airmapsdk.networking.services;

import com.airmap.airmapsdk.AirMapException;
import com.airmap.airmapsdk.models.AirMapToken;
import com.airmap.airmapsdk.networking.callbacks.AirMapCallback;
import com.airmap.airmapsdk.networking.callbacks.GenericOkHttpCallback;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Vansh Gandhi on 4/6/17.
 * Copyright © 2016 AirMap, Inc. All rights reserved.
 */

public class AuthService extends BaseService {

    public static void performAnonymousLogin(String userId, final AirMapCallback<Void> callback) {
        String url = anonymousLoginUrl;
        Map<String, String> params = new HashMap<>();
        params.put("user_id", userId);
        AirMap.getClient().post(url, params, new GenericOkHttpCallback(new AirMapCallback<AirMapToken>() {
            @Override
            public void onSuccess(AirMapToken response) {
                AirMap.setAuthToken(response.getAuthToken());
                callback.onSuccess(null);
            }

            @Override
            public void onError(AirMapException e) {
                callback.onError(e);
            }
        }, AirMapToken.class));
    }
}
