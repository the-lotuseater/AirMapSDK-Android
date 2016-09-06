package com.airmap.airmapsdk.models.pilot;

import com.airmap.airmapsdk.models.AirMapBaseModel;

import org.json.JSONObject;

import java.io.Serializable;
import java.util.Date;

import static com.airmap.airmapsdk.Utils.getDateFromIso8601String;

/**
 * Created by Vansh Gandhi on 7/26/16.
 * Copyright © 2016 AirMap, Inc. All rights reserved.
 */
@SuppressWarnings("unused")
public class AirMapPilotStatsFlight implements Serializable, AirMapBaseModel {

    private int total;
    private Date lastFlightTime; //The last date/time the pilot had a flight

    public AirMapPilotStatsFlight(JSONObject statsJson) {
        constructFromJson(statsJson);
    }

    public AirMapPilotStatsFlight() {

    }

    @Override
    public AirMapPilotStatsFlight constructFromJson(JSONObject json) {
        if (json != null) {
            setTotal(json.optInt("total"));
            setLastFlightTime(getDateFromIso8601String(json.optString("last_flight_time")));
        }
        return this;
    }

    public int getTotal() {
        return total;
    }

    public AirMapPilotStatsFlight setTotal(int total) {
        this.total = total;
        return this;
    }

    public Date getLastFlightTime() {
        return lastFlightTime;
    }

    public AirMapPilotStatsFlight setLastFlightTime(Date lastFlightTime) {
        this.lastFlightTime = lastFlightTime;
        return this;
    }
}
