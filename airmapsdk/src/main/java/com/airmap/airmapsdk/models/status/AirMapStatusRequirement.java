package com.airmap.airmapsdk.models.status;

import com.airmap.airmapsdk.models.AirMapBaseModel;

import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by Vansh Gandhi on 6/15/16.
 * Copyright © 2016 AirMap, Inc. All rights reserved.
 */
@SuppressWarnings("unused")
public class AirMapStatusRequirement implements Serializable, AirMapBaseModel {
    private AirMapStatusRequirementNotice notice;

    public AirMapStatusRequirement(JSONObject requirementJson) {
        constructFromJson(requirementJson);
    }

    public AirMapStatusRequirement() {
    }

    @Override
    public AirMapStatusRequirement constructFromJson(JSONObject json) {
        if (json != null) {
            setNotice(new AirMapStatusRequirementNotice(json.optJSONObject("notice")));
        }
        return this;
    }

    public AirMapStatusRequirementNotice getNotice() {
        return notice;
    }

    public AirMapStatusRequirement setNotice(AirMapStatusRequirementNotice notice) {
        this.notice = notice;
        return this;
    }
}
