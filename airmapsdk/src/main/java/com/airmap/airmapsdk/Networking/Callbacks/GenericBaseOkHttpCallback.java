package com.airmap.airmapsdk.networking.callbacks;

import com.airmap.airmapsdk.models.AirMapBaseModel;
import com.airmap.airmapsdk.util.Utils;

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
}
