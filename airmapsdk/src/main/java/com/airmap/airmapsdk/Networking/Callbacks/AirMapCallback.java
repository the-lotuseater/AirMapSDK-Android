package com.airmap.airmapsdk.Networking.Callbacks;

import com.airmap.airmapsdk.AirMapException;

/**
 * Created by Vansh Gandhi on 6/16/16.
 * Copyright © 2016 AirMap, Inc. All rights reserved.
 */
public interface AirMapCallback<T> {
    /**
     * Called when the request was successful
     * @param response The object response
     */
    void onSuccess(T response);

    /**
     * Called when the request was unsuccessful or there was an error making the request
     * @param e Specifics of the error
     */
    void onError(AirMapException e);
}