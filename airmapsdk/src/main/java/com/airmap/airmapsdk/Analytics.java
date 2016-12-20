package com.airmap.airmapsdk;

import android.util.Log;

/**
 * Created by collin@airmap.com on 12/19/16.
 */

public class Analytics {

    private static final String TAG = "SDKAnalytics";

    private static AnalyticsTracker tracker;

    public static void init(AnalyticsTracker analyticsTracker) {
        tracker = analyticsTracker;
    }

    public static void logEvent(String section, String action, String label) {
        if (tracker != null) {
            tracker.logEvent(section, action, label);
        } else {
            Log.e(TAG, "No tracker set!");
        }
    }

    public static void logEvent(String section, String action, String label, int value) {
        if (tracker != null) {
            tracker.logEvent(section, action, label, value);
        } else {
            Log.e(TAG, "No tracker set!");
        }
    }

    public static class Event {
        public static final String unspecified = "";
        public static final String verifyEmail = "email_verification_needed";
        public static final String noNetworkConnection = "no_network_connect";
        public static final String intro = "intro";
        public static final String futureFlightPlans = "future_flights";
        public static final String map = "map";
        public static final String mapLayers = "map_layers";
        public static final String mapLayersHelp = "uao_near_airports_infographic";
        public static final String search = "search";
        public static final String menu = "menu";
        public static final String myAircraft = "my_aircraft";
        public static final String flightPlans = "flights";
        public static final String activeUserFlightPlans = "active_user_flights";
        public static final String permitWallet = "permit_wallet";
        public static final String about = "about";
        public static final String faq = "faq";
        public static final String settings = "settings";
        public static final String login = "login_registration";
        public static final String myProfile = "my_profile";
        public static final String userProfile = "user_profile";
        public static final String flightPlan = "flight";
        public static final String createAirCraft = "create_aircraft";
        public static final String editAirCraft = "edit_aircraft";
        public static final String aircraftSelector = "aircraft_selector";
        public static final String createFlightPlan = "create_flight";
        public static final String editFlightPlan = "edit_flight";
        public static final String viewFlightPlan = "view_flight";
        public static final String flightRequirmentsForm = "flight_requirements_form";
        public static final String statusBottomSheet = "status_bottom_sheet";
        public static final String logout = "logout";
    }

    public static class Action {
        public static final String swipe = "swipe";
        public static final String switchControl = "switch";
        public static final String slide = "slide";
        public static final String tap = "tap";
        public static final String zoom = "zoom";
        public static final String toggle = "toggle";
        public static final String upload = "upload";
        public static final String delete = "delete";
        public static final String search = "search";
        public static final String longPress = "longPress";
        public static final String select = "select";
        public static final String drag = "drag";
        public static final String close = "close";
        public static final String exit = "exit";
    }
}
