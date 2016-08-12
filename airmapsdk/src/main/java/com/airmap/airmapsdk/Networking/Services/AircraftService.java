package com.airmap.airmapsdk.Networking.Services;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.airmap.airmapsdk.Models.Aircraft.AirMapAircraftManufacturer;
import com.airmap.airmapsdk.Models.Aircraft.AirMapAircraftModel;
import com.airmap.airmapsdk.Networking.Callbacks.AirMapCallback;
import com.airmap.airmapsdk.Networking.Callbacks.GenericListOkHttpCallback;
import com.airmap.airmapsdk.Networking.Callbacks.GenericOkHttpCallback;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Vansh Gandhi on 6/23/16.
 * Copyright © 2016 AirMap, Inc. All rights reserved.
 */
@SuppressWarnings("unused")
class AircraftService extends BaseService {

    /**
     * Get a list of all Aircraft Manufacturers
     *
     * @param listener The callback that is invoked on success or error
     */
    public static void getManufacturers(final AirMapCallback<List<AirMapAircraftManufacturer>> listener) {
        String url = aircraftManufacturersUrl;
        AirMap.getClient().get(url, new GenericListOkHttpCallback(listener, AirMapAircraftManufacturer.class));
    }

    /**
     * Get a list of all Aircraft Manufacturers
     *
     * @param query    manufacturer to search for
     * @param listener The callback that is invoked on success or error
     */
    public static void getManufacturers(String query, final AirMapCallback<List<AirMapAircraftManufacturer>> listener) {
        String url = aircraftManufacturersUrl;
        Map<String, String> params = new HashMap<>();
        params.put("q", query);
        AirMap.getClient().get(url, params, new GenericListOkHttpCallback(listener, AirMapAircraftManufacturer.class));
    }

    /**
     * Get a list of all Aircraft Models
     *
     * @param listener The callback that is invoked on success or error
     */
    public static void getModels(AirMapCallback<List<AirMapAircraftModel>> listener) {
        String url = aircraftModelsUrl;
        AirMap.getClient().get(url, new GenericListOkHttpCallback(listener, AirMapAircraftModel.class));
    }

    /**
     * Get a model by ID
     *
     * @param query          The model to search for
     * @param manufacturerId The ID of the manufacturer to get models for
     * @param listener       The callback that is invoked on success or error
     */
    public static void getModels(@Nullable String query, @Nullable String manufacturerId, AirMapCallback<List<AirMapAircraftModel>> listener) {
        String url = aircraftModelsUrl;
        Map<String, String> params = new HashMap<>();
        params.put("manufacturer", manufacturerId);
        params.put("q", query);
        AirMap.getClient().get(url, params, new GenericListOkHttpCallback(listener, AirMapAircraftModel.class));
    }

    /**
     * Get a model by ID
     *
     * @param manufacturerId The ID of the manufacturer to get models for
     * @param listener       The callback that is invoked on success or error
     */
    public static void getModels(String manufacturerId, AirMapCallback<List<AirMapAircraftModel>> listener) {
        String url = aircraftModelsUrl;
        Map<String, String> params = new HashMap<>();
        params.put("manufacturer", manufacturerId);
        AirMap.getClient().get(url, params, new GenericListOkHttpCallback(listener, AirMapAircraftModel.class));
    }

    /**
     * Get a model by ID
     *
     * @param modelId  The ID of the model to get
     * @param listener The callback that is invoked on success or error
     */
    public static void getModel(@NonNull String modelId, AirMapCallback<AirMapAircraftModel> listener) {
        String url = String.format(aircraftModelUrl, modelId);
        AirMap.getClient().get(url, new GenericOkHttpCallback(listener, AirMapAircraftModel.class));
    }
}
