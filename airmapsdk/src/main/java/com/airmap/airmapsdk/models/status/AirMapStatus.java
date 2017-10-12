package com.airmap.airmapsdk.models.status;

import android.support.annotation.ColorRes;
import android.text.TextUtils;

import com.airmap.airmapsdk.R;
import com.airmap.airmapsdk.models.AirMapBaseModel;
import com.airmap.airmapsdk.models.Coordinate;
import com.airmap.airmapsdk.models.permits.AirMapAvailablePermit;
import com.airmap.airmapsdk.models.permits.AirMapPermitIssuer;
import com.airmap.airmapsdk.networking.services.MappingService;
import com.airmap.airmapsdk.util.Utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Vansh Gandhi on 6/15/16.
 * Copyright © 2016 AirMap, Inc. All rights reserved.
 */
@Deprecated
public class AirMapStatus implements Serializable, AirMapBaseModel {
    public enum StatusColor {
        Red("red"), Yellow("yellow"), Green("green"), Orange("orange");

        private final String text;

        StatusColor(String text) {
            this.text = text;
        }

        @Override
        public String toString() {
            return text;
        }

        public static StatusColor fromString(String text) {
            switch (text) {
                case "red":
                    return Red;
                case "yellow":
                    return Yellow;
                case "green":
                    return Green;
                case "orange":
                    return Orange;
                default:
                    return null;
            }
        }

        public int intValue() {
            switch (this) {
                case Red:
                    return 4;
                case Orange:
                    return 3;
                case Yellow:
                    return 2;
                default:
                case Green:
                    return 1;
            }
        }

        public @ColorRes int getColorRes() {
            switch (this) {
                case Red:
                    return R.color.status_red;
                case Orange:
                    return R.color.status_orange;
                case Yellow:
                    return R.color.status_yellow;
                default:
                case Green:
                    return R.color.status_green;
            }
        }
    }

    private StatusColor advisoryColor;
    private int maxSafeRadius;
    private List<AirMapStatusAdvisory> advisories;
    private List<AirMapAvailablePermit> applicablePermits;
    private List<AirMapPermitIssuer> organizations;

    /**
     * Initialize an AirMapStatus from JSON
     *
     * @param statusJson A JSON representation of an AirMapStatus
     */
    public AirMapStatus(JSONObject statusJson) {
        constructFromJson(statusJson);
    }

    /**
     * Initialize an AirMapStatus with default values
     */
    public AirMapStatus() {

    }

    @Override
    public AirMapStatus constructFromJson(JSONObject json) {
        if (json != null) {
            List<AirMapStatusAdvisory> advisories = new ArrayList<>();
            JSONArray advisoriesJson = json.optJSONArray("advisories");
            for (int i = 0; advisoriesJson != null && i < advisoriesJson.length(); i++) {
                advisories.add(new AirMapStatusAdvisory(advisoriesJson.optJSONObject(i)));
            }
            setAdvisories(advisories);
            setMaxSafeRadius(json.optInt("max_safe_distance"));
            setAdvisoryColor(StatusColor.fromString(json.optString("advisory_color")));

            // applicable permits
            List<AirMapAvailablePermit> applicablePermits = new ArrayList<>();
            JSONArray applicablePermitsJson = json.optJSONArray("applicable_permits");
            for (int k = 0; applicablePermitsJson != null && k < applicablePermitsJson.length(); k++) {
                applicablePermits.add(new AirMapAvailablePermit(applicablePermitsJson.optJSONObject(k)));
            }
            setApplicablePermits(applicablePermits);

            // organizations (issuers)
            List<AirMapPermitIssuer> organizations = new ArrayList<>();
            JSONArray organizationsJSON = json.optJSONArray("organizations");
            for (int j = 0; organizationsJSON != null && j < organizationsJSON.length(); j++) {
                organizations.add(new AirMapPermitIssuer(organizationsJSON.optJSONObject(j)));
            }
            setOrganizations(organizations);
        }
        return this;
    }

    /**
     * Serializes the necessary information for the Status API. Only serializes info which is
     * shared between point, path, and polygon. Other information must be added separately
     *
     * @param coordinate   A position with latitude and longitude
     * @param types        Airspace types to include in the calculation and response
     * @param ignoredTypes Airspace types to ignore in the calculation and repose
     * @param weather      If set to true, shows current weather conditions in the response
     * @param date         Date for planned flight
     * @return A map of all the required fields
     */
    public static Map<String, String> getAsParams(Coordinate coordinate,
                                                  List<MappingService.AirMapAirspaceType> types,
                                                  List<MappingService.AirMapAirspaceType> ignoredTypes,
                                                  boolean weather, Date date) {
        Map<String, String> params = new HashMap<>();
        params.put("latitude", String.valueOf(coordinate.getLatitude()));
        params.put("longitude", String.valueOf(coordinate.getLongitude()));
        if (types != null && !types.isEmpty()) {
            String typesString = TextUtils.join(",", types); //Will call toString on the AirMapAirspaceType, which will return the appropriate string
            params.put("types", typesString);
        }
        if (ignoredTypes != null && !ignoredTypes.isEmpty()) {
            String ignoredTypesString = TextUtils.join(",", types); //Will call toString on the AirMapAirspaceType, which will return the appropriate string
            params.put("ignored_types", ignoredTypesString);
        }
        params.put("weather", String.valueOf(weather));
        if (date != null) {
            params.put("datetime", Utils.getIso8601StringFromDate(date));
        }
        return params;
    }

    public int getMaxSafeRadius() {
        return maxSafeRadius;
    }

    public AirMapStatus setMaxSafeRadius(int maxSafeRadius) {
        this.maxSafeRadius = maxSafeRadius;
        return this;
    }

    public StatusColor getAdvisoryColor() {
        return advisoryColor;
    }

    public AirMapStatus setAdvisoryColor(StatusColor advisoryColor) {
        this.advisoryColor = advisoryColor;
        return this;
    }

    public List<AirMapStatusAdvisory> getAdvisories() {
        return advisories;
    }

    public AirMapStatus setAdvisories(List<AirMapStatusAdvisory> advisories) {
        this.advisories = advisories;
        return this;
    }

    public List<AirMapAvailablePermit> getApplicablePermits() {
        return applicablePermits;
    }

    public AirMapStatus setApplicablePermits(List<AirMapAvailablePermit> permits) {
        this.applicablePermits = permits;
        return this;
    }

    public List<AirMapPermitIssuer> getOrganizations() {
        return organizations;
    }

    public AirMapStatus setOrganizations(List<AirMapPermitIssuer> organizations) {
        this.organizations = organizations;
        return this;
    }
}
