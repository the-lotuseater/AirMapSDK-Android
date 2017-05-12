package com.airmap.airmapsdk.models;

import org.json.JSONObject;

/**
 * Created by Vansh Gandhi on 4/6/17.
 * Copyright © 2016 AirMap, Inc. All rights reserved.
 */

public class AirMapToken implements AirMapBaseModel {

    private String authToken;

    public AirMapToken(JSONObject tokenJson) {
        constructFromJson(tokenJson);
    }

    public AirMapToken() {
    }

    @Override
    public AirMapToken constructFromJson(JSONObject json) {
        if (json != null) {
            setAuthToken(json.optString("id_token"));
        }
        return this;
    }

    public String getAuthToken() {
        return authToken;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }
}
