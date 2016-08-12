package com.airmap.airmapsdk.Networking.Services;

import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.airmap.airmapsdk.Models.Permits.AirMapAvailablePermit;
import com.airmap.airmapsdk.Models.Permits.AirMapPilotPermit;
import com.airmap.airmapsdk.Networking.Callbacks.AirMapCallback;
import com.airmap.airmapsdk.Networking.Callbacks.GenericListOkHttpCallback;
import com.airmap.airmapsdk.Networking.Callbacks.GenericOkHttpCallback;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Vansh Gandhi on 7/21/16.
 * Copyright © 2016 AirMap, Inc. All rights reserved.
 */
@SuppressWarnings("unused")
class PermitService extends BaseService {
    /**
     * Get permits by permit ID and/or organization ID. At least one of these parameters must be
     * non-null
     *
     * @param permitIds      The IDs of the permit to get
     * @param organizationId The organization of the permit
     * @param listener       The callback that is invoked on success or error
     */
    public static void getPermits(@Nullable List<String> permitIds, @Nullable String organizationId,
                                  AirMapCallback<List<AirMapAvailablePermit>> listener) {
        Map<String, String> params = new HashMap<>();
        if (permitIds != null && !permitIds.isEmpty()) {
            params.put("ids", TextUtils.join(",", permitIds));
        }
        params.put("organization_id", organizationId);
        AirMap.getClient().get(permitBaseUrl, params, new GenericListOkHttpCallback(listener, AirMapAvailablePermit.class));
    }

    /**
     * Apply for a permit
     *
     * @param permit   The permit to apply for
     * @param listener The callback that is invoked on success or error
     */
    public static void applyForPermit(AirMapAvailablePermit permit, AirMapCallback<AirMapPilotPermit> listener) {
        String url = String.format(permitApplyUrl, permit.getId());
        AirMap.getClient().postWithJsonBody(url, permit.getAsParams(), new GenericOkHttpCallback(listener, AirMapPilotPermit.class));
    }
}
