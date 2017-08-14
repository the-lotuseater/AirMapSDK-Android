package com.airmap.airmapsdk.models.rules;

import com.airmap.airmapsdk.models.AirMapBaseModel;

import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by collin@airmap.com on 8/11/17.
 */

public class AirMapAuthority implements Serializable, AirMapBaseModel {

    private String name;

    private AirMapAuthority() {
    }

    public AirMapAuthority(JSONObject jsonObject) {
        constructFromJson(jsonObject);
    }

    @Override
    public AirMapBaseModel constructFromJson(JSONObject json) {
        AirMapAuthority authority = new AirMapAuthority();
        authority.setName(json.optString("name"));
        return authority;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
