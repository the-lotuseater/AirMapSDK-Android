package com.airmap.airmapsdk;

/**
 * Created by collin@airmap.com on 12/19/16.
 */

public interface AnalyticsTracker {
    void logEvent(String section, String action, String label);

    void logEvent(String section, String action, String label, int value);

    void logEvent(String section, String action, String label, String value);

    void report(Exception e);
}
