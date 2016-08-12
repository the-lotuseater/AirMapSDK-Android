package com.airmap.airmapsdk.Models.Permits;

import com.airmap.airmapsdk.Models.AirMapBaseModel;

import org.json.JSONObject;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Vansh Gandhi on 7/21/16.
 * Copyright © 2016 AirMap, Inc. All rights reserved.
 */
@SuppressWarnings("unused")
public class AirMapPilotPermitCustomProperty implements Serializable, AirMapBaseModel {
    public enum CustomPropertyType {
        Text("text"); //TODO: Add more answer types as the backend adds them

        private final String text;
        CustomPropertyType(String text) {
            this.text = text;
        }

        public static CustomPropertyType fromString(String text) {
            switch (text) {
                case "text":
                    return Text;
            }
            return null;
        }

        @Override
        public String toString() {
            return text;
        }

    }

    private String id;
    private String label;
    private CustomPropertyType type;
    private boolean required;
    private String value;

    public AirMapPilotPermitCustomProperty(JSONObject propertiesJson) {
        constructFromJson(propertiesJson);
    }

    public AirMapPilotPermitCustomProperty() {
    }

    @Override
    public AirMapPilotPermitCustomProperty constructFromJson(JSONObject json) {
        if (json != null) {
            setId(json.optString("id"));
            setType(CustomPropertyType.fromString(json.optString("type")));
            setRequired(json.optBoolean("required"));
            setLabel(json.optString("label"));
            setValue(json.optString("value", null)); //We don't want to represent the value as an empty string, so set to null instead
        }
        return this;
    }

    public Map<String, String> getAsParams() {
        Map<String, String> map = new HashMap<>();
        map.put("id", getId());
        map.put("value", getValue());
        return map;
    }

    public String getId() {
        return id;
    }

    public AirMapPilotPermitCustomProperty setId(String id) {
        this.id = id;
        return this;
    }

    public String getLabel() {
        return label;
    }

    public AirMapPilotPermitCustomProperty setLabel(String label) {
        this.label = label;
        return this;
    }

    public CustomPropertyType getType() {
        return type;
    }

    public AirMapPilotPermitCustomProperty setType(CustomPropertyType type) {
        this.type = type;
        return this;
    }

    public boolean isRequired() {
        return required;
    }

    public AirMapPilotPermitCustomProperty setRequired(boolean required) {
        this.required = required;
        return this;
    }

    public String getValue() {
        return value;
    }

    public AirMapPilotPermitCustomProperty setValue(String value) {
        this.value = value;
        return this;
    }

    /**
     * Comparison based on ID
     */
    @Override
    public boolean equals(Object o) {
        return o instanceof AirMapPilotPermitCustomProperty && getId().equals(((AirMapPilotPermitCustomProperty) o).getId());
    }

    @Override
    public String toString() {
        return getLabel();
    }
}
