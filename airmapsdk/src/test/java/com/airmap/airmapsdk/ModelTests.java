package com.airmap.airmapsdk;

import com.airmap.airmapsdk.Models.Aircraft.AirMapAircraft;
import com.airmap.airmapsdk.Models.Aircraft.AirMapAircraftManufacturer;
import com.airmap.airmapsdk.Models.Aircraft.AirMapAircraftModel;
import com.airmap.airmapsdk.Models.Comm.AirMapComm;
import com.airmap.airmapsdk.Models.Flight.AirMapFlight;
import com.airmap.airmapsdk.Models.Flight.AirMapFlightStatus;
import com.airmap.airmapsdk.Models.Pilot.AirMapPilot;
import com.airmap.airmapsdk.Models.Status.AirMapStatus;
import com.airmap.airmapsdk.Models.Status.AirMapStatusAdvisory;
import com.airmap.airmapsdk.Models.Status.AirMapStatusRequirement;
import com.airmap.airmapsdk.Models.Status.AirMapStatusWeather;
import com.airmap.airmapsdk.Models.Status.AirMapStatusWind;
import com.airmap.airmapsdk.Models.Traffic.AirMapTraffic;
import com.airmap.airmapsdk.Models.Traffic.AirMapTrafficProperties;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;

/**
 * Created by Vansh Gandhi on 6/15/16.
 * Copyright © 2016 AirMap, Inc. All rights reserved.
 */
public class ModelTests {
    @Test
    public void AirMapTrafficTest() throws Exception {
        AirMapTraffic airMapTraffic = new AirMapTraffic();
        assertEquals(airMapTraffic.getAltitude(), 0, 0);
        assertEquals(airMapTraffic.shouldShowAlert(), false);

        airMapTraffic.setTrueHeading(115);
        airMapTraffic.setGroundSpeedKt(72);
        assertEquals(airMapTraffic.getTrueHeading(), 115);
        assertEquals(airMapTraffic.getGroundSpeedKt(), 72);

        airMapTraffic.setShowAlert(true);
        assertEquals(airMapTraffic.shouldShowAlert(), true);
    }

    @Test
    public void AirMapTrafficPropertiesTest() {
        AirMapTrafficProperties amtp = new AirMapTrafficProperties();
        amtp.setAircraftId("12345");
        assertEquals(amtp.getAircraftId(), "12345");
    }

    @Test
    public void AirMapStatusTests() {
        AirMapStatus airmapstatus = new AirMapStatus();
        assertEquals(airmapstatus.getAdvisoryColor(), null);
        assertEquals("red", AirMapStatus.StatusColor.Red.toString());
        assertEquals(AirMapStatus.StatusColor.fromString("green"), AirMapStatus.StatusColor.Green);
    }

    @Test
    public void AirMapStatusAdvisoriesTest() {
        AirMapStatusAdvisory airMapStatusAdvisory = new AirMapStatusAdvisory();
        airMapStatusAdvisory.setColor(AirMapStatus.StatusColor.Yellow);
        assertEquals(airMapStatusAdvisory.getColor(), AirMapStatus.StatusColor.fromString("yellow"));
    }

    @Test
    public void AirMapStatusRequirementsTest() {
        AirMapStatusRequirement airMapStatusRequirement = new AirMapStatusRequirement();
        assertEquals(airMapStatusRequirement.getNotice(), null);
    }

    @Test
    public void AirMapStatusWeatherTest() {
        AirMapStatusWeather airMapStatusWeather = new AirMapStatusWeather();
        airMapStatusWeather.setTemperature(25);
        assertEquals(airMapStatusWeather.getTemperature(), 25, 0);
    }

    @Test
    public void AirMapStatusWeatherWind() {
        AirMapStatusWind wind = new AirMapStatusWind();
        assertEquals(wind.getGusting(), 0);
        wind.setHeading(-15);
        assertEquals(wind.getHeading(), -15);
    }

    @Test
    public void AirMapDroneTest() {
        AirMapAircraftModel airMapDrone = new AirMapAircraftModel();
        airMapDrone.setModelId("123456789");
        assertEquals(airMapDrone.getModelId(), "123456789");
    }

    @Test
    public void AirMapDroneManufacturerTest() {
        AirMapAircraftManufacturer airMapAircraftManufacturer = new AirMapAircraftManufacturer();
        airMapAircraftManufacturer.setName("Some name");
        assertEquals(airMapAircraftManufacturer.getName(), "Some name");
    }

    @Test
    public void AirMapAircraftTest() {
        AirMapAircraft airMapAircraft = new AirMapAircraft();
        airMapAircraft.setNickname("Some nickname");
        assertEquals(airMapAircraft.getNickname(), "Some nickname");
    }

    @Test
    public void AirMapCommTest() {
        AirMapComm airMapComm = new AirMapComm();
        airMapComm.setType("Some type");
        assertEquals(airMapComm.getType(), "Some type");
    }

    @Test
    public void AirMapFlightTest() {
        AirMapFlight airMapFlight = new AirMapFlight();
        assertFalse(airMapFlight.isPublic());
        airMapFlight.setMaxAltitude(75.3);
        assertEquals(airMapFlight.getMaxAltitude(), 75.3, 0);
    }

    @Test
    public void AirMapFlightNotificationStatusTes() {
        AirMapFlightStatus notificationStatus = new AirMapFlightStatus();
        assertEquals(AirMapFlightStatus.AirMapFlightStatusType.Rejected,
                AirMapFlightStatus.AirMapFlightStatusType.fromString("rejected"));
        notificationStatus.setId("ID Number");
        assertEquals(notificationStatus.getId(), "ID Number");
    }

    @Test
    public void AirMapPilotProfileTest() {
        AirMapPilot profile = new AirMapPilot();
        profile.setLastName("Doe");
        assertEquals(profile.getLastName(), "Doe");
    }
}