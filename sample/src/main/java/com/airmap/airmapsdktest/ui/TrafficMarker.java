package com.airmap.airmapsdktest.ui;

import com.airmap.airmapsdk.models.traffic.AirMapTraffic;
import com.mapbox.mapboxsdk.annotations.Marker;

/**
 * Created by collin@airmap.com on 10/2/17.
 */

public class TrafficMarker extends Marker {
    private AirMapTraffic traffic;

    public TrafficMarker(TrafficMarkerOptions trafficMarkerOptions, AirMapTraffic traffic) {
        super(trafficMarkerOptions);
        this.traffic = traffic;
    }

    public AirMapTraffic getTraffic() {
        return traffic;
    }

    public void setTraffic(AirMapTraffic traffic) {
        this.traffic = traffic;
    }
}
