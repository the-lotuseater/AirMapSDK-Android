package com.airmap.airmapsdk.models.status;

import android.text.TextUtils;

import com.airmap.airmapsdk.models.AirMapBaseModel;
import com.airmap.airmapsdk.models.Coordinate;
import com.airmap.airmapsdk.networking.services.MappingService;
import com.airmap.airmapsdk.Utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Vansh Gandhi on 6/15/16.
 * Copyright © 2016 AirMap, Inc. All rights reserved.
 */
@SuppressWarnings("unused")
public class AirMapStatus implements Serializable, AirMapBaseModel {
    public enum StatusColor {
        Red("red"), Yellow("yellow"), Green("green");

        private final String text;

        StatusColor(String text) {
            this.text = text;
        }

        @Override
        public String toString() {
            return text;
        }

        public static StatusColor fromString(String text) {
            switch (text) {
                case "red":
                    return Red;
                case "yellow":
                    return Yellow;
                case "green":
                    return Green;
                default:
                    return null;
            }
        }

    }

    private StatusColor advisoryColor;
    private int maxSafeRadius;
    private List<AirMapStatusAdvisory> advisories;
    private AirMapStatusWeather weather;

    /**
     * Initialize an AirMapStatus from JSON
     *
     * @param statusJson A JSON representation of an AirMapStatus
     */
    public AirMapStatus(JSONObject statusJson) {
        constructFromJson(statusJson);
    }

    /**
     * Initialize an AirMapStatus with default values
     */
    public AirMapStatus() {

    }

    @Override
    public AirMapStatus constructFromJson(JSONObject json) {
        if (json != null) {
            List<AirMapStatusAdvisory> advisories = new ArrayList<>();
            JSONArray advisoriesJson = json.optJSONArray("advisories");
            for (int i = 0; advisoriesJson != null && i < advisoriesJson.length(); i++) {
                advisories.add(new AirMapStatusAdvisory(advisoriesJson.optJSONObject(i)));
            }
            Collections.sort(advisories, new Comparator<AirMapStatusAdvisory>() {
                @Override
                public int compare(AirMapStatusAdvisory a1, AirMapStatusAdvisory a2) {
                    if (a1 != null && a1.getTfrProperties() != null && a1.getTfrProperties().getStartTime() != null && a2 != null && a2.getTfrProperties() != null) {
                        return a1.getTfrProperties().getStartTime().compareTo(a2.getTfrProperties().getStartTime());
                    } else {
                        return 0;
                    }
                }
            });
            setAdvisories(advisories);
            setMaxSafeRadius(json.optInt("max_safe_distance"));
            if (json.has("weather")) {
                setWeather(new AirMapStatusWeather(json.optJSONObject("weather")));
            }
            setAdvisoryColor(StatusColor.fromString(json.optString("advisory_color")));
        }
        return this;
    }

    /**
     * Serializes the necessary information for the Status API. Only serializes info which is
     * shared between point, path, and polygon. Other information must be added separately
     *
     * @param coordinate   A position with latitude and longitude
     * @param types        Airspace types to include in the calculation and response
     * @param ignoredTypes Airspace types to ignore in the calculation and repose
     * @param weather      If set to true, shows current weather conditions in the response
     * @param date         Date for planned flight
     * @return A map of all the required fields
     */
    public static Map<String, String> getAsParams(Coordinate coordinate,
                                                  List<MappingService.AirMapAirspaceType> types,
                                                  List<MappingService.AirMapAirspaceType> ignoredTypes,
                                                  boolean weather, Date date) {
        Map<String, String> params = new HashMap<>();
        params.put("latitude", String.valueOf(coordinate.getLatitude()));
        params.put("longitude", String.valueOf(coordinate.getLongitude()));
        if (types != null && !types.isEmpty()) {
            String typesString = TextUtils.join(",", types); //Will call toString on the AirMapAirspaceType, which will return the appropriate string
            params.put("types", typesString);
        }
        if (ignoredTypes != null && !ignoredTypes.isEmpty()) {
            String ignoredTypesString = TextUtils.join(",", types); //Will call toString on the AirMapAirspaceType, which will return the appropriate string
            params.put("ignored_types", ignoredTypesString);
        }
        params.put("weather", String.valueOf(weather));
        if (date != null) {
            params.put("datetime", Utils.getIso8601StringFromDate(date));
        }
        return params;
    }

    public int getMaxSafeRadius() {
        return maxSafeRadius;
    }

    public AirMapStatus setMaxSafeRadius(int maxSafeRadius) {
        this.maxSafeRadius = maxSafeRadius;
        return this;
    }

    public StatusColor getAdvisoryColor() {
        return advisoryColor;
    }

    public AirMapStatus setAdvisoryColor(StatusColor advisoryColor) {
        this.advisoryColor = advisoryColor;
        return this;
    }

    public List<AirMapStatusAdvisory> getAdvisories() {
        return advisories;
    }

    public AirMapStatus setAdvisories(List<AirMapStatusAdvisory> advisories) {
        this.advisories = advisories;
        return this;
    }

    public AirMapStatusWeather getWeather() {
        return weather;
    }

    public AirMapStatus setWeather(AirMapStatusWeather weather) {
        this.weather = weather;
        return this;
    }
}
