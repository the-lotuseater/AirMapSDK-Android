package com.airmap.airmapsdk.Models.Comm;

import com.airmap.airmapsdk.Models.AirMapBaseModel;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by Vansh Gandhi on 6/15/16.
 * Copyright © 2016 AirMap, Inc. All rights reserved.
 */
@SuppressWarnings("unused")
public class AirMapComm implements Serializable, AirMapBaseModel {
    private int[] key;
    private String type;
    private Date expiresAt;

    public AirMapComm(JSONObject commJson) {
        constructFromJson(commJson);
    }

    public AirMapComm() {
    }

    @Override
    public AirMapComm constructFromJson(JSONObject json) {
        if (json != null) {
            JSONObject keyJson = json.optJSONObject("key");
            if (keyJson != null) {
                JSONArray keyArray = keyJson.optJSONArray("data");
                int[] key = new int[keyArray.length()];
                for (int i = 0; i < key.length; i++) {
                    key[i] = keyArray.optInt(i);
                }
                setKey(key);
                setType(keyJson.optString("type"));
                setExpiresAt(new Date(System.currentTimeMillis() + 5 * 60 * 1000)); //5 minutes in millis\
            }
        }
        return this;
    }

    public boolean isExpired() {
        return new Date().after(getExpiresAt()); //If the current time is after the expiration time
    }

    public int[] getKey() {
        return key;
    }

    public AirMapComm setKey(int[] key) {
        this.key = key;
        return this;
    }

    public String getType() {
        return type;
    }

    public AirMapComm setType(String type) {
        this.type = type;
        return this;
    }

    public Date getExpiresAt() {
        return expiresAt;
    }

    public AirMapComm setExpiresAt(Date expiresAt) {
        this.expiresAt = expiresAt;
        return this;
    }
}