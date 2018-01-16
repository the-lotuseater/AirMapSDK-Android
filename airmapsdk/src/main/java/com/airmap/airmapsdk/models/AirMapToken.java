package com.airmap.airmapsdk.models;

import org.json.JSONObject;

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
