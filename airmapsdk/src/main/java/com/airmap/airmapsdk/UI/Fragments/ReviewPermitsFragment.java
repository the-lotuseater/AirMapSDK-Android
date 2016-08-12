package com.airmap.airmapsdk.UI.Fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.airmap.airmapsdk.Models.Permits.AirMapAvailablePermit;
import com.airmap.airmapsdk.R;
import com.airmap.airmapsdk.UI.Activities.CustomPropertiesActivity;

import java.util.ArrayList;

/**
 * Created by Vansh Gandhi on 7/25/16.
 * Copyright © 2016 AirMap, Inc. All rights reserved.
 */
public class ReviewPermitsFragment extends Fragment {

    private static final String PERMITS = "permits";
    private static final int REQUEST_CUSTOM_PROPERTIES = 3;

    private ArrayList<AirMapAvailablePermit> selectedPermits;

    private ListView listView;

    public ReviewPermitsFragment() {
        // Required empty public constructor
    }

    public static ReviewPermitsFragment newInstance(ArrayList<AirMapAvailablePermit> selectedPermits) {
        ReviewPermitsFragment fragment = new ReviewPermitsFragment();
        Bundle args = new Bundle();
        args.putSerializable(PERMITS, selectedPermits);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_review_permits, container, false);
        selectedPermits = (ArrayList<AirMapAvailablePermit>) getArguments().getSerializable(PERMITS);
        initializeViews(view);
        populateViews();
        return view;
    }

    private void initializeViews(View view) {
        listView = (ListView) view.findViewById(R.id.airmap_list);
    }

    private void populateViews() {
        ArrayAdapter<AirMapAvailablePermit> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, selectedPermits);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getContext(), CustomPropertiesActivity.class);
                intent.putExtra(CustomPropertiesActivity.PERMIT, selectedPermits.get(position));
                startActivityForResult(intent, REQUEST_CUSTOM_PROPERTIES);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CUSTOM_PROPERTIES) {
            if (resultCode == Activity.RESULT_OK) {
                AirMapAvailablePermit permit = (AirMapAvailablePermit) data.getSerializableExtra(PERMITS);
                selectedPermits.remove(permit); //Will remove permit with old custom properties based on ID
                selectedPermits.add(permit); //Will add the permit with the updated custom properties
            }
        }
    }
}