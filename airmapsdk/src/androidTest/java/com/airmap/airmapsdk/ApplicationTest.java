package com.airmap.airmapsdk;

import android.app.Application;
import android.test.ApplicationTestCase;

import com.airmap.airmapsdk.Models.Coordinate;
import com.airmap.airmapsdk.Models.Flight.AirMapFlight;
import com.airmap.airmapsdk.Models.Traffic.AirMapTraffic;
import com.airmap.airmapsdk.Networking.Callbacks.AirMapCallback;
import com.airmap.airmapsdk.Networking.Callbacks.AirMapTrafficListener;
import com.airmap.airmapsdk.Networking.Services.AirMap;
import com.airmap.airmapsdk.Networking.Services.TelemetryService;

import java.util.Date;
import java.util.List;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class ApplicationTest extends ApplicationTestCase<Application> {
    public ApplicationTest() {
        super(Application.class);
    }

    String token = "";

    public void testTelemetry() {
        AirMap.init(getContext(), token);
        AirMap.enableLogging(true);
        AirMapFlight flight = new AirMapFlight()
                .setCoordinate(new Coordinate(31.5, -118))
                .setStartsAt(new Date())
                .setPublic(true)
                .setMaxAltitude(100)
                .setBuffer(500)
                .setNotify(true);
        AirMap.createFlight(flight, new AirMapCallback<AirMapFlight>() {
            @Override
            public void onSuccess(AirMapFlight response) {
                TelemetryService service = new TelemetryService(response);
                service.sendMessage(response.getCoordinate(), 10, 15, 20, 1000f);
            }

            @Override
            public void onError(AirMapException e) {
                e.printStackTrace();
            }
        });

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void testTrafficService() {
        AirMap.init(getContext(), token);
        AirMap.enableLogging(true);
        AirMap.enableTrafficAlerts(new AirMapTrafficListener() {
            @Override
            public void onAddTraffic(List<AirMapTraffic> added) {

            }

            @Override
            public void onUpdateTraffic(List<AirMapTraffic> updated) {

            }

            @Override
            public void onRemoveTraffic(List<AirMapTraffic> removed) {

            }
        });
        try {
            Thread.sleep(600000);
        } catch (InterruptedException e) {
            fail();
            e.printStackTrace();
        }
    }
}