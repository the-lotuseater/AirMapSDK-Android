package com.airmap.airmapsdk.models.permits;

import com.airmap.airmapsdk.models.AirMapBaseModel;

import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by collinvance on 11/9/16.
 */

public class AirMapPermitIssuer implements Serializable, AirMapBaseModel {

    private String id;
    private String name;

    public AirMapPermitIssuer(JSONObject issuerJson) {
        constructFromJson(issuerJson);
    }

    @Override
    public AirMapBaseModel constructFromJson(JSONObject json) {
        setId(json.optString("id"));
        setName(json.optString("name"));
        return this;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Comparison based on ID
     */
    @Override
    public boolean equals(Object o) {
        return o instanceof AirMapPermitIssuer && getId().equals(((AirMapPermitIssuer) o).getId());
    }
}
