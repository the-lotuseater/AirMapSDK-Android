package com.airmap.airmapsdk.networking.services;

import android.support.annotation.Nullable;

import com.airmap.airmapsdk.AirMapException;
import com.airmap.airmapsdk.models.comm.AirMapComm;
import com.airmap.airmapsdk.models.flight.AirMapFlight;
import com.airmap.airmapsdk.networking.callbacks.AirMapCallback;
import com.airmap.airmapsdk.networking.callbacks.GenericOkHttpCallback;
import com.airmap.airmapsdk.networking.callbacks.ListFlightsCallback;
import com.airmap.airmapsdk.networking.callbacks.VoidCallback;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Response;
import rx.Observable;

import static com.airmap.airmapsdk.util.Utils.getIso8601StringFromDate;

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
     * @param limit           Max number of flights to return
     * @param pilotId         Search for flights from a particular pilot
     * @param startAfter      Search for flights that start after this time
     * @param startBefore     Search for flights that start before this time
     * @param endAfter        Search for flights that end after this time
     * @param endBefore       Search for flights that end before this time
     * @param startsAfterNow  Search for flights starting after now
     * @param startsBeforeNow Search for flights starting before now
     * @param endsAfterNow    Search for flights ending after now
     * @param endsBeforeNow   Search for flights ending before now
     * @param country         Search for flights within this country (Length: 3, case insensitive)
     * @param city            Search for flights within this city
     * @param state           Search for flights within this state
     * @param enhanced        Returns enhanced flight, pilot, and aircraft information
     * @param listener        The callback that is invoked on success or error
     */
    public static void getFlights(@Nullable Integer limit, @Nullable String pilotId,
                                  @Nullable Date startAfter, @Nullable Date startBefore,
                                  @Nullable Date endAfter, @Nullable Date endBefore,
                                  @Nullable Boolean startsAfterNow, @Nullable Boolean startsBeforeNow,
                                  @Nullable Boolean endsAfterNow, @Nullable Boolean endsBeforeNow,
                                  @Nullable String country, @Nullable String city,
                                  @Nullable String state, @Nullable Boolean enhanced,
                                  @Nullable AirMapCallback<List<AirMapFlight>> listener) {
        Map<String, String> params = new HashMap<>();
        if (limit != null && limit > 0) {
            params.put("limit", limit.toString());
        }
        params.put("pilot_id", pilotId);
        params.put("start_after", startsAfterNow != null && startsAfterNow ? "now" : getIso8601StringFromDate(startAfter));
        params.put("start_before", startsBeforeNow != null && startsBeforeNow ? "now" : getIso8601StringFromDate(startBefore));
        params.put("end_after", endsAfterNow != null && endsAfterNow ? "now" : getIso8601StringFromDate(endAfter));
        params.put("end_before", endsBeforeNow != null && endsBeforeNow ? "now" : getIso8601StringFromDate(endBefore));
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
     * @param from     Search for flights from this date
     * @param to       Search for flights to this date
     * @param listener The callback that is invoked on success or error
     */
    //listPublicFlights
    public static void getPublicFlights(Integer limit, final Date from, final Date to,
                                        final AirMapCallback<List<AirMapFlight>> listener) {
        //endAfter is fromDate
        //startsBefore is toDate

        final boolean endAfterNow = from == null;
        final boolean startBeforeNow = to == null;
        AirMap.getFlights(limit, null, null, to, from, null, null, startBeforeNow, endAfterNow, null, null, null, null, true, new AirMapCallback<List<AirMapFlight>>() {
            @Override
            public void onSuccess(final List<AirMapFlight> publicFlights) {
                if (AirMap.hasValidAuthenticatedUser()) {
                    AirMap.getFlights(null, AirMap.getUserId(), null, to, from, null, null, startBeforeNow, endAfterNow, null, null, null, null, true, new AirMapCallback<List<AirMapFlight>>() {
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

    public static Observable<AirMapComm> getCommKey(AirMapFlight flight) {
        String url = String.format(flightStartCommUrl, flight.getFlightId());
        return AirMap.getClient().post(url, AirMapComm.class);
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
