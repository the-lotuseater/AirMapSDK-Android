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

    public static void logEvent(String section, String action, String label, String value) {
        if (tracker != null) {
            tracker.logEvent(section, action, label, value);
        } else {
            Log.e(TAG, "No tracker set!");
        }
    }

    public static class Page {
        public static final String CREATE_FLIGHT = "Create Flight";
        public static final String POINT_CREATE_FLIGHT = "Create Flight - Point";
        public static final String PATH_CREATE_FLIGHT = "Create Flight - Path";
        public static final String POLYGON_CREATE_FLIGHT = "Create Flight - Polygon";
        public static final String DETAILS_CREATE_FLIGHT = "Create Flight - Details";
        public static final String PERMITS_CREATE_FLIGHT = "Create Flight - Permits";
        public static final String AVAILABLE_PERMITS_CREATE_FLIGHT = "Create Flight - Available Permits";
        public static final String NOTICES_CREATE_FLIGHT = "Create Flight - Flight Notices";

        public static final String PERMIT_DETAILS = "Permit Details";
        public static final String PILOT_PROFILE = "Pilot Profile";
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
        public static final String draw = "draw";
        public static final String drop = "drop";
        public static final String start = "start";
    }

    public static class Label {
        public static final String START_CREATE_FLIGHT = "Start Create Flight";
        public static final String POINT = "Point";
        public static final String PATH = "Path";
        public static final String POLYGON = "Polygon";
        public static final String DRAG_POINT = "Drag Point";
        public static final String POINT_RADIUS = "Point Radius";
        public static final String WIDTH = "Width";
        public static final String ZOOM_MAP = "Zoom Map";
        public static final String ADVISORY_ICON = "Advisory Icon";
        public static final String TRASH_ICON = "Trash Icon";
        public static final String NEXT = "Next Button";
        public static final String CANCEL = "Cancel Button";
        public static final String REVIEW = "Review Button";
        public static final String DRAG_PATH_POINT = "Drag Path Point";
        public static final String DRAG_POLYGON_POINT = "Drag Polygon Point";
        public static final String DRAG_NEW_POINT = "Drag New Point";
        public static final String DRAW_PATH = "Draw Path";
        public static final String DRAW_POLYGON = "Draw Polygon";
        public static final String DROP_POINT_TRASH_ICON = "Drop Point Trash Icon";

        public static final String ALTITUDE_SLIDER = "Altitude Slider";
        public static final String FLIGHT_START_TIME = "Flight Start Time";
        public static final String FLIGHT_END_TIME = "Flight End Time";
        public static final String SELECT_PILOT = "Select Pilot";
        public static final String SELECT_AIRCRAFT = "Select Aircraft";
        public static final String SHARE_FLIGHT = "Share Flight";

        public static final String INFO_FAQ_BUTTON = "Info Button (Permit FAQ's)";
        public static final String SELECT_PERMIT = "Select Permit";
        public static final String SELECT_DIFFERENT_PERMIT = "Select a Different Permit";
        public static final String PERMIT_DETAILS = "Permit Details";
    }

    public static final class Value {
        public static final String AIRMAP = "AirMap";
    }
}
