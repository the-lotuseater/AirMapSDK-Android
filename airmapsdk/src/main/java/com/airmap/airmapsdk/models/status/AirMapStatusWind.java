package com.airmap.airmapsdk.models.status;

import com.airmap.airmapsdk.models.AirMapBaseModel;

import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by Vansh Gandhi on 6/15/16.
 * Copyright © 2016 AirMap, Inc. All rights reserved.
 */
@SuppressWarnings("unused")
public class AirMapStatusWind implements Serializable, AirMapBaseModel {
    private int heading;
    private int speed;
    private int gusting;

    public AirMapStatusWind(JSONObject windJson) {
        constructFromJson(windJson);
    }

    public AirMapStatusWind() {

    }

    @Override
    public AirMapStatusWind constructFromJson(JSONObject json) {
        if (json != null) {
            setGusting(json.optInt("gusting"));
            setSpeed(json.optInt("speed"));
            setHeading(json.optInt("heading"));
        }
        return this;
    }

    /**
     * @return Direction wind is coming from
     */
    public int getHeading() {
        return heading;
    }

    public AirMapStatusWind setHeading(int heading) {
        this.heading = heading;
        return this;
    }

    /**
     * @return Wind speed in km/h
     */
    public int getSpeed() {
        return speed;
    }

    public AirMapStatusWind setSpeed(int speed) {
        this.speed = speed;
        return this;
    }

    /**
     * @return Speed of wind gusts in km/h
     */
    public int getGusting() {
        return gusting;
    }

    public AirMapStatusWind setGusting(int gusting) {
        this.gusting = gusting;
        return this;
    }
}
