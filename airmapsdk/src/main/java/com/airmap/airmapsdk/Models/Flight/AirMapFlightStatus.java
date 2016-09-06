package com.airmap.airmapsdk.models.flight;

import com.airmap.airmapsdk.models.AirMapBaseModel;

import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by Vansh Gandhi on 6/15/16.
 * Copyright © 2016 AirMap, Inc. All rights reserved.
 */
@SuppressWarnings("unused")
public class AirMapFlightStatus implements Serializable, AirMapBaseModel {
    public enum AirMapFlightStatusType {
        Accepted("accepted"), Rejected("rejected"), Pending("pending");
        private final String text;

        AirMapFlightStatusType(String text) {
            this.text = text;
        }
        @Override
        public String toString() {
            return text;
        }
        public static AirMapFlightStatusType fromString(String text) {
            switch (text) {
                case "accepted":
                    return Accepted;
                case "rejected":
                    return Rejected;
                default:
                    return Pending;
            }
        }
    }

    private String id;
    private String managerId;
    private AirMapFlightStatusType status;

    /**
     * Initialize an AirMapFlightStatus from JSON
     * @param statusJson A JSON representation of an AirMapFlightStatus
     */
    public AirMapFlightStatus(JSONObject statusJson)  {
        constructFromJson(statusJson);
    }

    /**
     * Initialize an AirMapFlightStatus with default values
     */
    public AirMapFlightStatus() {

    }

    @Override
    public AirMapFlightStatus constructFromJson(JSONObject json) {
        if (json != null) {
            setId(json.optString("id"));
            setManagerId(json.optString("manager_id"));
            String statusType = json.optString("status");
            setStatus(AirMapFlightStatusType.fromString(statusType));
        }
        return this;
    }

    public String getId() {
        return id;
    }

    public AirMapFlightStatus setId(String id) {
        this.id = id;
        return this;
    }

    public String getManagerId() {
        return managerId;
    }

    public AirMapFlightStatus setManagerId(String managerId) {
        this.managerId = managerId;
        return this;
    }

    public AirMapFlightStatusType getStatus() {
        return status;
    }

    public AirMapFlightStatus setStatus(AirMapFlightStatusType status) {
        this.status = status;
        return this;
    }

    /**
     * Comparison based on ID
     */
    @Override
    public boolean equals(Object o) {
        return o instanceof AirMapFlightStatus && getId().equals(((AirMapFlightStatus) o).getId());
    }
}
