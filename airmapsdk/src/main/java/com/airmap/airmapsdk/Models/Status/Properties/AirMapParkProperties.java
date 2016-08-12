package com.airmap.airmapsdk.Models.Status.Properties;

import com.airmap.airmapsdk.Models.AirMapBaseModel;

import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by Vansh Gandhi on 7/15/16.
 * Copyright © 2016 AirMap, Inc. All rights reserved.
 */
@SuppressWarnings("unused")
public class AirMapParkProperties implements Serializable, AirMapBaseModel {
    private int size;
    private String type; //TODO: turn into an enum

    public AirMapParkProperties(JSONObject propertiesJson) {
        constructFromJson(propertiesJson);
    }

    public AirMapParkProperties() {

    }

    @Override
    public AirMapParkProperties constructFromJson(JSONObject json) {
        if (json != null) {
            setSize(json.optInt("size"));
            setType(json.optString("type"));
        }
        return this;
    }

    public int getSize() {
        return size;
    }

    public AirMapParkProperties setSize(int size) {
        this.size = size;
        return this;
    }

    public String getType() {
        return type;
    }

    public AirMapParkProperties setType(String type) {
        this.type = type;
        return this;
    }
}
