package com.airmap.airmapsdk.networking.services;

import android.support.annotation.Nullable;
import android.support.annotation.StringRes;

import com.airmap.airmapsdk.R;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Vansh Gandhi on 7/7/16.
 * Copyright © 2016 AirMap, Inc. All rights reserved.
 */
@SuppressWarnings("unused")
public class MappingService extends BaseService {

    public enum AirMapLayerType implements Serializable {
        TFRS("tfrs", R.string.tfrs_title, R.string.tfrs_description),
        Wildfires("wildfires", R.string.wildfires_title, R.string.wildfires_description),
        Prohibited("sua_prohibited", R.string.prohibited_area_title, R.string.prohibited_area_description),
        Restricted("sua_restricted", R.string.restricted_area_title, R.string.restricted_area_description),
        NationalParks("national_parks", R.string.national_park_title, R.string.national_park_description),
        NOAA("noaa", R.string.noaa_title, R.string.noaa_description),
        Schools("schools", R.string.school_title, R.string.school_description),
        Hospitals("hospitals", R.string.hospital_title, R.string.hospital_description),
        Heliports("heliports", R.string.heliport_title, R.string.heliport_description),
        PowerPlants("power_plants", R.string.power_plant_title, R.string.power_plant_description),
        AirportsCommercial("airports_commercial", R.string.airport_title, R.string.airport_description),
        AirportsRecreational("airports_recreational", R.string.airport_title, R.string.airport_description),
        AirportsCommercialPrivate("airports_commercial_private", R.string.private_airport_title, R.string.private_airport_description),
        AirportsRecreationalPrivate("airports_recreational_private", R.string.private_airport_title, R.string.private_airport_description),
        ClassB("class_b", R.string.class_b_title, R.string.class_b_description),
        ClassC("class_c", R.string.class_c_title, R.string.class_c_description),
        ClassD("class_d", R.string.class_d_title, R.string.class_d_description),
        ClassE("class_e0", R.string.class_e_title, R.string.class_e_description),
        HazardAreas("hazard_areas", R.string.hazard_areas_title, R.string.hazard_areas_description),
        AerialRecreationalAreas("aerial_recreational_areas", R.string.aeriel_rec_title, R.string.aeriel_rec_description),
        Cities("cities", R.string.cities_title, R.string.cities_description),
        Custom("custom", R.string.custom_title, R.string.custom_description),
        Prisons("prisons", R.string.prisons_title, R.string.prisons_description),
        Universities("universities", R.string.universities_title, R.string.universities_description),
        Other("aerial_recreational_areas,custom,hazard_areas,hospitals,power_plants,prisons,schools,universities,cities", R.string.other_cautionary_title, R.string.other_cautionary_description);

        private final String text;
        private final int title;
        private final int description;

        AirMapLayerType(String text, @StringRes int title, @StringRes int description) {
            this.text = text;
            this.title = title;
            this.description = description;
        }

        @Override
        public String toString() {
            return text;
        }

        public int getTitle() {
            return title;
        }

        public int getDescription() {
            return description;
        }

        public static AirMapLayerType fromString(String text) {
            switch (text) {
                case "tfrs":
                    return TFRS;
                case "wildfires":
                    return Wildfires;
                case "sua_prohibited":
                    return Prohibited;
                case "sua_restricted":
                    return Restricted;
                case "national_parks":
                    return NationalParks;
                case "noaa":
                    return NOAA;
                case "schools":
                    return Schools;
                case "hospitals":
                    return Hospitals;
                case "heliports":
                    return Heliports;
                case "power_plants":
                    return PowerPlants;
                case "airports_commercial":
                    return AirportsCommercial;
                case "airports_recreational":
                    return AirportsRecreational;
                case "airports_commercial_private":
                    return AirportsCommercialPrivate;
                case "airports_recreational_private":
                    return AirportsRecreationalPrivate;
                case "class_b":
                    return ClassB;
                case "class_c":
                    return ClassC;
                case "class_d":
                    return ClassD;
                case "class_e0":
                    return ClassE;
                case "hazard_areas":
                    return HazardAreas;
                case "aerial_recreational_areas":
                    return AerialRecreationalAreas;
                case "cities":
                    return Cities;
                case "custom":
                    return Custom;
                case "prisons":
                    return Prisons;
                case "universities":
                    return Universities;
                default:
                    return null;
            }
        }

        public AirMapAirspaceType getAirspaceType() {
            switch (this) {
                case TFRS:
                    return AirMapAirspaceType.TFR;
                case Wildfires:
                    return AirMapAirspaceType.Wildfires;
                case Prohibited:
                case Restricted:
                    return AirMapAirspaceType.SpecialUse;
                case NationalParks:
                case NOAA:
                    return AirMapAirspaceType.Park;
                case Schools:
                    return AirMapAirspaceType.School;
                case Hospitals:
                    return AirMapAirspaceType.Hospitals;
                case PowerPlants:
                    return AirMapAirspaceType.PowerPlant;
                case AirportsCommercial:
                case AirportsRecreational:
                case AirportsCommercialPrivate:
                case AirportsRecreationalPrivate:
                    return AirMapAirspaceType.Airport;
                case Heliports:
                case ClassB:
                case ClassC:
                case ClassD:
                case ClassE:
                    return AirMapAirspaceType.ControlledAirspace;
                case HazardAreas:
                    return AirMapAirspaceType.HazardArea;
                case AerialRecreationalAreas:
                    return AirMapAirspaceType.RecreationalArea;
                case Cities:
                    return AirMapAirspaceType.City;
                case Custom:
                    return AirMapAirspaceType.Custom;
                case Prisons:
                    return AirMapAirspaceType.Prison;
                case Universities:
                    return AirMapAirspaceType.University;
            }
            return null;
        }
    }

    public enum AirMapAirspaceType {
        Airport("airport", R.string.airport_title),
        Heliport("heliport", R.string.heliport_title),
        Park("park", R.string.park_title),
        PowerPlant("power_plant", R.string.power_plant_title),
        ControlledAirspace("controlled_airspace", R.string.controlled_airspace_title),
        School("school", R.string.school_title),
        SpecialUse("special_use_airspace", R.string.special_use_title),
        TFR("tfr", R.string.tfr_title),
        Wildfires("wildfire", R.string.wildfire_title),
        Hospitals("hospital", R.string.hospital_title),
        HazardArea("hazard_area", R.string.hazard_area_title),
        RecreationalArea("recreational_area", R.string.aeriel_rec_title),
        City("city", R.string.city_title),
        Custom("custom", R.string.custom_title),
        Prison("prison", R.string.prison_title),
        University("university", R.string.university_title);

        private final String text;
        private final int title;

        AirMapAirspaceType(String text, @StringRes int title) {
            this.text = text;
            this.title = title;
        }

        @Override
        public String toString() {
            return text;
        }

        public int getTitle() {
            return title;
        }

        public static AirMapAirspaceType fromString(String text) {
            switch (text) {
                case "airport":
                    return Airport;
                case "heliport":
                    return Heliport;
                case "park":
                    return Park;
                case "power_plant":
                    return PowerPlant;
                case "controlled_airspace":
                    return ControlledAirspace;
                case "school":
                    return School;
                case "special_use_airspace":
                    return SpecialUse;
                case "tfr":
                    return TFR;
                case "wildfire":
                    return Wildfires;
                case "hospital":
                    return Hospitals;
                case "hazard_area":
                    return HazardArea;
                case "recreational_area":
                    return RecreationalArea;
                case "city":
                    return City;
                case "custom":
                    return Custom;
                case "prison":
                    return Prison;
                case "university":
                    return University;
            }
            return null;
        }
    }

    public enum AirMapMapTheme {
        Standard("standard"),
        Light("light"),
        Dark("dark"),
        Satellite("satellite");

        private final String text;

        AirMapMapTheme(String text) {
            this.text = text;
        }

        @Override
        public String toString() {
            return text;
        }

        public static AirMapMapTheme fromString(String text) {
            switch (text) {
                case "standard":
                    return Standard;
                case "light":
                    return Light;
                case "dark":
                    return Dark;
                case "satellite":
                    return Satellite;
            }
            return null;
        }
    }

    protected String getTileSourceUrl(@Nullable List<AirMapLayerType> layers, AirMapMapTheme theme) {
        String tiles = (layers == null || layers.size() == 0) ? "_-_" : android.text.TextUtils.join(",", layers);
        return mapTilesBaseUrl + tiles + "?&theme=" + theme.toString() + "&apikey=" + AirMap.getInstance().getApiKey() + "&token=" + AirMap.getInstance().getApiKey();
    }
}
