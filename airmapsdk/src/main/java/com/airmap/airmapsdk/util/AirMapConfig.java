package com.airmap.airmapsdk.util;

import com.airmap.airmapsdk.AirMapLog;
import com.airmap.airmapsdk.networking.services.AirMap;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by collin@airmap.com on 8/30/17.
 */

public class AirMapConfig {

    private static final String TAG = "AirMapConfig";


    public static String getDomain() {
        try {
            return AirMap.getConfig().getJSONObject("airmap").getString("domain");
        } catch (JSONException e) {
            AirMapLog.e(TAG, "Error getting airmap domain from airmap.config.json", e);
            throw new RuntimeException(e);
        }
    }

    public static String getApiKey() {
        try {
            return AirMap.getConfig().getJSONObject("airmap").getString("api_key");
        } catch (JSONException e) {
            AirMapLog.e(TAG, "Error getting api key from airmap.config.json", e);
            throw new RuntimeException("Error getting api key from airmap.config.json");
        }
    }

    public static String getApiOverride(String key) {
        try {
            return AirMap.getConfig().getJSONObject("airmap").getJSONObject("api_overrides").getString(key);
        } catch (JSONException e) {
            AirMapLog.e(TAG, "Error getting api override for " + key + "  from airmap.config.json", e);
            throw new RuntimeException("Error getting api override for " + key +  " from airmap.config.json");
        }
    }

    public static boolean isStage() {
        try {
            return AirMap.getConfig().getJSONObject("airmap").optString("environment", "prod").equals("stage");
        } catch (JSONException e) {
            AirMapLog.e(TAG, "Error getting environment key from airmap.config.json", e);
            return false;
        }
    }

    public static String getMapboxApiKey() {
        try {
            return AirMap.getConfig().getJSONObject("mapbox").getString("access_token");
        } catch (JSONException e) {
            e.printStackTrace();
            throw new RuntimeException("Error getting mapbox key from airmap.config.json");
        }
    }

    public static String getAuth0Host() {
        try {
            return AirMap.getConfig().getJSONObject("auth0").getString("host");
        } catch (JSONException e) {
            AirMapLog.e(TAG, "Error getting auth0 host from airmap.config.json", e);
            throw new RuntimeException("Error getting auth0 host from airmap.config.json");
        }
    }

    public static String getAuth0ClientId() {
        try {
            JSONObject auth0 = AirMap.getConfig().getJSONObject("auth0");
            return auth0.getString("client_id");
        } catch (JSONException e) {
            throw new RuntimeException("client_id and/or callback_url not found in airmap.config.json");
        }
    }

    public static String getMqttDomain() {
        try {
            return AirMap.getConfig().getJSONObject("airmap").getString("mqtt_domain");
        } catch (JSONException e) {
            AirMapLog.e(TAG, "No mqtt domain found in airmap.config.json, defaulting to airmap.io", e);

            return "airmap.io";
        }
    }
}
