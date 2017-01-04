package com.airmap.airmapsdk;

import android.util.Log;

/**
 * Created by Vansh Gandhi on 6/21/16.
 * Copyright © 2016 AirMap, Inc. All rights reserved.
 */
@SuppressWarnings("unused")
public class AirMapLog {
    public static boolean ENABLED = false;
    public static boolean TESTING = false;

    public static void d(String tag, String message) {
        if (ENABLED) {
            if (TESTING) {
                System.out.println(tag + ": " + message);
            } else {
                Log.d(tag, message);
            }
        }
    }

    public static void v(String tag, String message) {
        if (ENABLED) {
            if (TESTING) {
                System.out.println(tag + ": " + message);
            } else {
                Log.v(tag, message);
            }
        }
    }

    public static void e(String tag, String message) {
        if (ENABLED) {
            if (TESTING) {
                System.out.println(tag + ": " + message);
            } else {
                Log.e(tag, message);
            }
        }
    }

    public static void e(String tag, String message, Exception e) {
        if (ENABLED) {
            if (TESTING) {
                System.out.println(tag + ": " + message);
            } else {
                Log.e(tag, message, e);
            }
        }
    }

    public static void i(String tag, String message) {
        if (ENABLED) {
            if (TESTING) {
                System.out.println(tag + ": " + message);
            } else {
                Log.i(tag, message);
            }
        }
    }
}
