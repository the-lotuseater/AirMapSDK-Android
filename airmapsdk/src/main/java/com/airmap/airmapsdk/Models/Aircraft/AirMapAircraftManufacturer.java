package com.airmap.airmapsdk.Models.Aircraft;

import com.airmap.airmapsdk.Models.AirMapBaseModel;

import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by Vansh Gandhi on 6/15/16.
 * Copyright © 2016 AirMap, Inc. All rights reserved.
 */
@SuppressWarnings("unused")
public class AirMapAircraftManufacturer implements Serializable, AirMapBaseModel {
    private String id;
    private String name;
    private String url;


    public AirMapAircraftManufacturer(JSONObject manufacturerJson) {
        constructFromJson(manufacturerJson);
    }

    public AirMapAircraftManufacturer() {
    }

    @Override
    public AirMapAircraftManufacturer constructFromJson(JSONObject json) {
        if (json != null) {
            setId(json.optString("id"));
            setName(json.optString("name"));
            setUrl(json.optString("url"));
        }
        return this;
    }

    public String getId() {
        return id;
    }

    public AirMapAircraftManufacturer setId(String id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public AirMapAircraftManufacturer setName(String name) {
        this.name = name;
        return this;
    }

    public String getUrl() {
        return url;
    }

    public AirMapAircraftManufacturer setUrl(String url) {
        this.url = url;
        return this;
    }

    /**
     * Comparison based on ID
     */
    @Override
    public boolean equals(Object o) {
        return o instanceof AirMapAircraftManufacturer && ((AirMapAircraftManufacturer) o).getId().equals(getId());
    }

    @Override
    public String toString() {
        return getName();
    }
}
