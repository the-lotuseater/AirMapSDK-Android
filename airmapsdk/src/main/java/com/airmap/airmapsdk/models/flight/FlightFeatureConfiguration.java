package com.airmap.airmapsdk.models.flight;

import com.airmap.airmapsdk.models.AirMapBaseModel;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by collin@airmap.com on 6/1/17.
 */

public class FlightFeatureConfiguration implements AirMapBaseModel {

    private String flightFeatureId;
    private ValueConfiguration imperialValueConfig;
    private ValueConfiguration metricValueConfig;

    public FlightFeatureConfiguration(String flightFeatureId, JSONObject jsonObject) {
        this.flightFeatureId = flightFeatureId;
        constructFromJson(jsonObject);
    }

    @Override
    public AirMapBaseModel constructFromJson(JSONObject json) {
        if (json != null) {
            setImperialValueConfig(new ValueConfiguration(json.optJSONObject("imperial")));
            setMetricValueConfig(new ValueConfiguration(json.optJSONObject("metric")));
        }
        return null;
    }

    public String getFlightFeatureId() {
        return flightFeatureId;
    }

    public void setFlightFeatureId(String flightFeatureId) {
        this.flightFeatureId = flightFeatureId;
    }

    public ValueConfiguration getImperialValueConfig() {
        return imperialValueConfig;
    }

    public void setImperialValueConfig(ValueConfiguration imperialValueConfig) {
        this.imperialValueConfig = imperialValueConfig;
    }

    public ValueConfiguration getMetricValueConfig() {
        return metricValueConfig;
    }

    public void setMetricValueConfig(ValueConfiguration metricValueConfig) {
        this.metricValueConfig = metricValueConfig;
    }

    public ValueConfiguration getValueConfig(boolean useMetric) {
        return useMetric ? metricValueConfig : imperialValueConfig;
    }

    public class ValueConfiguration {
        private double defaultValue;
        private List<Double> presets;
        private double conversionFactor;
        private String unit;

        public ValueConfiguration(JSONObject jsonObject) {
            if (jsonObject != null) {
                JSONArray values = jsonObject.optJSONArray("preset_values");

                presets = new ArrayList<>();
                for (int i = 0; i < values.length(); i++) {
                    presets.add(values.optDouble(i));
                }

                int defaultIndex = jsonObject.optInt("default_value_index");
                setDefaultValue(presets.get(defaultIndex));

                setConversionFactor(jsonObject.optDouble("conversion_factor", 1));

                setUnit(jsonObject.optString("unit"));
            }
        }

        public double getDefaultValue() {
            return defaultValue;
        }

        public void setDefaultValue(double defaultValue) {
            this.defaultValue = defaultValue;
        }

        public List<Double> getPresets() {
            return presets;
        }

        public void setPresets(List<Double> presets) {
            this.presets = presets;
        }

        public double getConversionFactor() {
            return conversionFactor;
        }

        public void setConversionFactor(double conversionFactor) {
            this.conversionFactor = conversionFactor;
        }

        public String getUnit() {
            return unit;
        }

        public void setUnit(String unit) {
            this.unit = unit;
        }
    }
}
