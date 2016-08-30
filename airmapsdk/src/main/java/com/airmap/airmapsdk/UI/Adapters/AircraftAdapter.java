package com.airmap.airmapsdk.UI.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.airmap.airmapsdk.Models.Aircraft.AirMapAircraft;

import java.util.List;

/**
 * Created by Vansh Gandhi on 8/30/16.
 * Copyright © 2016 AirMap, Inc. All rights reserved.
 */
public class AircraftAdapter extends ArrayAdapter<AirMapAircraft> {
    public AircraftAdapter(Context context, List<AirMapAircraft> objects) {
        super(context, android.R.layout.simple_list_item_2, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        AirMapAircraft aircraft = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(android.R.layout.simple_list_item_2, parent, false);
        }
        ((TextView) (convertView.findViewById(android.R.id.text1))).setText(aircraft.getNickname());
        ((TextView) (convertView.findViewById(android.R.id.text2))).setText(aircraft.getModel().toString());
        return convertView;
    }
}