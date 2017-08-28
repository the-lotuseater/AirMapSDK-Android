package com.airmap.airmapsdk.models.aircraft;

import com.airmap.airmapsdk.models.AirMapBaseModel;

import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by Vansh Gandhi on 6/15/16.
 * Copyright © 2016 AirMap, Inc. All rights reserved.
 */
@SuppressWarnings("unused")
public class AirMapAircraftModel implements Serializable, AirMapBaseModel {
    private String modelId;
    private String name;
    private AirMapAircraftManufacturer manufacturer;
    private AirMapAircraftMetaData metaData;

    public AirMapAircraftModel(JSONObject modelJson) {
        constructFromJson(modelJson);
    }

    public AirMapAircraftModel() {

    }

    @Override
    public AirMapAircraftModel constructFromJson(JSONObject json) {
        if (json != null) {
            setModelId(json.optString("id"));
            setName(json.optString("name"));
            setManufacturer(new AirMapAircraftManufacturer(json.optJSONObject("manufacturer")));
            setMetaData(new AirMapAircraftMetaData(json.optJSONObject("metadata")));
        }
        return this;
    }

    public String getModelId() {
        return modelId;
    }

    public AirMapAircraftModel setModelId(String modelId) {
        this.modelId = modelId;
        return this;
    }

    public String getName() {
        return name;
    }

    public AirMapAircraftModel setName(String name) {
        this.name = name;
        return this;
    }

    public AirMapAircraftManufacturer getManufacturer() {
        return manufacturer;
    }

    public AirMapAircraftModel setManufacturer(AirMapAircraftManufacturer manufacturer) {
        this.manufacturer = manufacturer;
        return this;
    }

    public AirMapAircraftMetaData getMetaData() {
        return metaData;
    }

    public AirMapAircraftModel setMetaData(AirMapAircraftMetaData metaData) {
        this.metaData = metaData;
        return this;
    }

    /**
     * Comparison based on ID
     */
    @Override
    public boolean equals(Object o) {
        return o instanceof AirMapAircraftModel && ((AirMapAircraftModel) o).getModelId().equals(getModelId());
    }

    @Override
    public String toString() {
        return manufacturer.getName() + " " + getName();
    }
}
