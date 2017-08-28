package com.airmap.airmapsdk.models.status;

import com.airmap.airmapsdk.models.AirMapBaseModel;

import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by Vansh Gandhi on 8/1/16.
 * Copyright © 2016 AirMap, Inc. All rights reserved.
 */
@SuppressWarnings("unused")
public class AirMapStatusAdvisoryRule implements AirMapBaseModel, Serializable {

    private String id;
    private String name;
    private String description;
    private AirMapStatusAdvisoryRuleJurisdiction jurisdiction;
    private String restrictionType; //TODO: turn this into an enum
    private String geometryString;

    public AirMapStatusAdvisoryRule(JSONObject ruleJson) {
        constructFromJson(ruleJson);
    }

    public AirMapStatusAdvisoryRule() {

    }

    @Override
    public AirMapStatusAdvisoryRule constructFromJson(JSONObject json) {
        if (json != null) {
            setId(json.optString("id"));
            setName(json.optString("name"));
            setDescription(json.optString("description"));
            setRestrictionType(json.optString("restriction_type"));
            setGeometryString(json.optString("geometry"));
            setJurisdiction(new AirMapStatusAdvisoryRuleJurisdiction(json.optJSONObject("jurisdiction")));
        }
        return this;
    }

    public String getId() {
        return id;
    }

    public AirMapStatusAdvisoryRule setId(String id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public AirMapStatusAdvisoryRule setName(String name) {
        this.name = name;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public AirMapStatusAdvisoryRule setDescription(String description) {
        this.description = description;
        return this;
    }

    public AirMapStatusAdvisoryRuleJurisdiction getJurisdiction() {
        return jurisdiction;
    }

    public AirMapStatusAdvisoryRule setJurisdiction(AirMapStatusAdvisoryRuleJurisdiction jurisdiction) {
        this.jurisdiction = jurisdiction;
        return this;
    }

    public String getRestrictionType() {
        return restrictionType;
    }

    public AirMapStatusAdvisoryRule setRestrictionType(String restrictionType) {
        this.restrictionType = restrictionType;
        return this;
    }

    public String getGeometryString() {
        return geometryString;
    }

    /**
     * @param geometryString Must be WKT/GeoJson formatted string
     */
    public AirMapStatusAdvisoryRule setGeometryString(String geometryString) {
        this.geometryString = geometryString;
        return this;
    }

    /**
     * Comparison based on ID
     */
    @Override
    public boolean equals(Object o) {
        return o instanceof AirMapStatusAdvisoryRule && getId().equals(((AirMapStatusAdvisoryRule) o).getId());
    }

    @Override
    public String toString() {
        return getName();
    }
}
