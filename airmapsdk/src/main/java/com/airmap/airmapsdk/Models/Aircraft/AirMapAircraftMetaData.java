package com.airmap.airmapsdk.Models.Aircraft;

import com.airmap.airmapsdk.Models.AirMapBaseModel;

import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by Vansh on 6/20/16.
 * Copyright © 2016 AirMap, Inc. All rights reserved.
 */
@SuppressWarnings("unused")
public class AirMapAircraftMetaData implements Serializable, AirMapBaseModel {
    private String image;
    private double weight;
    private double flightTime;

    public AirMapAircraftMetaData(JSONObject metaDataJson) {
        constructFromJson(metaDataJson);
    }

    public AirMapAircraftMetaData() {
    }

    @Override
    public AirMapAircraftMetaData constructFromJson(JSONObject json) {
        if (json != null) {
            setImage(json.optString("image", null));
            setWeight(json.optDouble("weight"));
            setFlightTime(json.optDouble("flight_time"));
        }
        return this;
    }

    public String getImage() {
        return image;
    }

    public AirMapAircraftMetaData setImage(String image) {
        this.image = image;
        return this;
    }

    public double getWeight() {
        return weight;
    }

    public AirMapAircraftMetaData setWeight(double weight) {
        this.weight = weight;
        return this;
    }

    public double getFlightTime() {
        return flightTime;
    }

    public AirMapAircraftMetaData setFlightTime(double flightTime) {
        this.flightTime = flightTime;
        return this;
    }
}
