package com.airmap.airmapsdk.models.status.properties;

import com.airmap.airmapsdk.models.AirMapBaseModel;

import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by Collin Vance on 1/4/18.
 * Copyright © 2016 AirMap, Inc. All rights reserved.
 */
@SuppressWarnings("unused")
public class AirMapOptionalProperties implements AirMapBaseModel, Serializable {

    private String url;
    private String description;

    public AirMapOptionalProperties(JSONObject propertiesJson) {
        constructFromJson(propertiesJson);
    }

    public AirMapOptionalProperties() {
    }

    @Override
    public AirMapOptionalProperties constructFromJson(JSONObject json) {
        if (json != null) {
            setUrl(json.optString("url"));
            setDescription(json.optString("description"));
        }
        return this;
    }

    public String getUrl() {
        return url;
    }

    public AirMapOptionalProperties setUrl(String url) {
        this.url = url;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public AirMapOptionalProperties setDescription(String description) {
        this.description = description;
        return this;
    }
}
