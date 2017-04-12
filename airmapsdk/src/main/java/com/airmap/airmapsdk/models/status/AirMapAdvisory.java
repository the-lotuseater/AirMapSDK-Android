package com.airmap.airmapsdk.models.status;

import android.util.Log;

import com.airmap.airmapsdk.models.AirMapBaseModel;
import com.airmap.airmapsdk.models.Coordinate;
import com.airmap.airmapsdk.networking.services.MappingService;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.airmap.airmapsdk.util.Utils.getDateFromIso8601String;

/**
 * Created by Vansh Gandhi on 6/15/16.
 * Copyright © 2016 AirMap, Inc. All rights reserved.
 */
public class AirMapAdvisory implements Serializable, AirMapBaseModel {
    private String id;
    private String name;
    private String organizationId;
    private MappingService.AirMapAirspaceType type;
    private String city;
    private String state;
    private String country;
    private Date lastUpdated;
    private AirMapStatus.StatusColor color;
    private int distance;
    private Coordinate coordinate;
    private String geometryString;

//    private AirMapAdvisoryProperties properties;

    /**
     * Initialize an AirMapStatusAdvisory from JSON
     *
     * @param advisoryJson A JSON representation of an AirMapStatusAdvisory
     */
    public AirMapAdvisory(JSONObject advisoryJson) {
        constructFromJson(advisoryJson);
    }

    /**
     * Initialize an AirMapStatusAdvisory with default values
     */
    public AirMapAdvisory() {

    }

    @Override
    public AirMapAdvisory constructFromJson(JSONObject json) {
        if (json != null) {
            setId(json.optString("id"));
            setName(json.optString("name"));
            setOrganizationId(json.optString("organization_id"));
            String typeString = json.optString("type");
            setType(MappingService.AirMapAirspaceType.fromString(typeString));
            setCountry(json.optString("country"));
            setDistance(json.optInt("distance"));
            setCity(json.optString("city"));
            setState(json.optString("state"));
            setColor(AirMapStatus.StatusColor.fromString(json.optString("color")));
            setGeometryString(json.optString("geometry"));
            double lat = json.optDouble("latitude");
            double lng = json.optDouble("longitude");
            if (lat != Double.NaN && lng != Double.NaN) {
                setCoordinate(new Coordinate(lat, lng));
            }
            setLastUpdated(getDateFromIso8601String(json.optString("last_updated")));

            JSONObject properties = json.optJSONObject("properties");
        }
        return this;
    }

    public int getDistance() {
        return distance;
    }

    public AirMapAdvisory setDistance(int distance) {
        this.distance = distance;
        return this;
    }

    public String getId() {
        return id;
    }

    public AirMapAdvisory setId(String id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public AirMapAdvisory setName(String name) {
        this.name = name;
        return this;
    }

    public String getOrganizationId() {
        return organizationId;
    }

    public AirMapAdvisory setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
        return this;
    }

    public MappingService.AirMapAirspaceType getType() {
        return type;
    }

    public AirMapAdvisory setType(MappingService.AirMapAirspaceType type) {
        this.type = type;
        return this;
    }

    public String getCity() {
        return city;
    }

    public AirMapAdvisory setCity(String city) {
        this.city = city;
        return this;
    }

    public String getState() {
        return state;
    }

    public AirMapAdvisory setState(String state) {
        this.state = state;
        return this;
    }

    public String getCountry() {
        return country;
    }

    public AirMapAdvisory setCountry(String country) {
        this.country = country;
        return this;
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public AirMapAdvisory setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
        return this;
    }

    public AirMapStatus.StatusColor getColor() {
        return color;
    }

    public AirMapAdvisory setColor(AirMapStatus.StatusColor color) {
        this.color = color;
        return this;
    }

    public Coordinate getCoordinate() {
        return coordinate;
    }

    public AirMapAdvisory setCoordinate(Coordinate coordinate) {
        this.coordinate = coordinate;
        return this;
    }

    public String getGeometryString() {
        return geometryString;
    }

    public AirMapAdvisory setGeometryString(String geometryString) {
        this.geometryString = geometryString;
        return this;
    }

    /**
     * Comparison based on ID
     */
    @Override
    public boolean equals(Object o) {
        return o instanceof AirMapAdvisory && getId().equals(((AirMapAdvisory) o).getId());
    }

    @Override
    public String toString() {
        return getName();
    }
}
