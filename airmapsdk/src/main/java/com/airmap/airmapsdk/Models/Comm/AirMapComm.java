package com.airmap.airmapsdk.models.comm;

import android.util.Base64;

import com.airmap.airmapsdk.models.AirMapBaseModel;

import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by Vansh Gandhi on 6/15/16.
 * Copyright © 2016 AirMap, Inc. All rights reserved.
 */
@SuppressWarnings("unused")
public class AirMapComm implements Serializable, AirMapBaseModel {
    private byte[] key;

    public AirMapComm(JSONObject commJson) {
        constructFromJson(commJson);
    }

    public AirMapComm() {
    }

    @Override
    public AirMapComm constructFromJson(JSONObject json) {
        if (json != null) {
            setKey(Base64.decode(json.optString("key"), Base64.DEFAULT));
        }

        return this;
    }

    public byte[] getKey() {
        return key;
    }

    public AirMapComm setKey(byte[] key) {
        this.key = key;
        return this;
    }

}