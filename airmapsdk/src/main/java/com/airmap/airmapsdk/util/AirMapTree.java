package com.airmap.airmapsdk.util;

import timber.log.Timber;

/**
 * Mapbox spams Timber with their map tile http requests. This tree ignores all logs with HTTPRequest as their tag
 */
public class AirMapTree extends Timber.DebugTree {

    @Override
    protected boolean isLoggable(String tag, int priority) {
        return !tag.equals("HTTPRequest");
    }
}
