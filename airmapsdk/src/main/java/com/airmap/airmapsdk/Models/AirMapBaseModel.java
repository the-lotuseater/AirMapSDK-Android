package com.airmap.airmapsdk.models;

import org.json.JSONObject;

/**
 * Created by Vansh Gandhi on 7/21/16.
 * Copyright © 2016 AirMap, Inc. All rights reserved.
 */
public interface AirMapBaseModel {
    AirMapBaseModel constructFromJson(JSONObject json);
}
