package com.airmap.airmapsdk.Models.Status;

import com.airmap.airmapsdk.Models.AirMapBaseModel;

import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by Vansh Gandhi on 8/1/16.
 * Copyright © 2016 AirMap, Inc. All rights reserved.
 */
@SuppressWarnings("unused")
public class AirMapStatusAdvisoryRuleJurisdiction implements AirMapBaseModel, Serializable {

    private String name;
    private String type; //TODO: turn this into an enum

    public AirMapStatusAdvisoryRuleJurisdiction(JSONObject jurisdictionJson) {
        constructFromJson(jurisdictionJson);
    }

    public AirMapStatusAdvisoryRuleJurisdiction() {

    }

    @Override
    public AirMapStatusAdvisoryRuleJurisdiction constructFromJson(JSONObject json) {
        if (json != null) {
            setName(json.optString("name"));
            setType(json.optString("type"));
        }
        return this;
    }

    public String getName() {
        return name;
    }

    public AirMapStatusAdvisoryRuleJurisdiction setName(String name) {
        this.name = name;
        return this;
    }

    public String getType() {
        return type;
    }

    public AirMapStatusAdvisoryRuleJurisdiction setType(String type) {
        this.type = type;
        return this;
    }

    @Override
    public String toString() {
        return getName();
    }
}
