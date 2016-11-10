package com.airmap.airmapsdk.models.airspace;

import com.airmap.airmapsdk.models.AirMapBaseModel;
import com.airmap.airmapsdk.models.shapes.AirMapGeometry;

import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by Vansh Gandhi on 11/7/16.
 * Copyright © 2016 AirMap, Inc. All rights reserved.
 */
public class AirMapAirspaceRule implements Serializable, AirMapBaseModel {

    private String name;
    private AirMapGeometry geometry;

    public AirMapAirspaceRule(JSONObject ruleJson) {
        constructFromJson(ruleJson);
    }

    public AirMapAirspaceRule() {

    }

    @Override
    public AirMapAirspaceRule constructFromJson(JSONObject json) {
        if (json != null) {
            setName(json.optString("name"));
            setGeometry(AirMapGeometry.getGeometryFromGeoJSON(json.optJSONObject("geometry")));
        }
        return this;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public AirMapGeometry getGeometry() {
        return geometry;
    }

    public void setGeometry(AirMapGeometry geometry) {
        this.geometry = geometry;
    }

    //TODO: Parcelable
}
