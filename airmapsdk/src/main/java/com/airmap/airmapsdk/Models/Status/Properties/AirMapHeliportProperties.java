package com.airmap.airmapsdk.models.status.properties;

import com.airmap.airmapsdk.models.AirMapBaseModel;

import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by Vansh Gandhi on 8/8/16.
 * Copyright © 2016 AirMap, Inc. All rights reserved.
 */
@SuppressWarnings("unused")
public class AirMapHeliportProperties implements Serializable, AirMapBaseModel {
    private boolean publicUse;
    private String phoneNumber;

    public AirMapHeliportProperties(JSONObject heliportJson) {
        constructFromJson(heliportJson);
    }

    public AirMapHeliportProperties() {
    }

    @Override
    public AirMapHeliportProperties constructFromJson(JSONObject json) {
        if (json != null) {
            String use = json.optString("use");
            setPublicUse(use.equals("public"));

            setPhoneNumber(json.optString("phone"));
        }
        return this;
    }

    public boolean isPublicUse() {
        return publicUse;
    }

    public AirMapHeliportProperties setPublicUse(boolean publicUse) {
        this.publicUse = publicUse;
        return this;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}
