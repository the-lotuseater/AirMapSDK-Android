package com.airmap.airmapsdk.Models.Permits;

import com.airmap.airmapsdk.Models.AirMapBaseModel;
import com.airmap.airmapsdk.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Vansh Gandhi on 7/21/16.
 * Copyright © 2016 AirMap, Inc. All rights reserved.
 */
@SuppressWarnings("unused")
public class AirMapAvailablePermit implements Serializable, AirMapBaseModel {
    private String id;
    private String organizationId;
    private String name;
    private String description;
    private String descriptionUrl;
    private boolean singleUse;
    private int validFor; //A permit either has validUntil or validFor, but NOT both
    private Date validUntil;
    private List<AirMapPilotPermitCustomProperty> customProperties;

    public AirMapAvailablePermit(JSONObject permitJson) {
        constructFromJson(permitJson);
    }

    public AirMapAvailablePermit() {
    }

    @Override
    public AirMapAvailablePermit constructFromJson(JSONObject json) {
        if (json != null) {
            setId(json.optString("id"));
            setOrganizationId(json.optString("organization_id"));
            setName(json.optString("name"));
            setDescription(json.optString("description"));
            setSingleUse(json.optBoolean("single_use"));
            //Either validFor or validUntil will be set, but not both
            if (json.has("valid_until")) {
                setValidUntil(Utils.getDateFromIso8601String(json.optString("valid_until")));
                setValidFor(-1);
            } else if (json.has("valid_for")) {
                setValidFor(json.optInt("valid_for", -1));
                setValidUntil(null);
            }
            JSONArray properties = json.optJSONArray("custom_properties");
            if (properties != null) {
                customProperties = new ArrayList<>();
                for (int i = 0; i < properties.length(); i++) {
                    JSONObject property = properties.optJSONObject(i);
                    customProperties.add(new AirMapPilotPermitCustomProperty(property));
                }
                setCustomProperties(customProperties);
            }
        }
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

    public AirMapAvailablePermit setId(String id) {
        this.id = id;
        return this;
    }

    public String getOrganizationId() {
        return organizationId;
    }

    public AirMapAvailablePermit setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
        return this;
    }

    public String getName() {
        return name;
    }

    public AirMapAvailablePermit setName(String name) {
        this.name = name;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public AirMapAvailablePermit setDescription(String description) {
        this.description = description;
        return this;
    }

    public String getDescriptionUrl() {
        return descriptionUrl;
    }

    public AirMapAvailablePermit setDescriptionUrl(String descriptionUrl) {
        this.descriptionUrl = descriptionUrl;
        return this;
    }

    public boolean isSingleUse() {
        return singleUse;
    }

    public AirMapAvailablePermit setSingleUse(boolean singleUse) {
        this.singleUse = singleUse;
        return this;
    }

    public int getValidFor() {
        return validFor;
    }

    public AirMapAvailablePermit setValidFor(int validFor) {
        this.validFor = validFor;
        return this;
    }

    public Date getValidUntil() {
        return validUntil;
    }

    public AirMapAvailablePermit setValidUntil(Date validUntil) {
        this.validUntil = validUntil;
        return this;
    }

    public List<AirMapPilotPermitCustomProperty> getCustomProperties() {
        return customProperties;
    }

    public AirMapAvailablePermit setCustomProperties(List<AirMapPilotPermitCustomProperty> customProperties) {
        this.customProperties = customProperties;
        return this;
    }

    @Override
    public String toString() {
        return getName();
    }

    /**
     * Comparison based on ID
     */
    @Override
    public boolean equals(Object o) {
        return o instanceof AirMapAvailablePermit && getId().equals(((AirMapAvailablePermit) o).getId());
    }
}
