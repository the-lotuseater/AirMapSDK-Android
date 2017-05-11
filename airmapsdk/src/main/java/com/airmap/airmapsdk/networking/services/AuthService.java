package com.airmap.airmapsdk.networking.services;

import android.util.Log;

import com.airmap.airmapsdk.AirMapException;
import com.airmap.airmapsdk.AirMapLog;
import com.airmap.airmapsdk.models.AirMapToken;
import com.airmap.airmapsdk.networking.callbacks.AirMapCallback;
import com.airmap.airmapsdk.networking.callbacks.GenericOkHttpCallback;
import com.airmap.airmapsdk.util.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.Response;

/**
 * Created by Vansh Gandhi on 4/6/17.
 * Copyright © 2016 AirMap, Inc. All rights reserved.
 */

public class AuthService extends BaseService {

    public static String getAuth0Domain() {
        return auth0Domain;
    }

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

    public static void refreshAccessToken(String refreshToken, final AirMapCallback<Void> listener) {
        HttpUrl.Builder urlBuilder = HttpUrl.parse(delegationUrl).newBuilder();
        urlBuilder.addQueryParameter("grant_type", "urn:ietf:params:oauth:grant-type:jwt-bearer");
        urlBuilder.addQueryParameter("api_type", "app");
        urlBuilder.addQueryParameter("client_id", Utils.getClientId());
        urlBuilder.addQueryParameter("refresh_token", refreshToken);
        String url = urlBuilder.build().toString();
        AirMap.getClient().get(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                AirMapLog.e("AuthServices", e.getMessage());
                if (listener != null) {
                    listener.onError(new AirMapException(e.getMessage()));
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    String json = response.body().string();
                    response.body().close();
                    JSONObject jsonObject = new JSONObject(json);
                    String idToken = jsonObject.getString("id_token");
                    AirMap.setAuthToken(idToken);
                    if (listener != null) {
                        listener.onSuccess(null);
                    }
                } catch (JSONException e) {
                    AirMapLog.e("AuthServices", e.getMessage());
                    if (listener != null) {
                        listener.onError(new AirMapException(response.code(), e.getMessage()));
                    }
                }
            }
        });
    }

    public static void getFirebaseToken(final AirMapCallback<String> listener) {
        HttpUrl.Builder urlBuilder = HttpUrl.parse(delegationUrl).newBuilder();
        urlBuilder.addQueryParameter("grant_type", "urn:ietf:params:oauth:grant-type:jwt-bearer");
        urlBuilder.addQueryParameter("client_id", Utils.getClientId());
        urlBuilder.addQueryParameter("id_token", AirMap.getAuthToken());
        urlBuilder.addQueryParameter("scope", "openid");
        urlBuilder.addQueryParameter("device", "android_app");
        String url = urlBuilder.build().toString();
        AirMap.getClient().get(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (listener != null) {
                    listener.onError(new AirMapException(e.getMessage()));
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    String json = response.body().string();
                    response.body().close();
                    JSONObject jsonObject = new JSONObject(json);
                    String customToken = jsonObject.getString("id_token");
                    if (listener != null) {
                        listener.onSuccess(customToken);
                    }
                } catch (JSONException e) {
                    AirMapLog.e("AuthServices", e.getMessage());
                    if (listener != null) {
                        listener.onError(new AirMapException(response.code(), e.getMessage()));
                    }
                }
            }
        });
    }
}
