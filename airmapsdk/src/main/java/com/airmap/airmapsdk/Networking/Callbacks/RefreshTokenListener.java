package com.airmap.airmapsdk.networking.callbacks;

import com.airmap.airmapsdk.AirMapException;

/**
 * Created by Vansh Gandhi on 8/10/16.
 * Copyright © 2016 AirMap, Inc. All rights reserved.
 */
public interface RefreshTokenListener {
    void onSuccess();
    void onError(AirMapException e);
}
