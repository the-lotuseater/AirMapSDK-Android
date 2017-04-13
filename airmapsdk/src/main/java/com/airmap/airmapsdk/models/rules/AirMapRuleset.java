package com.airmap.airmapsdk.models.rules;

import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.airmap.airmapsdk.models.AirMapBaseModel;
import com.airmap.airmapsdk.util.Utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by collin@airmap.com on 3/28/17.
 */

public class AirMapRuleset implements Serializable, AirMapBaseModel, Comparable {

    public enum Type {
        PickOne("pickone"), Required("required"), Optional("optional");

        private String text;

        Type(String text) {
            this.text = text;
        }

        public static AirMapRuleset.Type fromString(String text) {
            switch (text.toLowerCase()) {
                case "required":
                    return Required;
                case "optional":
                    return Optional;
                case "pick1":
                case "pickone":
                    return PickOne;
            }

            return Required;
        }

        public String toString() {
            return text;
        }

        public int intValue() {
            if (this == PickOne) {
                return 2;
            } else if (this == Optional) {
                return 1;
            } else {
                return 0;
            }
        }
    }


    private String id;
    private String name;
    private String shortName;
    private int jurisdictionId;
    private AirMapJurisdiction.RegionCategory region;
    private AirMapRuleset.Type type;
    private boolean isDefault;
    private String summary;
    private List<String> layers;

    private List<AirMapRule> rules;

    public AirMapRuleset() {
    }

    public AirMapRuleset(JSONObject resultJson) {
        constructFromJson(resultJson);
    }

    @Override
    public AirMapRuleset constructFromJson(JSONObject json) {
        if (json != null) {
            setId(json.optString("id"));
            setName(json.optString("name"));
            setShortName(Utils.optString(json, "short_name"));
            setJurisdictionId(json.optInt("jurisdiction_id"));
            setSummary(Utils.optString(json, "summary"));
            setDefault(json.optBoolean("default"));
            setType(Type.fromString(json.optString("type")));
            setRegion(AirMapJurisdiction.RegionCategory.fromString(json.optString("region")));


            layers = new ArrayList<>();
            JSONArray layersJSON = json.optJSONArray("layers");
            for (int i = 0; layersJSON != null && i < layersJSON.length(); i++) {
                layers.add(layersJSON.optString(i));
            }

            rules = new ArrayList<>();
            JSONArray rulesJSON = json.optJSONArray("rules");
            for (int j = 0; rulesJSON != null && j < rulesJSON.length(); j++) {
                rules.add(new AirMapRule(rulesJSON.optJSONObject(j)));
            }
        }
        return this;
    }

    public String getId() {
        return id;
    }

    public AirMapRuleset setId(String id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public AirMapRuleset setName(String name) {
        this.name = name;
        return this;
    }

    public int getJurisdictionId() {
        return jurisdictionId;
    }

    public AirMapRuleset setJurisdictionId(int jurisdictionId) {
        this.jurisdictionId = jurisdictionId;
        return this;
    }

    public String getShortName() {
        if (TextUtils.isEmpty(shortName)) {
            return name;
        }
        return shortName;
    }

    public AirMapRuleset setShortName(String shortName) {
        this.shortName = shortName;
        return this;
    }

    public AirMapJurisdiction.RegionCategory getRegion() {
        return region;
    }

    public AirMapRuleset setRegion(AirMapJurisdiction.RegionCategory region) {
        this.region = region;
        return this;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public AirMapRuleset.Type getType() {
        return type;
    }

    public AirMapRuleset setType(AirMapRuleset.Type type) {
        this.type = type;
        return this;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }

    public List<String> getLayers() {
        return layers;
    }

    public List<AirMapRule> getRules() {
        return rules;
    }

    @Override
    public int compareTo(@NonNull Object o) {
        if (!(o instanceof AirMapRuleset)) {
            return -1;
        }

        if (type.intValue() > ((AirMapRuleset) o).getType().intValue()) {
            return 1;
        } else if (type.intValue() < ((AirMapRuleset) o).getType().intValue()) {
            return -1;
        } else {
            return 0;
        }
    }

    @Override
    public boolean equals(Object o) {
        return o != null && o instanceof AirMapRuleset && ((AirMapRuleset) o).getId().equals(this.getId());
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
