package com.airmap.airmapsdk.models.welcome;

import com.airmap.airmapsdk.models.AirMapBaseModel;
import com.airmap.airmapsdk.util.Utils;

import org.json.JSONObject;

import java.io.Serializable;
import java.util.Date;

import static com.airmap.airmapsdk.util.Utils.getDateFromIso8601String;

/**
 * Created by Vansh Gandhi on 1/12/17.
 * Copyright © 2016 AirMap, Inc. All rights reserved.
 */

public class AirMapWelcomeResult implements Serializable, AirMapBaseModel {

    private String id;
    private String jurisdictionName;
    private String jurisdictionType;
    private String text;
    private String summary;
    private String url;
    private Date lastUpdated;

    public AirMapWelcomeResult() {
    }

    public AirMapWelcomeResult(JSONObject resultJson) {
        constructFromJson(resultJson);
    }

    @Override
    public AirMapWelcomeResult constructFromJson(JSONObject json) {
        if (json != null) {
            setId(json.optString("id"));
            setJurisdictionName(json.optString("jurisdiction_name"));
            setJurisdictionType(json.optString("jurisdiction_type"));
            setText(Utils.optString(json, "text"));
            setSummary(Utils.optString(json, "summary"));
            setUrl(Utils.optString(json, "url"));
            setLastUpdated(getDateFromIso8601String(json.optString("last_updated")));
        }
        return this;
    }

    public String getId() {
        return id;
    }

    public AirMapWelcomeResult setId(String id) {
        this.id = id;
        return this;
    }

    public String getJurisdictionName() {
        return jurisdictionName;
    }

    public AirMapWelcomeResult setJurisdictionName(String jurisdictionName) {
        this.jurisdictionName = jurisdictionName;
        return this;
    }

    public String getJurisdictionType() {
        return jurisdictionType;
    }

    public AirMapWelcomeResult setJurisdictionType(String jurisdictionType) {
        this.jurisdictionType = jurisdictionType;
        return this;
    }

    public String getText() {
        return text;
    }

    public AirMapWelcomeResult setText(String text) {
        this.text = text;
        return this;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getUrl() {
        return url;
    }

    public AirMapWelcomeResult setUrl(String url) {
        this.url = url;
        return this;
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public AirMapWelcomeResult setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
        return this;
    }
}
