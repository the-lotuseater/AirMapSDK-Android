package com.airmap.airmapsdk.Models.Pilot;

import com.airmap.airmapsdk.Models.AirMapBaseModel;

import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by Vansh Gandhi on 7/26/16.
 * Copyright © 2016 AirMap, Inc. All rights reserved.
 */
@SuppressWarnings("unused")
public class AirMapPilotStatsAircraft implements Serializable, AirMapBaseModel {

    private int total;

    public AirMapPilotStatsAircraft(JSONObject statsJson) {
        constructFromJson(statsJson);
    }

    public AirMapPilotStatsAircraft() {

    }

    @Override
    public AirMapPilotStatsAircraft constructFromJson(JSONObject json) {
        if (json != null) {
            setTotal(json.optInt("total"));
        }
        return this;
    }

    public int getTotal() {
        return total;
    }

    public AirMapPilotStatsAircraft setTotal(int total) {
        this.total = total;
        return this;
    }
}
