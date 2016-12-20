package com.airmap.airmapsdk.ui.fragments;

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

import com.airmap.airmapsdk.models.permits.AirMapAvailablePermit;
import com.airmap.airmapsdk.R;
import com.airmap.airmapsdk.models.permits.AirMapPilotPermit;
import com.airmap.airmapsdk.ui.activities.CustomPropertiesActivity;
import com.airmap.airmapsdk.util.Constants;

import java.util.ArrayList;

/**
 * Created by Vansh Gandhi on 7/25/16.
 * Copyright © 2016 AirMap, Inc. All rights reserved.
 */
public class ReviewPermitsFragment extends Fragment {

    private static final String PERMITS_SELECTED = "permits_to_apply_for";
    private static final String PERMITS_OWNED = "permits_owned";
    private static final int REQUEST_CUSTOM_PROPERTIES = 3;

    private ListView listView;
    private ArrayAdapter<AirMapAvailablePermit> adapter;

    public ReviewPermitsFragment() {
        // Required empty public constructor
    }

    public static ReviewPermitsFragment newInstance(ArrayList<AirMapAvailablePermit> selectedPermits, ArrayList<AirMapPilotPermit> permitsOwned) {
        ReviewPermitsFragment fragment = new ReviewPermitsFragment();
        Bundle args = new Bundle();
        args.putSerializable(PERMITS_SELECTED, selectedPermits);
        args.putSerializable(PERMITS_OWNED, permitsOwned);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_review_permits, container, false);
        initializeViews(view);
        populateViews();
        return view;
    }

    private void initializeViews(View view) {
        listView = (ListView) view.findViewById(R.id.airmap_list);
    }

    private void populateViews() {
        //noinspection unchecked
        ArrayList<AirMapAvailablePermit> selectedPermits = (ArrayList<AirMapAvailablePermit>) getArguments().getSerializable(PERMITS_SELECTED);
        selectedPermits = selectedPermits != null ? selectedPermits : new ArrayList<AirMapAvailablePermit>();

        final ArrayList<AirMapPilotPermit> pilotPermits = (ArrayList<AirMapPilotPermit>) getArguments().getSerializable(PERMITS_OWNED);

        adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, selectedPermits);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                AirMapAvailablePermit availablePermit = adapter.getItem(position);
                AirMapPilotPermit pilotPermit = null;
                if (pilotPermits != null && !pilotPermits.isEmpty()) {
                    for (AirMapPilotPermit p : pilotPermits) {
                        if (p.getShortDetails().getPermitId().equals(availablePermit.getId())) {
                            pilotPermit = p;
                            break;
                        }
                    }
                }

                Intent intent = new Intent(getContext(), CustomPropertiesActivity.class);
                intent.putExtra(Constants.AVAILABLE_PERMIT_EXTRA, availablePermit);
                if (pilotPermit != null) {
                    intent.putExtra(Constants.PERMIT_WALLET_EXTRA, pilotPermit);
                }
                startActivityForResult(intent, REQUEST_CUSTOM_PROPERTIES);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CUSTOM_PROPERTIES) {
            if (resultCode == Activity.RESULT_OK) {
                AirMapAvailablePermit permit = (AirMapAvailablePermit) data.getSerializableExtra(Constants.AVAILABLE_PERMIT_EXTRA);
                if (adapter != null) {
                    adapter.remove(permit); //Will remove permit with old custom properties based on ID
                    adapter.add(permit); //Will add the permit with the updated custom properties
                    adapter.notifyDataSetChanged();
                }
            }
        }
    }
}