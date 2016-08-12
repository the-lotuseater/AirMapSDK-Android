package com.airmap.airmapsdk.Networking.Callbacks;

import com.airmap.airmapsdk.Auth;

/**
 * Internal use
 * Created by Vansh Gandhi on 8/10/16.
 * Copyright © 2016 AirMap, Inc. All rights reserved.
 */
public interface LoginCallback {
    void onSuccess(Auth.AuthCredential authCredential);

    void onEmailVerificationNeeded(String resendLink);

    void onErrorDomainBlackList();

    void onContinue();
}
