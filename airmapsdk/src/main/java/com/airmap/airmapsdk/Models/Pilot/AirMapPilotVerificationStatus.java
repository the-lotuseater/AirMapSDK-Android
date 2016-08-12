package com.airmap.airmapsdk.Models.Pilot;

import com.airmap.airmapsdk.Models.AirMapBaseModel;

import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by Vansh Gandhi on 7/26/16.
 * Copyright © 2016 AirMap, Inc. All rights reserved.
 */
@SuppressWarnings("unused")
public class AirMapPilotVerificationStatus implements Serializable, AirMapBaseModel{

    private boolean phone;
    private boolean email;

    public AirMapPilotVerificationStatus(JSONObject verificationJson) {
        constructFromJson(verificationJson);
    }

    public AirMapPilotVerificationStatus() {
    }

    @Override
    public AirMapPilotVerificationStatus constructFromJson(JSONObject json) {
        if (json != null) {
            setPhone(json.optBoolean("phone"));
            setEmail(json.optBoolean("email"));
        }
        return this;
    }

    public boolean isPhone() {
        return phone;
    }

    public AirMapPilotVerificationStatus setPhone(boolean phone) {
        this.phone = phone;
        return this;
    }

    public boolean isEmail() {
        return email;
    }

    public AirMapPilotVerificationStatus setEmail(boolean email) {
        this.email = email;
        return this;
    }
}
