package com.airmap.airmapsdk.models.welcome;

import com.airmap.airmapsdk.models.AirMapBaseModel;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Vansh Gandhi on 1/12/17.
 * Copyright © 2016 AirMap, Inc. All rights reserved.
 */

public class AirMapWelcome implements Serializable, AirMapBaseModel {

    private List<AirMapWelcomeResult> results;

    public AirMapWelcome() {
    }

    public AirMapWelcome(JSONObject welcomeJson) {
        constructFromJson(welcomeJson);
    }

    @Override
    public AirMapWelcome constructFromJson(JSONObject json) {
        if (json != null) {
            results = new ArrayList<>();
            JSONArray resultsJsonArray = json.optJSONArray("results");
            for (int i = 0; i < resultsJsonArray.length(); i++) {
                results.add(new AirMapWelcomeResult(resultsJsonArray.optJSONObject(i)));
            }
        }
        return this;
    }

    public List<AirMapWelcomeResult> getResults() {
        return results;
    }

    public void setResults(List<AirMapWelcomeResult> results) {
        this.results = results;
    }
}
