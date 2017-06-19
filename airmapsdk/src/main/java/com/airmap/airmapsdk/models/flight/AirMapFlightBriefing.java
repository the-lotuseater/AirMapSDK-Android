package com.airmap.airmapsdk.models.flight;

import com.airmap.airmapsdk.models.AirMapBaseModel;
import com.airmap.airmapsdk.models.airspace.AirMapAirspace;
import com.airmap.airmapsdk.models.airspace.AirMapAirspaceAdvisoryStatus;
import com.airmap.airmapsdk.models.rules.AirMapRuleset;
import com.airmap.airmapsdk.models.status.AirMapAdvisory;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.airmap.airmapsdk.util.Utils.getDateFromIso8601String;

/**
 * Created by collin@airmap.com on 5/22/17.
 */

public class AirMapFlightBriefing implements Serializable, AirMapBaseModel {

    private String color;
    private Date createdAt;
    private List<AirMapRuleset> rulesets;
    private AirMapAirspaceAdvisoryStatus airspace;

    public AirMapFlightBriefing() {

    }

    public AirMapFlightBriefing(JSONObject jsonObject) {
        constructFromJson(jsonObject);
    }

    @Override
    public AirMapBaseModel constructFromJson(JSONObject json) {
        if (json != null) {
            color = json.optString("color");
            createdAt = getDateFromIso8601String(json.optString("created_at"));

            rulesets = new ArrayList<>();
            if (json.has("rulesets")) {
                JSONArray rulesetsArray = json.optJSONArray("rulesets");
                for (int i = 0; i < rulesetsArray.length(); i++) {
                    rulesets.add(new AirMapRuleset(rulesetsArray.optJSONObject(i)));
                }
            }

            airspace = new AirMapAirspaceAdvisoryStatus(json.optJSONObject("airspace"));
        }
        return this;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public List<AirMapRuleset> getRulesets() {
        return rulesets;
    }

    public void setRulesets(List<AirMapRuleset> rulesets) {
        this.rulesets = rulesets;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public AirMapAirspaceAdvisoryStatus getAirspace() {
        return airspace;
    }

    public void setAirspace(AirMapAirspaceAdvisoryStatus airspace) {
        this.airspace = airspace;
    }
}
