package com.airmap.airmapsdk.util;

import android.content.Context;
import android.preference.PreferenceManager;
import android.support.annotation.StringRes;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.TypedValue;

import com.airmap.airmapsdk.AirMapException;
import com.airmap.airmapsdk.AirMapLog;
import com.airmap.airmapsdk.R;
import com.airmap.airmapsdk.models.Coordinate;
import com.airmap.airmapsdk.models.status.AirMapStatus;
import com.airmap.airmapsdk.networking.callbacks.AirMapCallback;
import com.airmap.airmapsdk.networking.services.AirMap;
import com.mapbox.mapboxsdk.annotations.PolygonOptions;
import com.mapbox.mapboxsdk.geometry.LatLng;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Vansh Gandhi on 7/25/16.
 * Copyright © 2016 AirMap, Inc. All rights reserved.
 */
@SuppressWarnings("unused")
public class Utils {
    public static final String REFRESH_TOKEN_KEY = "AIRMAP_SDK_REFRESH_TOKEN";

    public static Float dpToPixels(Context context, int dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
    }

    public static boolean useMetric(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(Constants.MEASUREMENT_SYSTEM, Constants.IMPERIAL_SYSTEM).equals(Constants.METRIC_SYSTEM);
    }

    public static String titleCase(String s) {
        if (TextUtils.isEmpty(s)) {
            return s;
        }

        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    /**
     * Converts pressure in millimeters of mercury (Hg) to hectoPascals (hPa)
     *
     * @param hg Pressure in mm of Hg
     * @return Pressing in hPa
     */
    public static Float hgToHpa(float hg) {
        return (float) (hg * 33.864);
    }

    public static double feetToMeters(double feet) {
        return feet * 0.3048;
    }

    public static double metersToFeet(double meters) {
        return meters * 3.2808;
    }

    public static String getIso8601StringFromDate(Date date) {
        if (date == null) {
            return null;
        }

        DateTimeFormatter dateTimeFormatter = ISODateTimeFormat.dateTime().withZoneUTC();
        return dateTimeFormatter.print(new DateTime(date));
    }

    /**
     * Formats a string into a @link{java.util Date} object
     *
     * @param iso8601 The ISO 8601 string to convert to a Date object
     * @return The converted Date
     */
    public static Date getDateFromIso8601String(String iso8601) {
        if (TextUtils.isEmpty(iso8601)) {
            return null;
        }

        try {
            DateTimeFormatter dateTimeFormatter = ISODateTimeFormat.dateTime().withZoneUTC();
            DateTime dateTime = dateTimeFormatter.parseDateTime(iso8601);
            return dateTime.toDate();
        } catch (UnsupportedOperationException | IllegalArgumentException e) {
            AirMapLog.e("AirMap Utils", "Error parsing date: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public static boolean statusSuccessful(JSONObject object) {
        return object != null && object.optString("status").equalsIgnoreCase("success");
    }

    public static void error(AirMapCallback listener, Exception e) {
        if (e != null && listener != null) {
            if (e.getMessage().toLowerCase().startsWith("unable to resolve host")) {
                listener.onError(new AirMapException("No internet connection"));
            } else if (!e.getMessage().toLowerCase().contains("canceled")) { //Not an error if it was canceled
                listener.onError(new AirMapException(e.getMessage()));
            }
        }
    }

    //So we don't have to be doing null checks constantly
    public static void error(AirMapCallback listener, int code, JSONObject json) {
        if (listener != null) {
            listener.onError(new AirMapException(code, json));
        }
    }

    /**
     * @return Default duration presets when creating a flight
     */
    public static StringNumberPair[] getDurationPresets() {
        return new StringNumberPair[]{
                new StringNumberPair(R.string.five_min, 5 * 60 * 1000L), //5 minutes in millis
                new StringNumberPair(R.string.ten_min, 10 * 60 * 1000L),
                new StringNumberPair(R.string.fifteen_min, 15 * 60 * 1000L),
                new StringNumberPair(R.string.thirty_min, 30 * 60 * 1000L),
                new StringNumberPair(R.string.forty_five_min, 45 * 60 * 1000L),
                new StringNumberPair(R.string.one_hour, 60 * 60 * 1000L),
                new StringNumberPair(R.string.one_half_hour, 90 * 60 * 1000L),
                new StringNumberPair(R.string.two_hour, 120 * 60 * 1000L),
                new StringNumberPair(R.string.two_half_hour, 150 * 60 * 1000L),
                new StringNumberPair(R.string.three_hour, 180 * 60 * 1000L),
                new StringNumberPair(R.string.three_half_hour, 210 * 60 * 1000L),
                new StringNumberPair(R.string.four_hour, 240 * 60 * 1000L)
        };
    }

    /**
     * @return Default altitude presets when creating a flight
     */
    public static StringNumberPair[] getAltitudePresets() {
        return new StringNumberPair[]{
                new StringNumberPair(R.string.fifty_feet, feetToMeters(50)),
                new StringNumberPair(R.string.one_hundred_feet, feetToMeters(100)),
                new StringNumberPair(R.string.two_hundred_feet, feetToMeters(200)),
                new StringNumberPair(R.string.three_hundred_feet, feetToMeters(300)),
                new StringNumberPair(R.string.four_hundred_feet, feetToMeters(400))
        };
    }

    /**
     * @return Default altitude presets when creating a flight
     */
    public static StringNumberPair[] getAltitudePresetsMetric() {
        return new StringNumberPair[]{
                new StringNumberPair(R.string.fifteen_meters, 15),
                new StringNumberPair(R.string.thirty_meters, 30),
                new StringNumberPair(R.string.sixty_meters, 60),
                new StringNumberPair(R.string.ninty_meters, 90),
                new StringNumberPair(R.string.one_twenty_meters, 120)
        };
    }

    /**
     * @return Default buffer presets when creating a flight
     */
    public static StringNumberPair[] getBufferPresets() {
        return new StringNumberPair[]{
                new StringNumberPair(R.string.twenty_five_feet, feetToMeters(25)),
                new StringNumberPair(R.string.fifty_feet, feetToMeters(50)),
                new StringNumberPair(R.string.seventy_feet, feetToMeters(75)),
                new StringNumberPair(R.string.hundred_feet, feetToMeters(100)),
                new StringNumberPair(R.string.one_twenty_five_feet, feetToMeters(125)),
                new StringNumberPair(R.string.one_fifty_feet, feetToMeters(150)),
                new StringNumberPair(R.string.one_seventy_five_feet, feetToMeters(175)),
                new StringNumberPair(R.string.two_hundred_feet, feetToMeters(200)),
                new StringNumberPair(R.string.two_fifty_feet, feetToMeters(250)),
                new StringNumberPair(R.string.three_hundred_feet, feetToMeters(300)),
                new StringNumberPair(R.string.three_fifty_feet, feetToMeters(350)),
                new StringNumberPair(R.string.four_hundred_feet, feetToMeters(400)),
                new StringNumberPair(R.string.four_fifty_feet, feetToMeters(450)),
                new StringNumberPair(R.string.five_hundred_feet, feetToMeters(500)),
                new StringNumberPair(R.string.six_hundred, feetToMeters(600)),
                new StringNumberPair(R.string.seven_hundred, feetToMeters(700)),
                new StringNumberPair(R.string.eight_hundred, feetToMeters(800)),
                new StringNumberPair(R.string.nine_hundred_feet, feetToMeters(900)),
                new StringNumberPair(R.string.one_thousand_feet, feetToMeters(1000)),
                new StringNumberPair(R.string.twelve_fifty_feet, feetToMeters(1250)),
                new StringNumberPair(R.string.fifteen_hundred_feet, feetToMeters(1500)),
                new StringNumberPair(R.string.seventeen_fifty_feet, feetToMeters(1750)),
                new StringNumberPair(R.string.two_thousand_feet, feetToMeters(2000)),
                new StringNumberPair(R.string.twenty_five_hundred, feetToMeters(2500)),
                new StringNumberPair(R.string.three_thousand_feet, feetToMeters(3000))
        };
    }

    /**
     * @return Default buffer presets when creating a flight
     */
    public static StringNumberPair[] getBufferPresetsMetric() {
        return new StringNumberPair[]{
                new StringNumberPair(R.string.ten_meters, 10),
                new StringNumberPair(R.string.fifteen_meters, 15),
                new StringNumberPair(R.string.twenty_meters, 20),
                new StringNumberPair(R.string.twenty_five_meters, 25),
                new StringNumberPair(R.string.thirty_meters, 30),
                new StringNumberPair(R.string.fifty_meters, 50),
                new StringNumberPair(R.string.sixty_meters, 60),
                new StringNumberPair(R.string.seventy_five_meters, 75),
                new StringNumberPair(R.string.one_hundred_meters, 100),
                new StringNumberPair(R.string.one_twenty_meters, 120),
                new StringNumberPair(R.string.one_fifty_meters, 150),
                new StringNumberPair(R.string.one_seventy_five_meters, 175),
                new StringNumberPair(R.string.two_hundred_meters, 200),
                new StringNumberPair(R.string.two_twenty_five_meters, 225),
                new StringNumberPair(R.string.two_fifty_meters, 250),
                new StringNumberPair(R.string.two_seventy_five_meters, 275),
                new StringNumberPair(R.string.three_hundred_meters, 300),
                new StringNumberPair(R.string.three_fifty_meters, 350),
                new StringNumberPair(R.string.four_hundred_meters, 400),
                new StringNumberPair(R.string.five_hundred_meters, 500),
                new StringNumberPair(R.string.six_hundred_meters, 600),
                new StringNumberPair(R.string.seven_fifty_meters, 750),
                new StringNumberPair(R.string.one_thousand_meters, 1000)
        };
    }

    public static int indexOfMeterPreset(double meters, StringNumberPair[] pairs) {
        for (int i = 0; i < pairs.length; i++) {
            StringNumberPair pair = pairs[i];
            if (pair.value.doubleValue() == meters) {
                return i;
            }
        }
        return -1;
    }

    public static int indexOfDurationPreset(long millis) {
        for (int i = 0; i < getDurationPresets().length; i++) {
            StringNumberPair pair = getDurationPresets()[i];
            if (pair.value.longValue() == millis) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Pair of String and a Number
     */
    public static class StringNumberPair {

        public StringNumberPair(@StringRes int label, long value) {
            this.label = label;
            this.value = new BigDecimal(value);
        }

        public StringNumberPair(@StringRes int label, double value) {
            this.label = label;
            this.value = new BigDecimal(value);
        }

        public int label;
        public BigDecimal value;
    }

    /**
     * Makes a polygon with many sides to simulate a circle
     *
     * @param radius     Radius of the circle to draw
     * @param coordinate Coordinate to draw the circle
     * @return A "circle"
     */
    public static PolygonOptions getCirclePolygon(double radius, Coordinate coordinate, int color) {
        //We'll make a polygon with 45 sides to make a "circle"
        int degreesBetweenPoints = 8; //45 sides
        int numberOfPoints = (int) Math.floor(360 / degreesBetweenPoints);
        double distRadians = radius / 6371000.0; // earth radius in meters
        double centerLatRadians = coordinate.getLatitude() * Math.PI / 180;
        double centerLonRadians = coordinate.getLongitude() * Math.PI / 180;
        ArrayList<LatLng> points = new ArrayList<>(); //array to hold all the points
        for (int index = 0; index < numberOfPoints; index++) {
            double degrees = index * degreesBetweenPoints;
            double degreeRadians = degrees * Math.PI / 180;
            double pointLatRadians = Math.asin(Math.sin(centerLatRadians) * Math.cos(distRadians) + Math.cos(centerLatRadians) * Math.sin(distRadians) * Math.cos(degreeRadians));
            double pointLonRadians = centerLonRadians + Math.atan2(Math.sin(degreeRadians) * Math.sin(distRadians) * Math.cos(centerLatRadians),
                    Math.cos(distRadians) - Math.sin(centerLatRadians) * Math.sin(pointLatRadians));
            double pointLat = pointLatRadians * 180 / Math.PI;
            double pointLon = pointLonRadians * 180 / Math.PI;
            LatLng point = new LatLng(pointLat, pointLon);
            points.add(point);
        }
        return new PolygonOptions().addAll(points).strokeColor(color).alpha(0.66f).fillColor(color);
    }

    public static int getStatusCircleColor(AirMapStatus latestStatus, Context context) {
        int color = 0;
        if (latestStatus != null) {
            AirMapStatus.StatusColor statusColor = latestStatus.getAdvisoryColor();
            if (statusColor == AirMapStatus.StatusColor.Red) {
                color = ContextCompat.getColor(context, R.color.airmap_red);
            } else if (statusColor == AirMapStatus.StatusColor.Yellow) {
                color = ContextCompat.getColor(context, R.color.airmap_yellow);
            } else if (statusColor == AirMapStatus.StatusColor.Green) {
                color = ContextCompat.getColor(context, R.color.airmap_green);
            }
        } else {
            color = 0x1E88E5;
        }
        return color;
    }

    public static String readInputStreamAsString(InputStream in) throws IOException {
        BufferedInputStream bis = new BufferedInputStream(in);
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        int result = bis.read();
        while (result != -1) {
            byte b = (byte) result;
            buf.write(b);
            result = bis.read();
        }
        return buf.toString();
    }

    public static String getMapboxApiKey() {
        try {
            return AirMap.getConfig().getJSONObject("mapbox").getString("access_token");
        } catch (JSONException e) {
            e.printStackTrace();
            throw new RuntimeException("Error getting mapbox key from airmap.config.json");
        }
    }

    public static String getCallbackUrl() {
        try {
            JSONObject auth0 = AirMap.getConfig().getJSONObject("auth0");
            return auth0.getString("callback_url");
        } catch (JSONException e) {
            throw new RuntimeException("client_id and/or callback_url not found in airmap.config.json");
        }
    }

    public static String getClientId() {
        try {
            JSONObject auth0 = AirMap.getConfig().getJSONObject("auth0");
            return auth0.getString("client_id");
        } catch (JSONException e) {
            throw new RuntimeException("client_id and/or callback_url not found in airmap.config.json");
        }
    }

    public static String getDebugUrl() {
        try {
            return AirMap.getConfig().getJSONObject("internal").getString("debug_url");
        } catch (JSONException e) {
            return "v2/";
        }
    }

    public static String getMqttDebugUrl() {
        try {
            return AirMap.getConfig().getJSONObject("internal").getString("mqtt_url");
        } catch (JSONException e) {
            return "v2/";
        }
    }

    public static String getTelemetryDebugUrl() {
        try {
            return AirMap.getConfig().getJSONObject("internal").getString("telemetry_url");
        } catch (JSONException e) {
            return "v2/";
        }
    }


}
