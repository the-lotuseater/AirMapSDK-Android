package com.airmap.airmapsdk.models.status;

import android.support.annotation.Nullable;

import com.airmap.airmapsdk.models.AirMapBaseModel;
import com.airmap.airmapsdk.models.permits.AirMapAvailablePermit;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Vansh Gandhi on 7/19/16.
 * Copyright © 2016 AirMap, Inc. All rights reserved.
 */
@SuppressWarnings("unused")
public class AirMapStatusPermits implements Serializable, AirMapBaseModel {
    private String authorityName; //This has to be manually set (it is not from JSON)
    private List<AirMapAvailablePermit> applicablePermits;
    private List<AirMapAvailablePermit> availablePermits;

    public AirMapStatusPermits() {
        applicablePermits = new ArrayList<>();
        availablePermits = new ArrayList<>();
    }

    @Override
    public AirMapStatusPermits constructFromJson(JSONObject json) {
        if (json != null) {
            JSONArray typesArray = json.optJSONArray("applicablePermits");
            applicablePermits = new ArrayList<>();
            for (int i = 0; i < typesArray.length(); i++) {
                applicablePermits.add(new AirMapAvailablePermit(typesArray.optJSONObject(i)));
            }
        }
        return this;
    }

    public List<AirMapAvailablePermit> getApplicablePermits() {
        return applicablePermits;
    }

    public AirMapStatusPermits setApplicablePermits(List<AirMapAvailablePermit> applicablePermits) {
        this.applicablePermits = applicablePermits;
        return this;
    }

    public List<AirMapAvailablePermit> getAvailablePermits() {
        return availablePermits;
    }

    public AirMapStatusPermits setAvailablePermits(List<AirMapAvailablePermit> availablePermits) {
        this.availablePermits = availablePermits;
        return this;
    }

    @Nullable
    public String getAuthorityName() {
        return authorityName;
    }

    public AirMapStatusPermits setAuthorityName(String authorityName) {
        this.authorityName = authorityName;
        return this;
    }
}
