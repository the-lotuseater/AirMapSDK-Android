package com.airmap.airmapsdk.models.status.properties;

import com.airmap.airmapsdk.models.AirMapBaseModel;

import org.json.JSONObject;

import java.io.Serializable;

@SuppressWarnings("unused")
public class AirMapSpecialUseProperties implements Serializable, AirMapBaseModel {
    private boolean currentlyActive;
    private String description;

    /**
     * Initialize an AirMapSpecialUseProperties from JSON
     *
     * @param propertiesJson The JSON representation
     */
    public AirMapSpecialUseProperties(JSONObject propertiesJson) {
        constructFromJson(propertiesJson);
    }

    public AirMapSpecialUseProperties() {

    }

    @Override
    public AirMapSpecialUseProperties constructFromJson(JSONObject json) {
        if (json != null) {
            setCurrentlyActive(json.optBoolean("currentlyActive"));
            setDescription(json.optString("description"));
        }
        return this;
    }

    public boolean isCurrentlyActive() {
        return currentlyActive;
    }

    public AirMapSpecialUseProperties setCurrentlyActive(boolean currentlyActive) {
        this.currentlyActive = currentlyActive;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public AirMapSpecialUseProperties setDescription(String description) {
        this.description = description;
        return this;
    }

    @Override
    public String toString() {
        return getDescription();
    }
}
