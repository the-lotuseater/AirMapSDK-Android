package com.airmap.airmapsdk.models.permits;

import com.airmap.airmapsdk.models.AirMapBaseModel;

import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by Vansh Gandhi on 7/28/16.
 * Copyright © 2016 AirMap, Inc. All rights reserved.
 */
@SuppressWarnings("unused")
public class AirMapPilotPermitShortDetails implements Serializable, AirMapBaseModel {

    private String permitId;
    private String name;
    private String description;
    private boolean singleUse;

    public AirMapPilotPermitShortDetails(JSONObject detailsJson) {
        constructFromJson(detailsJson);
    }

    public AirMapPilotPermitShortDetails() {
    }

    @Override
    public AirMapPilotPermitShortDetails constructFromJson(JSONObject json) {
        if (json != null) {
            setPermitId(json.optString("id"));
            setName(json.optString("name"));
            setDescription(json.optString("description"));
            setSingleUse(json.optBoolean("single_use"));
        }
        return this;
    }

    public String getPermitId() {
        return permitId;
    }

    public AirMapPilotPermitShortDetails setPermitId(String permitId) {
        this.permitId = permitId;
        return this;
    }


    public String getDescription() {
        return description;
    }

    public AirMapPilotPermitShortDetails setDescription(String description) {
        this.description = description;
        return this;
    }

    public String getName() {
        return name;
    }

    public AirMapPilotPermitShortDetails setName(String name) {
        this.name = name;
        return this;
    }

    public boolean isSingleUse() {
        return singleUse;
    }

    public AirMapPilotPermitShortDetails setSingleUse(boolean singleUse) {
        this.singleUse = singleUse;
        return this;
    }

    @Override
    public String toString() {
        return getName();
    }
}
