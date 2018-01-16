package com.airmap.airmapsdk;

import android.util.Log;

@SuppressWarnings("unused")
public class AirMapLog {
    public static boolean ENABLED = false;
    public static boolean TESTING = false;

    public static void d(String tag, String message) {
        if (ENABLED) {
            if (TESTING) {
                System.out.println(tag + ": " + message);
            } else if (!message.isEmpty()) {
                Log.d(tag, message);
            }
        }
    }

    public static void v(String tag, String message) {
        if (ENABLED) {
            if (TESTING) {
                System.out.println(tag + ": " + message);
            } else if (!message.isEmpty()) {
                Log.v(tag, message);
            }
        }
    }

    public static void w(String tag, String message) {
        if (ENABLED) {
            if (TESTING) {
                System.out.println(tag + ": " + message);
            } else if (!message.isEmpty()) {
                Log.w(tag, message);
            }
        }
    }

    public static void e(String tag, String message) {
        if (ENABLED) {
            if (TESTING) {
                System.out.println(tag + ": " + message);
            } else if (!message.isEmpty()) {
                Log.e(tag, message);
            }
        }
    }

    public static void e(String tag, String message, Exception e) {
        if (ENABLED) {
            if (TESTING) {
                System.out.println(tag + ": " + message);
            } else if (!message.isEmpty()) {
                Log.e(tag, message, e);
            }
        }
    }

    public static void e(String tag, String message, Throwable e) {
        if (ENABLED) {
            if (TESTING) {
                System.out.println(tag + ": " + message);
            } else if (!message.isEmpty()) {
                Log.e(tag, message, e);
            }
        }
    }

    public static void i(String tag, String message) {
        if (ENABLED) {
            if (TESTING) {
                System.out.println(tag + ": " + message);
            } else if (!message.isEmpty()) {
                Log.i(tag, message);
            }
        }
    }
}