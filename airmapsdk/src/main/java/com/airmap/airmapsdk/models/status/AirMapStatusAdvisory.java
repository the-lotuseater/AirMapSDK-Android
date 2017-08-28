package com.airmap.airmapsdk.models.status;

import com.airmap.airmapsdk.models.AirMapBaseModel;
import com.airmap.airmapsdk.models.Coordinate;
import com.airmap.airmapsdk.models.permits.AirMapAvailablePermit;
import com.airmap.airmapsdk.models.status.properties.AirMapAirportProperties;
import com.airmap.airmapsdk.models.status.properties.AirMapControlledAirspaceProperties;
import com.airmap.airmapsdk.models.status.properties.AirMapEmergencyProperties;
import com.airmap.airmapsdk.models.status.properties.AirMapHeliportProperties;
import com.airmap.airmapsdk.models.status.properties.AirMapParkProperties;
import com.airmap.airmapsdk.models.status.properties.AirMapPowerPlantProperties;
import com.airmap.airmapsdk.models.status.properties.AirMapSchoolProperties;
import com.airmap.airmapsdk.models.status.properties.AirMapSpecialUseProperties;
import com.airmap.airmapsdk.models.status.properties.AirMapTfrProperties;
import com.airmap.airmapsdk.models.status.properties.AirMapWildfireProperties;
import com.airmap.airmapsdk.networking.services.MappingService;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.airmap.airmapsdk.util.Utils.getDateFromIso8601String;

/**
 * Created by Vansh Gandhi on 6/15/16.
 * Copyright © 2016 AirMap, Inc. All rights reserved.
 */
@SuppressWarnings("unused")
public class AirMapStatusAdvisory implements Serializable, AirMapBaseModel {
    private String id;
    private String name;
    private String organizationId;
    private MappingService.AirMapAirspaceType type;
    private String city;
    private String state;
    private String country;
    private Date lastUpdated;
    private AirMapStatus.StatusColor color;
    private int distance;
    private Coordinate coordinate;
    private AirMapStatusRequirement requirements;
    private String geometryString;
    private List<AirMapStatusAdvisoryRule> rules;
    private List<AirMapAvailablePermit> availablePermits;

    private AirMapAirportProperties airportProperties;
    private AirMapHeliportProperties heliportProperties;
    private AirMapControlledAirspaceProperties controlledAirspaceProperties;
    private AirMapParkProperties parkProperties;
    private AirMapPowerPlantProperties powerPlantProperties;
    private AirMapSchoolProperties schoolProperties;
    private AirMapSpecialUseProperties specialUseProperties;
    private AirMapTfrProperties tfrProperties;
    private AirMapWildfireProperties wildfireProperties;
    private AirMapEmergencyProperties emergencyProperties;

    /**
     * Initialize an AirMapStatusAdvisory from JSON
     *
     * @param advisoryJson A JSON representation of an AirMapStatusAdvisory
     */
    public AirMapStatusAdvisory(JSONObject advisoryJson) {
        constructFromJson(advisoryJson);
    }

    /**
     * Initialize an AirMapStatusAdvisory with default values
     */
    public AirMapStatusAdvisory() {

    }

    @Override
    public AirMapStatusAdvisory constructFromJson(JSONObject json) {
        if (json != null) {
            setId(json.optString("id"));
            setName(json.optString("name"));
            setOrganizationId(json.optString("organization_id"));
            setRequirements(new AirMapStatusRequirement(json.optJSONObject("requirements")));
            String typeString = json.optString("type");
            setType(MappingService.AirMapAirspaceType.fromString(typeString));
            setCountry(json.optString("country"));
            setDistance(json.optInt("distance"));
            setCity(json.optString("city"));
            setState(json.optString("state"));
            setColor(AirMapStatus.StatusColor.fromString(json.optString("color")));
            setGeometryString(json.optString("geometry"));
            double lat = json.optDouble("latitude");
            double lng = json.optDouble("longitude");
            if (lat != Double.NaN && lng != Double.NaN) {
                setCoordinate(new Coordinate(lat, lng));
            }
            setLastUpdated(getDateFromIso8601String(json.optString("last_updated")));

            List<AirMapStatusAdvisoryRule> rules = new ArrayList<>();
            JSONArray rulesArray = json.optJSONArray("rules");
            for (int i = 0; rulesArray != null && i < rulesArray.length(); i++) {
                rules.add(new AirMapStatusAdvisoryRule(rulesArray.optJSONObject(i)));
            }
            setRules(rules);

            List<AirMapAvailablePermit> availablePermits = new ArrayList<>();
            JSONArray availablePermitsJSON = json.optJSONArray("available_permits");
            for (int j = 0; availablePermitsJSON != null && j < availablePermitsJSON.length(); j++) {
                availablePermits.add(new AirMapAvailablePermit(availablePermitsJSON.optJSONObject(j)));
            }
            setAvailablePermits(availablePermits);

            JSONObject properties = json.optJSONObject("properties");
            if (type == MappingService.AirMapAirspaceType.Airport) {
                setAirportProperties(new AirMapAirportProperties(properties));
            } else if (type == MappingService.AirMapAirspaceType.Park) {
                setParkProperties(new AirMapParkProperties(properties));
            } else if (type == MappingService.AirMapAirspaceType.SpecialUse) {
                setSpecialUseProperties(new AirMapSpecialUseProperties(properties));
            } else if (type == MappingService.AirMapAirspaceType.PowerPlant) {
                setPowerPlantProperties(new AirMapPowerPlantProperties(properties));
            } else if (type == MappingService.AirMapAirspaceType.ControlledAirspace) {
                setControlledAirspaceProperties(new AirMapControlledAirspaceProperties(properties));
            } else if (type == MappingService.AirMapAirspaceType.School) {
                setSchoolProperties(new AirMapSchoolProperties(properties));
            } else if (type == MappingService.AirMapAirspaceType.TFR) {
                setTfrProperties(new AirMapTfrProperties(properties));
            } else if (type == MappingService.AirMapAirspaceType.Wildfires || type == MappingService.AirMapAirspaceType.Fires) {
                setWildfireProperties(new AirMapWildfireProperties(properties));
            } else if (type == MappingService.AirMapAirspaceType.Heliport) {
                setHeliportProperties(new AirMapHeliportProperties(properties));
            } else if (type == MappingService.AirMapAirspaceType.Emergencies) {
                setEmergencyProperties(new AirMapEmergencyProperties(properties));
            }
        }
        return this;
    }

    public int getDistance() {
        return distance;
    }

    public AirMapStatusAdvisory setDistance(int distance) {
        this.distance = distance;
        return this;
    }

    public String getId() {
        return id;
    }

    public AirMapStatusAdvisory setId(String id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public AirMapStatusAdvisory setName(String name) {
        this.name = name;
        return this;
    }

    public String getOrganizationId() {
        return organizationId;
    }

    public AirMapStatusAdvisory setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
        return this;
    }

    public MappingService.AirMapAirspaceType getType() {
        return type;
    }

    public AirMapStatusAdvisory setType(MappingService.AirMapAirspaceType type) {
        this.type = type;
        return this;
    }

    public String getCity() {
        return city;
    }

    public AirMapStatusAdvisory setCity(String city) {
        this.city = city;
        return this;
    }

    public String getState() {
        return state;
    }

    public AirMapStatusAdvisory setState(String state) {
        this.state = state;
        return this;
    }

    public String getCountry() {
        return country;
    }

    public AirMapStatusAdvisory setCountry(String country) {
        this.country = country;
        return this;
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public AirMapStatusAdvisory setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
        return this;
    }

    public AirMapStatus.StatusColor getColor() {
        return color;
    }

    public AirMapStatusAdvisory setColor(AirMapStatus.StatusColor color) {
        this.color = color;
        return this;
    }

    public AirMapStatusRequirement getRequirements() {
        return requirements;
    }

    public AirMapStatusAdvisory setRequirements(AirMapStatusRequirement requirements) {
        this.requirements = requirements;
        return this;
    }

    public AirMapAirportProperties getAirportProperties() {
        return airportProperties;
    }

    public AirMapStatusAdvisory setAirportProperties(AirMapAirportProperties airportProperties) {
        this.airportProperties = airportProperties;
        return this;
    }

    public AirMapHeliportProperties getHeliportProperties() {
        return heliportProperties;
    }

    public AirMapStatusAdvisory setHeliportProperties(AirMapHeliportProperties heliportProperties) {
        this.heliportProperties = heliportProperties;
        return this;
    }

    public AirMapControlledAirspaceProperties getControlledAirspaceProperties() {
        return controlledAirspaceProperties;
    }

    public AirMapStatusAdvisory setControlledAirspaceProperties(AirMapControlledAirspaceProperties controlledAirspaceProperties) {
        this.controlledAirspaceProperties = controlledAirspaceProperties;
        return this;
    }

    public AirMapParkProperties getParkProperties() {
        return parkProperties;
    }

    public AirMapStatusAdvisory setParkProperties(AirMapParkProperties parkProperties) {
        this.parkProperties = parkProperties;
        return this;
    }

    public AirMapPowerPlantProperties getPowerPlantProperties() {
        return powerPlantProperties;
    }

    public AirMapStatusAdvisory setPowerPlantProperties(AirMapPowerPlantProperties powerPlantProperties) {
        this.powerPlantProperties = powerPlantProperties;
        return this;
    }

    public AirMapSchoolProperties getSchoolProperties() {
        return schoolProperties;
    }

    public AirMapStatusAdvisory setSchoolProperties(AirMapSchoolProperties schoolProperties) {
        this.schoolProperties = schoolProperties;
        return this;
    }

    public AirMapSpecialUseProperties getSpecialUseProperties() {
        return specialUseProperties;
    }

    public AirMapStatusAdvisory setSpecialUseProperties(AirMapSpecialUseProperties specialUseProperties) {
        this.specialUseProperties = specialUseProperties;
        return this;
    }

    public AirMapTfrProperties getTfrProperties() {
        return tfrProperties;
    }

    public AirMapStatusAdvisory setTfrProperties(AirMapTfrProperties tfrProperties) {
        this.tfrProperties = tfrProperties;
        return this;
    }

    public AirMapWildfireProperties getWildfireProperties() {
        return wildfireProperties;
    }

    public AirMapStatusAdvisory setWildfireProperties(AirMapWildfireProperties wildfireProperties) {
        this.wildfireProperties = wildfireProperties;
        return this;
    }

    public AirMapEmergencyProperties getEmergencyProperties() {
        return emergencyProperties;
    }

    public AirMapStatusAdvisory setEmergencyProperties(AirMapEmergencyProperties emergencyProperties) {
        this.emergencyProperties = emergencyProperties;
        return this;
    }

    public Coordinate getCoordinate() {
        return coordinate;
    }

    public AirMapStatusAdvisory setCoordinate(Coordinate coordinate) {
        this.coordinate = coordinate;
        return this;
    }

    public String getGeometryString() {
        return geometryString;
    }

    public AirMapStatusAdvisory setGeometryString(String geometryString) {
        this.geometryString = geometryString;
        return this;
    }

    public List<AirMapStatusAdvisoryRule> getRules() {
        return rules;
    }

    public AirMapStatusAdvisory setRules(List<AirMapStatusAdvisoryRule> rules) {
        this.rules = rules;
        return this;
    }

    public List<AirMapAvailablePermit> getAvailablePermits() {
        return availablePermits;
    }

    public AirMapStatusAdvisory setAvailablePermits(List<AirMapAvailablePermit> availablePermits) {
        this.availablePermits = availablePermits;
        return this;
    }

    /**
     * Comparison based on ID
     */
    @Override
    public boolean equals(Object o) {
        return o instanceof AirMapStatusAdvisory && getId().equals(((AirMapStatusAdvisory) o).getId());
    }

    @Override
    public String toString() {
        return getName();
    }
}
