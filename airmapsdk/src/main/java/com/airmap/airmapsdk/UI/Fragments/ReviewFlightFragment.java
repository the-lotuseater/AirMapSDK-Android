package com.airmap.airmapsdk.UI.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.airmap.airmapsdk.AirMapException;
import com.airmap.airmapsdk.AirMapLog;
import com.airmap.airmapsdk.Models.Flight.AirMapFlight;
import com.airmap.airmapsdk.Models.Permits.AirMapAvailablePermit;
import com.airmap.airmapsdk.Models.Permits.AirMapPilotPermit;
import com.airmap.airmapsdk.Models.Status.AirMapStatus;
import com.airmap.airmapsdk.Models.Status.AirMapStatusRequirementNotice;
import com.airmap.airmapsdk.Networking.Callbacks.AirMapCallback;
import com.airmap.airmapsdk.Networking.Services.AirMap;
import com.airmap.airmapsdk.R;
import com.airmap.airmapsdk.Utils;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Vansh Gandhi on 7/25/16.
 * Copyright © 2016 AirMap, Inc. All rights reserved.
 */
public class ReviewFlightFragment extends Fragment implements OnMapReadyCallback {

    private OnFragmentInteractionListener mListener;

    private MapView mapView;
    private ViewPager viewPager;
    private Button submitButton;
    private FrameLayout progressBarContainer;
    private int totalNumberOfPermits;
    private int totalPermitsObtained = 0;

    private MapboxMap map;

    public ReviewFlightFragment() {
        //Required empty constructor
    }

    public static ReviewFlightFragment newInstance() {
        return new ReviewFlightFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_review_flight, container, false);
        initializeViews(view);
        setupMap(savedInstanceState);
        setupViewPager();
        return view;
    }

    private void initializeViews(View view) {
        mapView = (MapView) view.findViewById(R.id.airmap_map);
        viewPager = (ViewPager) view.findViewById(R.id.view_pager);
        progressBarContainer = (FrameLayout) view.findViewById(R.id.progress_bar_container);
        submitButton = (Button) view.findViewById(R.id.airmap_submit_button);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBarContainer.setVisibility(View.VISIBLE);
                applyPermitsToFlight();
            }
        });
        PagerTabStrip strip = (PagerTabStrip) view.findViewById(R.id.tab_strip);
        strip.setTabIndicatorColor(ContextCompat.getColor(getContext(), R.color.colorAccent));
    }

    private void applyPermitsToFlight() {
        totalNumberOfPermits = mListener.getSelectedPermits().size() + mListener.getPermitsToApplyFor().size();
        if (mListener.getPermitsToApplyFor().size() == 0) {
            submitFlight();
        } else {
            applyForPermits(); //Will call submitFlight after last permit is applied for
        }
    }

    private void applyForPermits() {
        for (AirMapAvailablePermit permitToApplyFor : mListener.getPermitsToApplyFor()) {
            AirMap.applyForPermit(permitToApplyFor, new AirMapCallback<AirMapPilotPermit>() {
                @Override
                public void onSuccess(AirMapPilotPermit response) {
                    mListener.getFlight().addPermitId(response.getId());
                    totalPermitsObtained++;
                    if (totalPermitsObtained == totalNumberOfPermits) {
                        submitFlight();
                    }
                }

                @Override
                public void onError(AirMapException e) {
                    submitButton.post(new Runnable() {
                        @Override
                        public void run() {
                            progressBarContainer.setVisibility(View.GONE);
                            Toast.makeText(getContext(), "Error applying for permit", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        }
    }

    private void submitFlight() {
        if (mListener.getFlight().getStartsAt().before(new Date())) { //If the startsAt date is in past
            mListener.getFlight().setStartsAt(null); //Default to current time
        }
        AirMap.createFlight(mListener.getFlight(), new AirMapCallback<AirMapFlight>() {
            @Override
            public void onSuccess(AirMapFlight response) {
                mListener.onFlightSubmitted(response);
            }

            @Override
            public void onError(AirMapException e) {
                submitButton.post(new Runnable() {
                    @Override
                    public void run() {
                        progressBarContainer.setVisibility(View.GONE);
                        Toast.makeText(getContext(), "Error submitting flight", Toast.LENGTH_SHORT).show();
                    }
                });
                AirMapLog.e("AirMapReviewFlight", e.getMessage());
                e.printStackTrace();
            }
        });
    }

    private void setupMap(Bundle savedInstanceState) {
        mapView.getMapAsync(this);
        mapView.onCreate(savedInstanceState);
    }

    @Override
    public void onMapReady(MapboxMap mapboxMap) {
        map = mapboxMap;
        LatLng position = new LatLng(mListener.getFlight().getCoordinate().getLatitude(), mListener.getFlight().getCoordinate().getLongitude());
        map.setCameraPosition(new CameraPosition.Builder().target(position).zoom(14).build());
        Icon icon = IconFactory.getInstance(getContext()).fromResource(R.drawable.airmap_flight_marker);
        map.addMarker(new MarkerOptions().position(position).icon(icon));
        map.addPolygon(Utils.getCirclePolygon(mListener.getFlight().getBuffer(), mListener.getFlight().getCoordinate()));
    }

    private void setupViewPager() {
        List<Fragment> fragments = new ArrayList<>();
        fragments.add(ReviewDetailsFragment.newInstance(mListener.getFlight()));
        if (!mListener.getPermitsToShowInReview().isEmpty()) {
            fragments.add(ReviewPermitsFragment.newInstance(mListener.getPermitsToShowInReview()));
        }
        if (!mListener.getNotices().isEmpty()) {
            fragments.add(ReviewNoticeFragment.newInstance(mListener.getFlightStatus(), mListener.getFlight().shouldNotify()));
        }
        viewPager.setAdapter(new SectionsPagerAdapter(fragments, getChildFragmentManager()));
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
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
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
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

    public interface OnFragmentInteractionListener {
        AirMapFlight getFlight();

        AirMapStatus getFlightStatus();

        ArrayList<AirMapAvailablePermit> getPermitsToApplyFor();

        ArrayList<AirMapPilotPermit> getSelectedPermits();

        ArrayList<AirMapAvailablePermit> getPermitsToShowInReview();

        ArrayList<AirMapStatusRequirementNotice> getNotices();

        void onFlightSubmitted(AirMapFlight response);
    }

    private class SectionsPagerAdapter extends FragmentStatePagerAdapter {
        private List<Fragment> data;

        public SectionsPagerAdapter(List<Fragment> fragments, FragmentManager fm) {
            super(fm);
            this.data = fragments;
        }

        @Override
        public Fragment getItem(int position) {
            return data.get(position);
        }

        @Override
        public int getCount() {
            return data.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Fragment fragment = getItem(position);
            if (fragment instanceof ReviewDetailsFragment) {
                return "Flight Details";
            } else if (fragment instanceof ReviewPermitsFragment) {
                return "Permits";
            } else if (fragment instanceof ReviewNoticeFragment) {
                return "Flight Notice";
            } else {
                return "Review";
            }
        }
    }
}
