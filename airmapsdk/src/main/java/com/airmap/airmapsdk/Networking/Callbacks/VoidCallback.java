package com.airmap.airmapsdk.Networking.Callbacks;

import com.airmap.airmapsdk.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Response;

/**
 * Created by Vansh Gandhi on 6/23/16.
 * Copyright © 2016 AirMap, Inc. All rights reserved.
 */
public class VoidCallback extends GenericBaseOkHttpCallback {
    public VoidCallback(AirMapCallback<Void> listener) {
        super(listener, null);
    }

    @Override
    public void onResponse(Call call, Response response) {
        if (listener == null) {
            return; //Don't need to do anything if no listener was provided
        }
        String jsonString;
        try {
            jsonString = response.body().string();
        } catch (IOException e) {
            Utils.error(listener, e);
            return;
        }
        response.body().close();
        JSONObject result = null;
        try {
            result = new JSONObject(jsonString);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (!response.isSuccessful() || !Utils.statusSuccessful(result)) {
            Utils.error(listener, response.code(), result);
        } else {
            listener.onSuccess(null);
        }
    }
}