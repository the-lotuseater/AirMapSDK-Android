package com.airmap.airmapsdk.models;

import com.mapbox.mapboxsdk.geometry.LatLng;

import java.io.Serializable;

/**
 * Class to validate and store Latitudes and Longitudes
 */
@SuppressWarnings("unused")
public class Coordinate implements Serializable {
    /**
     * The minimum allowed latitude
     */
    public static double MIN_LATITUDE = -90f;

    /**
     * The maximum allowed latitude
     */
    public static double MAX_LATITUDE = 90f;

    /**
     * The minimum allowed longitude
     */
    public static double MIN_LONGITUDE = -180f;

    /**
     * The maximum allowed longitude
     */
    public static double MAX_LONGITUDE = 180f;


    private double latitude;
    private double longitude;

    public Coordinate() {
        latitude = 0;
        longitude = 0;
    }

    public Coordinate(double lat, double lng) {
        setLatitude(lat);
        setLongitude(lng);
    }

    public Coordinate(LatLng mapboxLatLng) {
        mapboxLatLng = mapboxLatLng.wrap(); //In case the lat/lng are out of bounds
        setLatitude(mapboxLatLng.getLatitude());
        setLongitude(mapboxLatLng.getLongitude());
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        if (isValidLat(latitude)) {
            this.latitude = latitude;
        } else {
            throw new IllegalArgumentException("Latitude must be between -90 and 90");
        }
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        if (isValidLng(longitude)) {
            this.longitude = longitude;
        } else {
            throw new IllegalArgumentException("Longitude must be between -180 and 180");
        }
    }

    private static boolean isValidLat(double latitude) {
        return latitude >= MIN_LATITUDE && latitude <= MAX_LATITUDE;
    }

    private static boolean isValidLng(double longitude) {
        return longitude >= MIN_LONGITUDE && longitude <= MAX_LONGITUDE;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Coordinate && getLatitude() == ((Coordinate) o).getLatitude() && getLongitude() == ((Coordinate) o).getLongitude();
    }


    /**
     * @return GeoJSON representation of the coordinate
     */
    @Override
    public String toString() {
        return getLongitude() + " " + getLatitude();
    }
}
