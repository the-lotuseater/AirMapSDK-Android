package com.airmap.airmapsdk;

import android.content.Context;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.TypedValue;

import com.airmap.airmapsdk.models.Coordinate;
import com.airmap.airmapsdk.models.shapes.AirMapPolygon;
import com.airmap.airmapsdk.models.status.AirMapStatus;
import com.airmap.airmapsdk.networking.callbacks.AirMapCallback;
import com.airmap.airmapsdk.networking.services.AirMap;
import com.airmap.airmapsdk.util.Constants;
import com.mapbox.mapboxsdk.annotations.PolygonOptions;
import com.mapbox.mapboxsdk.geometry.LatLng;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;
import org.joda.time.format.ISODateTimeFormat;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

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

        return s.substring(0,1).toUpperCase() + s.substring(1);
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
                new StringNumberPair("5 min", 5 * 60 * 1000L), //5 minutes in millis
                new StringNumberPair("10 min", 10 * 60 * 1000L),
                new StringNumberPair("15 min", 15 * 60 * 1000L),
                new StringNumberPair("30 min", 30 * 60 * 1000L),
                new StringNumberPair("45 min", 45 * 60 * 1000L),
                new StringNumberPair("1 hr", 60 * 60 * 1000L),
                new StringNumberPair("1.5 hrs", 90 * 60 * 1000L),
                new StringNumberPair("2 hrs", 120 * 60 * 1000L),
                new StringNumberPair("2.5 hrs", 150 * 60 * 1000L),
                new StringNumberPair("3 hrs", 180 * 60 * 1000L),
                new StringNumberPair("3.5 hrs", 210 * 60 * 1000L),
                new StringNumberPair("4 hrs", 240 * 60 * 1000L)
        };
    }

    /**
     * @return Default altitude presets when creating a flight
     */
    public static StringNumberPair[] getAltitudePresets() {
        return new StringNumberPair[]{
                new StringNumberPair("50 ft", feetToMeters(50)),
                new StringNumberPair("100 ft", feetToMeters(100)),
                new StringNumberPair("200 ft", feetToMeters(200)),
                new StringNumberPair("300 ft", feetToMeters(300)),
                new StringNumberPair("400 ft", feetToMeters(400))
        };
    }

    /**
     * @return Default altitude presets when creating a flight
     */
    public static StringNumberPair[] getAltitudePresetsMetric() {
        return new StringNumberPair[]{
                new StringNumberPair("15m", 20),
                new StringNumberPair("30m", 30),
                new StringNumberPair("60m", 60),
                new StringNumberPair("90m", 90),
                new StringNumberPair("120m", 120)
        };
    }

    /**
     * @return Default buffer presets when creating a flight
     */
    public static StringNumberPair[] getBufferPresets() {
        return new StringNumberPair[]{
                new StringNumberPair("25 ft", feetToMeters(25)),
                new StringNumberPair("50 ft", feetToMeters(50)),
                new StringNumberPair("75 ft", feetToMeters(75)),
                new StringNumberPair("100 ft", feetToMeters(100)),
                new StringNumberPair("125 ft", feetToMeters(125)),
                new StringNumberPair("150 ft", feetToMeters(150)),
                new StringNumberPair("175 ft", feetToMeters(175)),
                new StringNumberPair("200 ft", feetToMeters(200)),
                new StringNumberPair("250 ft", feetToMeters(250)),
                new StringNumberPair("300 ft", feetToMeters(300)),
                new StringNumberPair("350 ft", feetToMeters(350)),
                new StringNumberPair("400 ft", feetToMeters(400)),
                new StringNumberPair("450 ft", feetToMeters(450)),
                new StringNumberPair("500 ft", feetToMeters(500)),
                new StringNumberPair("600 ft", feetToMeters(600)),
                new StringNumberPair("700 ft", feetToMeters(700)),
                new StringNumberPair("800 ft", feetToMeters(800)),
                new StringNumberPair("900 ft", feetToMeters(900)),
                new StringNumberPair("1000 ft", feetToMeters(1000)),
                new StringNumberPair("1250 ft", feetToMeters(1250)),
                new StringNumberPair("1500 ft", feetToMeters(1500)),
                new StringNumberPair("1750 ft", feetToMeters(1750)),
                new StringNumberPair("2000 ft", feetToMeters(2000)),
                new StringNumberPair("2500 ft", feetToMeters(2500)),
                new StringNumberPair("3000 ft", feetToMeters(3000))
        };
    }

    /**
     * @return Default buffer presets when creating a flight
     */
    public static StringNumberPair[] getBufferPresetsMetric() {
        return new StringNumberPair[]{
                new StringNumberPair("10m", 10),
                new StringNumberPair("15m", 15),
                new StringNumberPair("20m", 20),
                new StringNumberPair("25m", 25),
                new StringNumberPair("30m", 30),
                new StringNumberPair("50m", 50),
                new StringNumberPair("60m", 60),
                new StringNumberPair("75m", 75),
                new StringNumberPair("100m", 100),
                new StringNumberPair("125m", 125),
                new StringNumberPair("150m", 150),
                new StringNumberPair("175m", 175),
                new StringNumberPair("200m", 200),
                new StringNumberPair("225m", 225),
                new StringNumberPair("250m", 250),
                new StringNumberPair("275m", 275),
                new StringNumberPair("300m", 300),
                new StringNumberPair("350m", 350),
                new StringNumberPair("400m", 400),
                new StringNumberPair("500m", 500),
                new StringNumberPair("600m", 600),
                new StringNumberPair("750m", 750),
                new StringNumberPair("1000m", 1000)
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

        public StringNumberPair(String label, long value) {
            this.label = label;
            this.value = new BigDecimal(value);
        }

        public StringNumberPair(String label, double value) {
            this.label = label;
            this.value = new BigDecimal(value);
        }

        public String label;
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
        return new PolygonOptions().addAll(points).strokeColor(color).alpha(0.5f).fillColor(color);
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


    public static PolygonOptions getMapboxPolygon(AirMapPolygon airMapPolygon) {
        PolygonOptions polygonOptions = new PolygonOptions();
        for (Coordinate coordinate : airMapPolygon.getCoordinates()) {
            polygonOptions.add(new LatLng(coordinate.getLatitude(), coordinate.getLongitude()));
        }

        return polygonOptions;
    }
}
