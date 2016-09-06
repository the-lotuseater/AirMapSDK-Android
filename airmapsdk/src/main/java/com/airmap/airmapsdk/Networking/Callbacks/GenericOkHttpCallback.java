package com.airmap.airmapsdk.networking.callbacks;

import com.airmap.airmapsdk.AirMapLog;
import com.airmap.airmapsdk.models.AirMapBaseModel;
import com.airmap.airmapsdk.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Response;

/**
 * Created by Vansh Gandhi on 7/21/16.
 * Copyright © 2016 AirMap, Inc. All rights reserved.
 */
public class GenericOkHttpCallback extends GenericBaseOkHttpCallback {

    public GenericOkHttpCallback(AirMapCallback listener, Class<? extends AirMapBaseModel> classToInstantiate) {
        super(listener, classToInstantiate);
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
            AirMapLog.e("AirMapCallback", jsonString);
        }
        if (!response.isSuccessful() || !Utils.statusSuccessful(result)) {
            Utils.error(listener, response.code(), result);
            return;
        }
        JSONObject jsonObject = result.optJSONObject("data");
        if (jsonObject == null && !result.isNull("data")) {
            Utils.error(listener, response.code(), result); //There was a parsing exception most likely
        } else if (jsonObject == null || result.isNull("data") || jsonObject.length() == 0) {
            listener.onSuccess(null);  //There was no data inside the data object (length 0, or object had value null)
        } else {
            try {
                AirMapBaseModel model = classToInstantiate.newInstance().constructFromJson(jsonObject);
                listener.onSuccess(model);
            } catch (InstantiationException e) {
                e.printStackTrace();
                Utils.error(listener, e);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                Utils.error(listener, e);
            }
        }
    }
}