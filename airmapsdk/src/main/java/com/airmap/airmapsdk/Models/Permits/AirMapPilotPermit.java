package com.airmap.airmapsdk.models.permits;

import com.airmap.airmapsdk.models.AirMapBaseModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.airmap.airmapsdk.Utils.getDateFromIso8601String;

/**
 * Created by Vansh Gandhi on 7/21/16.
 * Copyright © 2016 AirMap, Inc. All rights reserved.
 */
@SuppressWarnings("unused")
public class AirMapPilotPermit implements AirMapBaseModel, Serializable {
    public enum PermitStatus {
        Accepted("accepted"), Rejected("rejected"), Pending("pending"), Detached("detached");

        private String text;

        PermitStatus(String text) {
            this.text = text;
        }

        public static PermitStatus fromString(String text) {
            switch (text) {
                case "accepted":
                    return Accepted;
                case "rejected":
                    return Rejected;
                case "detached":
                    return Detached;
                default:
                    return Pending;
            }
        }

        public String toString() {
            return text;
        }

    }

    private String id;
    private String permitId;
    private AirMapPermitIssuer issuer;
    private PermitStatus status;
    private String pilotId;
    private Date createdAt;
    private Date expiresAt;
    private Date updatedAt;
    private List<AirMapPilotPermitCustomProperty> customProperties;
    private AirMapPilotPermitShortDetails shortDetails;

    public AirMapPilotPermit(JSONObject permitJson) {
        constructFromJson(permitJson);
    }

    public AirMapPilotPermit() {
    }

    @Override
    public AirMapPilotPermit constructFromJson(JSONObject json) {
        setId(json.optString("id"));
        setPermitId(json.optString("permit_id"));
        setIssuer(new AirMapPermitIssuer(json.optJSONObject("issuer")));
        setPilotId(json.optString("pilot_id"));
        setCreatedAt(getDateFromIso8601String(json.optString("created_at")));
        setExpiresAt(getDateFromIso8601String(json.optString("expiration")));
        setUpdatedAt(getDateFromIso8601String(json.optString("updated_at")));
        setStatus(PermitStatus.fromString(json.optString("status")));
        JSONArray properties = json.optJSONArray("custom_properties");
        if (properties != null) {
            customProperties = new ArrayList<>();
            for (int i = 0; i < properties.length(); i++) {
                JSONObject property = properties.optJSONObject(i);
                customProperties.add(new AirMapPilotPermitCustomProperty(property));
            }
            setCustomProperties(customProperties);
        }
        setShortDetails(new AirMapPilotPermitShortDetails(json.optJSONObject("permit")));
        return this;
    }

    public JSONObject getAsParams() {
        Map<String, String> params = new HashMap<>();
        params.put("id", getId());
        JSONObject object = new JSONObject(params);
        try {
            object.put("custom_properties", getCustomPropertiesJson());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return object;
    }

    private JSONArray getCustomPropertiesJson() {
        if (customProperties == null || customProperties.isEmpty()) {
            return null;
        }
        JSONArray propertiesJsonArray = new JSONArray();
        for (AirMapPilotPermitCustomProperty property : customProperties) {
            JSONObject object = new JSONObject(property.getAsParams());
            propertiesJsonArray.put(object);
        }
        return propertiesJsonArray;
    }

    public String getId() {
        return id;
    }

    public AirMapPilotPermit setId(String id) {
        this.id = id;
        return this;
    }

    public String getPermitId() {
        return permitId;
    }

    public AirMapPilotPermit setPermitId(String permitId) {
        this.permitId = permitId;
        return this;
    }

    public PermitStatus getStatus() {
        return status;
    }

    public AirMapPilotPermit setStatus(PermitStatus status) {
        this.status = status;
        return this;
    }

    public String getPilotId() {
        return pilotId;
    }

    public AirMapPilotPermit setPilotId(String pilotId) {
        this.pilotId = pilotId;
        return this;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public AirMapPilotPermit setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public Date getExpiresAt() {
        return expiresAt;
    }

    public AirMapPilotPermit setExpiresAt(Date expiresAt) {
        this.expiresAt = expiresAt;
        return this;
    }

    public List<AirMapPilotPermitCustomProperty> getCustomProperties() {
        return customProperties;
    }

    public AirMapPilotPermit setCustomProperties(List<AirMapPilotPermitCustomProperty> customProperties) {
        this.customProperties = customProperties;
        return this;
    }

    public AirMapPilotPermitShortDetails getShortDetails() {
        return shortDetails;
    }

    public AirMapPilotPermit setShortDetails(AirMapPilotPermitShortDetails shortDetails) {
        this.shortDetails = shortDetails;
        return this;
    }

    public AirMapPermitIssuer getIssuer() {
        return issuer;
    }

    public AirMapPilotPermit setIssuer(AirMapPermitIssuer issuer) {
        this.issuer = issuer;
        return this;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public AirMapPilotPermit setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
        return this;
    }

    /**
     * Comparison based on ID (Not Permit ID)
     */
    @Override
    public boolean equals(Object o) {
        return o instanceof AirMapPilotPermit && getId().equals(((AirMapPilotPermit) o).getId());
    }
}
