package com.airmap.airmapsdktest;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.airmap.airmapsdk.models.airspace.AirMapAirspaceAdvisoryStatus;
import com.airmap.airmapsdk.models.rules.AirMapRuleset;
import com.airmap.airmapsdk.ui.activities.MyLocationMapActivity;
import com.airmap.airmapsdk.ui.views.AirMapMapView;

import java.util.List;

public class MapFragment extends Fragment implements AirMapMapView.MapListener {

    private static final String TAG = "MapFragment";

    private AirMapMapView mapView;                  // mapbox MapView wrapper
    private FloatingActionButton myLocationFab;       // FAB to view/change selected rulesets
    private FloatingActionButton themeFab;     // FAB to view advisories

    public static MapFragment newInstance() {
        return new MapFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        mapView = view.findViewById(R.id.map_view);
        mapView.onCreate(savedInstanceState);
        mapView.setMapListener(this);

        myLocationFab = view.findViewById(R.id.my_location_fab);
        myLocationFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MyLocationMapActivity) getActivity()).goToMyLocation();
            }
        });

        themeFab = view.findViewById(R.id.theme_fab);
        themeFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        return view;
    }

    @Override
    public void onMapReady() {
        ((MyLocationMapActivity) getActivity()).onMapReady(mapView.getMap());
    }

    @Override
    public void onMapLoaded() {

    }

    @Override
    public void onMapFailed(AirMapMapView.MapFailure failure) {
        /**
         *  Devices with an inaccurate date/time will not be able to load the mapbox map
         *  If the "automatic date/time" is disabled on the device and the map fails to load, recommend the user enable it
         */
        if (failure == AirMapMapView.MapFailure.INACCURATE_DATE_TIME_FAILURE) {
            // ask user to enabled "Automatic Date/Time"
            new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.error_loading_map_title)
                    .setMessage(R.string.error_loading_map_message)
                    .setPositiveButton(R.string.error_loading_map_button, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // open settings and kill this activity
                            startActivity(new Intent(Settings.ACTION_DATE_SETTINGS));
                            getActivity().finish();
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .show();

        } else if (failure == AirMapMapView.MapFailure.NETWORK_CONNECTION_FAILURE) {
            // ask user to turn on wifi/LTE
            new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.error_loading_map_title)
                    .setMessage(R.string.error_loading_map_network_message)
                    .setPositiveButton(R.string.error_loading_map_network_button, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // open settings and kill this activity
                            startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                            getActivity().finish();
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .show();
        }
    }

    @Override
    public void onRulesetsChanged(List<AirMapRuleset> availableRulesets, List<AirMapRuleset> selectedRulesets) {
        ((MapDemoActivity) getActivity()).setRulesets(availableRulesets, selectedRulesets);
    }

    @Override
    public void onAdvisoryStatusChanged(AirMapAirspaceAdvisoryStatus status) {
        ((MapDemoActivity) getActivity()).setAdvisoryStatus(status);
    }

    public void onRulesetSelected(AirMapRuleset ruleset) {
        mapView.onRulesetSelected(ruleset);
    }

    public void onRulesetDeselected(AirMapRuleset ruleset) {
        mapView.onRulesetDeselected(ruleset);
    }

    public void onRulesetSwitched(AirMapRuleset fromRuleset, AirMapRuleset toRuleset) {
        mapView.onRulesetSwitched(fromRuleset, toRuleset);
    }

    // Mapbox requires lifecycle
    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
}