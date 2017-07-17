package com.airmap.airmapsdk.networking.services;

import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.airmap.airmapsdk.models.Coordinate;
import com.airmap.airmapsdk.models.airspace.AirMapAirspaceAdvisoryStatus;
import com.airmap.airmapsdk.models.rules.AirMapRule;
import com.airmap.airmapsdk.models.rules.AirMapRuleset;
import com.airmap.airmapsdk.models.shapes.AirMapGeometry;
import com.airmap.airmapsdk.models.status.AirMapAdvisory;
import com.airmap.airmapsdk.models.status.AirMapStatusAdvisory;
import com.airmap.airmapsdk.models.welcome.AirMapWelcomeResult;
import com.airmap.airmapsdk.networking.callbacks.AirMapCallback;
import com.airmap.airmapsdk.networking.callbacks.GenericListOkHttpCallback;
import com.airmap.airmapsdk.networking.callbacks.GenericOkHttpCallback;
import com.airmap.airmapsdk.util.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;

/**
 * Created by Vansh Gandhi on 1/12/17.
 * Copyright © 2016 AirMap, Inc. All rights reserved.
 */

public class RulesService extends BaseService {

    public static Call getRulesets(Coordinate coordinate, AirMapCallback<List<AirMapRuleset>> listener) {
        Map<String, String> params = new HashMap<>();
        params.put("latitude", String.valueOf(coordinate.getLatitude()));
        params.put("longitude", String.valueOf(coordinate.getLongitude()));
        return AirMap.getClient().get(rulesetsBaseUrl, params, new GenericListOkHttpCallback(listener, AirMapRuleset.class));
    }

    public static Call getRulesets(JSONObject geometry, AirMapCallback<List<AirMapRuleset>> listener) {
        Map<String, String> params = new HashMap<>();
        params.put("geometry", geometry.toString());
        return AirMap.getClient().post(rulesetsBaseUrl, params, new GenericListOkHttpCallback(listener, AirMapRuleset.class));
    }

    public static Call getRules(String rulesetId, AirMapCallback<AirMapRuleset> listener) {
        String url = String.format(rulesByIdUrl, rulesetId);
        return AirMap.getClient().get(url, new GenericOkHttpCallback(listener, AirMapRuleset.class));
    }

    public static Call getRulesets(List<String> rulesetIds, AirMapCallback<List<AirMapRuleset>> listener) {
        String url = rulesetsUrl;
        Map<String, String> params = new HashMap<>();
        params.put("rulesets", TextUtils.join(",",rulesetIds));
        return AirMap.getClient().get(url, params, new GenericListOkHttpCallback(listener, AirMapRuleset.class));
    }

    public static Call getAdvisories(List<String> rulesets, List<Coordinate> geometry, @Nullable Map<String,Object> flightFeatures, AirMapCallback<AirMapAirspaceAdvisoryStatus> listener) {
        Map<String, String> params = new HashMap<>();
        params.put("rulesets", TextUtils.join(",", rulesets));
        params.put("geometry", "POLYGON(" + Utils.makeGeoString(geometry) + ")");

        if (flightFeatures != null && !flightFeatures.isEmpty()) {
            try {
                JSONObject jsonObject = new JSONObject();
                for (String key : flightFeatures.keySet()) {
                    jsonObject.put(key, flightFeatures.get(key));
                }
                params.put("flight_features", URLEncoder.encode(jsonObject.toString(), "UTF-8"));
            } catch (JSONException | UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        Call call = AirMap.getClient().get(advisoriesUrl, params, new GenericOkHttpCallback(listener, AirMapAirspaceAdvisoryStatus.class));
        return call;
    }

    public static Call getAdvisories(List<String> rulesets, JSONObject geometry, @Nullable Map<String,Object> flightFeatures, AirMapCallback<AirMapAirspaceAdvisoryStatus> listener) {
        Map<String, String> params = new HashMap<>();
        params.put("rulesets", TextUtils.join(",", rulesets));
        params.put("geometry", geometry.toString());

        if (flightFeatures != null && !flightFeatures.isEmpty()) {
            try {
                JSONObject jsonObject = new JSONObject();
                for (String key : flightFeatures.keySet()) {
                    jsonObject.put(key, flightFeatures.get(key));
                }
                params.put("flight_features", URLEncoder.encode(jsonObject.toString(), "UTF-8"));
            } catch (JSONException | UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        Call call = AirMap.getClient().get(advisoriesUrl, params, new GenericOkHttpCallback(listener, AirMapAirspaceAdvisoryStatus.class));
        return call;
    }

    public static Call getWelcomeSummary(Coordinate coordinate, AirMapCallback<List<AirMapWelcomeResult>> listener) {
        Map<String, String> params = new HashMap<>();
        params.put("latitude", String.valueOf(coordinate.getLatitude()));
        params.put("longitude", String.valueOf(coordinate.getLongitude()));
        return AirMap.getClient().get(welcomeBaseUrl, params, new GenericListOkHttpCallback(listener, AirMapWelcomeResult.class));
    }
}
