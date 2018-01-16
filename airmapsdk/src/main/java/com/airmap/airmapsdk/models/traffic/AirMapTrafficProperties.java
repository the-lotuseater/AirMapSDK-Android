package com.airmap.airmapsdk.models.traffic;

import com.airmap.airmapsdk.models.AirMapBaseModel;

import org.json.JSONObject;

import java.io.Serializable;

@SuppressWarnings("unused")
public class AirMapTrafficProperties implements Serializable, AirMapBaseModel {
    private String aircraftId;
    private String aircraftType;

    /**
     * Initialize an AirMapTrafficProperties from JSON
     * @param propertiesJson A JSON representation of an AirMapTrafficProperties
     */
    public AirMapTrafficProperties(JSONObject propertiesJson) {
        constructFromJson(propertiesJson);
    }

    /**
     * Initialize an AirMapTrafficProperties with default values
     */
    public AirMapTrafficProperties() {

    }

    @Override
    public AirMapTrafficProperties constructFromJson(JSONObject json) {
        if (json != null) {
            setAircraftId(json.optString("aircraft_id"));
            setAircraftType(json.optString("aircraft_type"));
        }
        return this;
    }

    public String getAircraftId() {
        return aircraftId;
    }

    public AirMapTrafficProperties setAircraftId(String aircraftId) {
        this.aircraftId = aircraftId;
        return this;
    }

    public String getAircraftType() {
        return aircraftType;
    }

    public AirMapTrafficProperties setAircraftType(String aircraftType) {
        this.aircraftType = aircraftType;
        return this;
    }

    /**
     * Comparison based on Aircraft ID
     */
    @Override
    public boolean equals(Object o) {
        return o instanceof AirMapTrafficProperties && getAircraftId().equals(((AirMapTrafficProperties) o).getAircraftId());
    }
}
