package com.airmap.airmapsdk.networking.callbacks;

import android.os.Handler;
import android.os.Looper;

import com.airmap.airmapsdk.models.AirMapBaseModel;
import com.airmap.airmapsdk.util.Utils;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Response;

/**
 * Created by Vansh Gandhi on 7/21/16.
 * Copyright © 2016 AirMap, Inc. All rights reserved.
 */
abstract class GenericBaseOkHttpCallback implements okhttp3.Callback {

    final AirMapCallback listener;
    final Class<? extends AirMapBaseModel> classToInstantiate;

    GenericBaseOkHttpCallback(AirMapCallback listener, Class<? extends AirMapBaseModel> classToInstantiate) {
        this.listener = listener;
        this.classToInstantiate = classToInstantiate;
    }

    @Override
    public void onFailure(Call call, IOException e) {
        Utils.error(listener, e);
    }

    @Override
    public abstract void onResponse(Call call, Response response);

    protected void success(final Object response) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                listener.onSuccess(response);
            }
        });
    }

    protected void failed(final Exception e) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Utils.error(listener, e);
            }
        });
    }

    protected void failed(final int code, final JSONObject jsonObject) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Utils.error(listener, code, jsonObject);
            }
        });
    }
}
