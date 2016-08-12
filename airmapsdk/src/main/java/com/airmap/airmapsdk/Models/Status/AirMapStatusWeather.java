package com.airmap.airmapsdk.Models.Status;

import com.airmap.airmapsdk.Models.AirMapBaseModel;

import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by Vansh Gandhi on 6/15/16.
 * Copyright © 2016 AirMap, Inc. All rights reserved.
 */
@SuppressWarnings("unused")
public class AirMapStatusWeather implements Serializable, AirMapBaseModel {
    private AirMapStatusWind wind;
    private String condition;
    private String icon;
    private double humidity;
    private double visibility;
    private double precipitation;
    private double temperature;

    public AirMapStatusWeather(JSONObject weatherJson) {
        constructFromJson(weatherJson);
    }

    public AirMapStatusWeather() {

    }

    @Override
    public AirMapStatusWeather constructFromJson(JSONObject json) {
        if (json != null) {
            setCondition(json.optString("condition"));
            setIcon(json.optString("icon"));
            setHumidity(json.optDouble("humidity"));
            setPrecipitation(json.optDouble("precipitation"));
            setTemperature(json.optDouble("temperature"));
            setVisibility(json.optDouble("visibility"));
            setWind(new AirMapStatusWind(json.optJSONObject("wind")));
        }
        return this;
    }

    /**
     * Determine the validity of the current weather
     *
     * @return whether the weather is valid or not
     */
    public boolean isValid() {
        return wind != null && condition != null && condition.isEmpty();
    }

    /**
     * @return Human readable condition, such as "Partly Cloudy"
     */
    public String getCondition() {
        return condition;
    }

    public AirMapStatusWeather setCondition(String condition) {
        this.condition = condition;
        return this;
    }

    public String getIcon() {
        return icon;
    }

    public AirMapStatusWeather setIcon(String icon) {
        this.icon = icon;
        return this;
    }

    public AirMapStatusWind getWind() {
        return wind;
    }

    public AirMapStatusWeather setWind(AirMapStatusWind wind) {
        this.wind = wind;
        return this;
    }

    /**
     * @return Humidity percentage
     */
    public double getHumidity() {
        return humidity;
    }

    public AirMapStatusWeather setHumidity(double humidity) {
        this.humidity = humidity;
        return this;
    }

    /**
     * @return Visibility in kilometers
     */
    public double getVisibility() {
        return visibility;
    }

    public AirMapStatusWeather setVisibility(double visibility) {
        this.visibility = visibility;
        return this;
    }

    /**
     * @return Percent probability of rain
     */
    public double getPrecipitation() {
        return precipitation;
    }

    public AirMapStatusWeather setPrecipitation(double precipitation) {
        this.precipitation = precipitation;
        return this;
    }

    /**
     * @return Temperature in Celsius
     */
    public double getTemperature() {
        return temperature;
    }

    public AirMapStatusWeather setTemperature(double temperature) {
        this.temperature = temperature;
        return this;
    }
}
