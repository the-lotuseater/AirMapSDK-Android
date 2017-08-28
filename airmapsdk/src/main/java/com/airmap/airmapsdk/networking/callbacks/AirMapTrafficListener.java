package com.airmap.airmapsdk.networking.callbacks;

import com.airmap.airmapsdk.models.traffic.AirMapTraffic;

import java.util.List;

/**
 * Created by Vansh Gandhi on 6/28/16.
 * Copyright © 2016 AirMap, Inc. All rights reserved.
 */
public interface AirMapTrafficListener {
    void onAddTraffic(List<AirMapTraffic> added);
    void onUpdateTraffic(List<AirMapTraffic> updated);
    void onRemoveTraffic(List<AirMapTraffic> removed);
}