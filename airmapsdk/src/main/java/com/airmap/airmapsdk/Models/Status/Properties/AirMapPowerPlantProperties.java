package com.airmap.airmapsdk.Models.Status.Properties;

import com.airmap.airmapsdk.Models.AirMapBaseModel;

import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by Vansh Gandhi on 7/15/16.
 * Copyright © 2016 AirMap, Inc. All rights reserved.
 */
@SuppressWarnings("unused")
public class AirMapPowerPlantProperties implements Serializable, AirMapBaseModel {
    private String tech;
    private int plantCode;

    /**
     * Initialize an AirMapPowerPlantProperties from JSON
     *
     * @param propertiesJson The JSON representation
     */
    public AirMapPowerPlantProperties(JSONObject propertiesJson) {
        constructFromJson(propertiesJson);
    }

    public AirMapPowerPlantProperties() {

    }

    @Override
    public AirMapPowerPlantProperties constructFromJson(JSONObject json) {
        if (json != null) {
            setTech(json.optString("tech"));
            setPlantCode(json.optInt("plant_code"));
        }
        return this;
    }

    public String getTech() {
        return tech;
    }

    public AirMapPowerPlantProperties setTech(String tech) {
        this.tech = tech;
        return this;
    }

    public int getPlantCode() {
        return plantCode;
    }

    public AirMapPowerPlantProperties setPlantCode(int plantCode) {
        this.plantCode = plantCode;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof AirMapPowerPlantProperties && getPlantCode() == ((AirMapPowerPlantProperties) o).getPlantCode();
    }
}
