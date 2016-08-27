package com.airmap.airmapsdk.Networking.Services;

import android.support.annotation.Nullable;

import com.airmap.airmapsdk.AirMapException;
import com.airmap.airmapsdk.Models.Comm.AirMapComm;
import com.airmap.airmapsdk.Models.Flight.AirMapFlight;
import com.airmap.airmapsdk.Networking.Callbacks.AirMapCallback;
import com.airmap.airmapsdk.Networking.Callbacks.GenericOkHttpCallback;
import com.airmap.airmapsdk.Networking.Callbacks.ListFlightsCallback;
import com.airmap.airmapsdk.Networking.Callbacks.VoidCallback;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.airmap.airmapsdk.Utils.getIso8601StringFromDate;

/**
 * Created by Vansh Gandhi on 6/23/16.
 * Copyright © 2016 AirMap, Inc. All rights reserved.
 * This class handles all flight related requests.
 */
@SuppressWarnings("unused")
class FlightService extends BaseService {

    /**
     * List Flights. All parameters are optional and nullable
     *
     * @param limit       Max number of flights to return
     * @param pilotId     Search for flights from a particular pilot
     * @param startAfter  Search for flights that start after this time
     * @param startBefore Search for flights that start before this time
     * @param endAfter    Search for flights that end after this time
     * @param endBefore   Search for flights that end before this time
     * @param country     Search for flights within this country (Length: 3, case insensitive)
     * @param city        Search for flights within this city
     * @param state       Search for flights within this state
     * @param enhanced    Returns enhanced flight, pilot, and aircraft information
     * @param listener    The callback that is invoked on success or error
     */
    public static void getFlights(@Nullable Integer limit, @Nullable String pilotId,
                                  @Nullable Date startAfter, @Nullable Date startBefore,
                                  @Nullable Date endAfter, @Nullable Date endBefore,
                                  @Nullable String country, @Nullable String city,
                                  @Nullable String state, @Nullable Boolean enhanced,
                                  @Nullable AirMapCallback<List<AirMapFlight>> listener) {
        Map<String, String> params = new HashMap<>();
        if (limit != null && limit > 0) {
            params.put("limit", limit.toString());
        }
        params.put("pilot_id", pilotId);
        params.put("start_after", startAfter == null ? null : getIso8601StringFromDate(startAfter));
        params.put("start_before", startBefore == null ? null : getIso8601StringFromDate(startBefore));
        params.put("end_after", endAfter == null ? null : getIso8601StringFromDate(endAfter));
        params.put("end_before", endBefore == null ? null : getIso8601StringFromDate(endBefore));
        params.put("country", country);
        params.put("city", city);
        params.put("state", state);
        params.put("enhance", enhanced == null ? "false" : enhanced.toString());
        AirMap.getClient().get(flightGetAllUrl, params, new ListFlightsCallback(listener, AirMapFlight.class));
    }

    /**
     * Get a list of all public flights combined with the authenticated user's public and private
     * flights
     *
     * @param limit    Max number of flights to return
     * @param date     Search for active flights during this time
     * @param listener The callback that is invoked on success or error
     */
    public static void getAllPublicAndAuthenticatedPilotFlights(@Nullable Integer limit, final Date date, final AirMapCallback<List<AirMapFlight>> listener) {
        AirMap.getFlights(limit, null, null, date, new Date(date.getTime() + 60 * 1000), null, null, null, null, true, new AirMapCallback<List<AirMapFlight>>() {
            @Override
            public void onSuccess(final List<AirMapFlight> publicFlights) {
                if (AirMap.hasValidAuthenticatedUser()) {
                    AirMap.getFlights(null, AirMap.getUserId(), null, date, date, null, null, null, null, null, new AirMapCallback<List<AirMapFlight>>() {
                        @Override
                        public void onSuccess(List<AirMapFlight> authenticatedUserFlights) {
                            List<AirMapFlight> allFlights = new ArrayList<>(publicFlights);
                            allFlights.removeAll(authenticatedUserFlights); //Remove any would-be duplicates
                            allFlights.addAll(0, authenticatedUserFlights);
                            listener.onSuccess(allFlights);
                        }

                        @Override
                        public void onError(AirMapException e) {
                            listener.onError(e);
                        }
                    });
                } else {
                    listener.onSuccess(publicFlights);
                }
            }

            @Override
            public void onError(AirMapException e) {
                listener.onError(e);
            }
        });
    }

    /**
     * Get a flight by ID
     *
     * @param flightId The ID of the flight to get
     * @param enhance  If true, the response will include explicit profile and aircraft information
     *                 instead of just IDs
     * @param listener The callback that is invoked on success or error
     */
    public static void getFlight(String flightId, boolean enhance, AirMapCallback<AirMapFlight> listener) {
        String url = String.format(flightByIdUrl, flightId);
        Map<String, String> params = new HashMap<>();
        params.put("enhance", String.valueOf(enhance));
        AirMap.getClient().get(url, params, new GenericOkHttpCallback(listener, AirMapFlight.class));
    }

    /**
     * Create a flight for the user
     *
     * @param flight   The flight to create
     * @param listener The callback that is invoked on success or error
     */
    public static void createFlight(AirMapFlight flight, final AirMapCallback<AirMapFlight> listener) {
        String url = flightBaseUrl + flight.getGeometryType().toString();
        JSONObject params = flight.getAsParams();
        AirMap.getClient().postWithJsonBody(url, params, new GenericOkHttpCallback(listener, AirMapFlight.class));
    }

    /**
     * End a flight
     *
     * @param flight   The flight to end
     * @param listener The callback that is invoked on success or error
     */
    public static void endFlight(AirMapFlight flight, AirMapCallback<AirMapFlight> listener) {
        String url = String.format(flightEndUrl, flight.getFlightId());
        AirMap.getClient().post(url, new GenericOkHttpCallback(listener, AirMapFlight.class));
    }

    public static void deleteFlight(AirMapFlight flight, AirMapCallback<Void> listener) {
        String url = String.format(flightDeleteUrl, flight.getFlightId());
        AirMap.getClient().post(url, new VoidCallback(listener));
    }

    /**
     * Get a comm key for a given flight to enable traffic alerts
     *
     * @param flight   The flight to get the comm key for
     * @param listener The callback that is invoked on success or error
     */
    public static void getCommKey(AirMapFlight flight, final AirMapCallback<AirMapComm> listener) {
        String url = String.format(flightStartCommUrl, flight.getFlightId());
        AirMap.getClient().post(url, new GenericOkHttpCallback(listener, AirMapComm.class));
    }

    /**
     * Stop receiving notifications for traffic alerts
     *
     * @param flight   The flight to stop receiving notifications for
     * @param listener The callback that is invoked on success or error
     */
    public static void clearCommKey(AirMapFlight flight, final AirMapCallback<Void> listener) {
        String url = String.format(flightEndCommUrl, flight.getFlightId());
        AirMap.getClient().post(url, new VoidCallback(listener));
    }
}
