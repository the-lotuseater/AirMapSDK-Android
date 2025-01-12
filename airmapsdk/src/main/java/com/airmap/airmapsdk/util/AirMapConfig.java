package com.airmap.airmapsdk.util;

import com.airmap.airmapsdk.networking.services.AirMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

import timber.log.Timber;

public class AirMapConfig {

    private static final String PROD = "";

    public static String getDomain() {
        try {
            return AirMap.getConfig().getJSONObject("airmap").getString("domain");
        } catch (JSONException e) {
            Timber.w(e, "Error getting airmap domain from airmap.config.json. Using fallback");
            return "airmap.com";
        }
    }

    public static String getEnvironment() {
        try {
            String environment = AirMap.getConfig().getJSONObject("airmap").getString("environment");
            if ("prod".equals(environment)) {
                return PROD;
            }
            return environment;
        } catch (JSONException e) {
            Timber.e(e, "No environment key from airmap.config.json");
            return PROD;
        }
    }

    public static String getApiKey() {
        try {
            return AirMap.getConfig().getJSONObject("airmap").getString("api_key");
        } catch (JSONException e) {
            Timber.e(e, "Error getting api key from airmap.config.json");
            throw new RuntimeException("Error getting api key from airmap.config.json");
        }
    }

    public static boolean isStage() {
        try {
            return AirMap.getConfig().getJSONObject("airmap").getString("environment").equals("stage");
        } catch (JSONException e) {
            Timber.e(e, "No environment key from airmap.config.json");
            return false;
        }
    }

    public static String getMapboxApiKey() {
        try {
            return AirMap.getConfig().getJSONObject("mapbox").getString("access_token");
        } catch (JSONException e) {
            Timber.e(e, "Error getting mapbox key from airmap.config.json");
            throw new RuntimeException("Error getting mapbox key from airmap.config.json");
        }
    }

    public static String getClientId() {
        try {
            return AirMap.getConfig().getJSONObject("airmap").getString("client_id");
        } catch (JSONException e) {
            Timber.e(e, "Error getting clientId from airmap.config.json");
            throw new RuntimeException("client_id not found in airmap.config.json");
        }
    }

    public static String getApiOverride(String key, String fallback) {
        try {
            return AirMap.getConfig().getJSONObject("airmap").getJSONObject("api_overrides").getString(key);
        } catch (JSONException e) {
            Timber.w(e, "No overridden end point found in airmap.config.json for: %s", key);
            return fallback;
        }
    }

    public static String getMapStyleUrl() {
        try {
            return AirMap.getConfig().getJSONObject("airmap").getString("map_style");
        } catch (JSONException e) {
            Timber.w(e, "No map style found in airmap.config.json for. Using fallback");
            return null;
        }
    }

    public static String getAboutUrl() {
        try {
            return AirMap.getConfig().getJSONObject("app").getString("about_url");
        } catch (JSONException e) {
            Timber.w(e, "No About URL found in airmap.config.json using fallback");
            return "https://" + getDomain();
        }
    }

    public static String getFAQUrl() {
        try {
            return AirMap.getConfig().getJSONObject("app").getString("faq_url");
        } catch (JSONException e) {
            Timber.w(e, "No FAQ in airmap.config.json using fallback");
            return "https://airmap.typeform.com/to/XDkePS?language=" + Locale.getDefault().getLanguage();
        }
    }

    public static String getPrivacyUrl() {
        try {
            return AirMap.getConfig().getJSONObject("app").getString("privacy_url");
        } catch (JSONException e) {
            Timber.w(e, "No Privacy URL found in airmap.config.json using fallback");
            return "https://" + getDomain() + "/privacy";
        }
    }

    public static String getTermsUrl() {
        try {
            return AirMap.getConfig().getJSONObject("app").getString("terms_url");
        } catch (JSONException e) {
            Timber.w(e, "No Terms URL found in airmap.config.json using fallback");
            return "https://" + getDomain() + "/terms";
        }
    }

    public static String getFeedbackUrl() {
        try {
            return AirMap.getConfig().getJSONObject("app").getString("feedback_url");
        } catch (JSONException e) {
            Timber.w(e, "No Feedback URL found in airmap.config.json using fallback");
            return "https://airmap.typeform.com/to/r6MaMO";
        }
    }

    public static String[] getIntroImages() {
        try {
            JSONArray jsonArray = AirMap.getConfig().getJSONObject("app").getJSONArray("intro_images");
            String[] urls = new String[jsonArray.length()];

            for (int i = 0; i < urls.length; i++) {
                urls[i] = (String) jsonArray.get(i);
            }
            return urls;
        } catch (JSONException e) {
            Timber.w(e, "No intro images found in airmap.config.json using fallback");

            //fallback urls
            return new String[] {
                    "https://cdn.airmap.io/mobile/android/intro/welcome_intro1.png",
                    "https://cdn.airmap.io/mobile/android/intro/welcome_intro2.png",
                    "https://cdn.airmap.io/mobile/android/intro/welcome_intro3.png",
                    "https://cdn.airmap.io/mobile/android/intro/welcome_intro4.png"
            };
        }
    }

    public static String getTwitterHandle() {
        try {
            return AirMap.getConfig().getJSONObject("app").getString("twitter_handle");
        } catch (JSONException e) {
            Timber.e("Unable to get twitter handle");
            return null;
        }
    }

    public static String getDomainForThirdPartyAPI(String key) {
        try {
            return AirMap.getConfig().getJSONObject(key).getString("api");
        } catch (JSONException e) {
            Timber.e("Unable to get domain for third party: " + key);
            return null;
        }
    }

    public static String getFrontendForThirdPartyAPI(String key) {
        try {
            return AirMap.getConfig().getJSONObject(key).getString("frontend");
        } catch (JSONException e) {
            Timber.e("Unable to get frontend for third party: " + key);
            return null;
        }
    }

    public static String getAppIdForThirdPartyAPI(String key) {
        try {
            return AirMap.getConfig().getJSONObject(key).getString("app_id");
        } catch (JSONException e) {
            Timber.e("Unable to get app id for third party: " + key);
            return null;
        }
    }
}
