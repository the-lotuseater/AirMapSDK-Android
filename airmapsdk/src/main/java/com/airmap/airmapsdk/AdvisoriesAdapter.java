package com.airmap.airmapsdk;

import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.airmap.airmapsdk.models.status.AirMapStatus;
import com.airmap.airmapsdk.models.status.AirMapStatusAdvisory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Vansh Gandhi on 11/8/16.
 * Copyright © 2016 AirMap, Inc. All rights reserved.
 */

public class AdvisoriesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int HEADER_TYPE = 0;
    private static final int ADVISORY_TYPE = 1;
    private List<AirMapStatusAdvisory> advisories;

    public AdvisoriesAdapter(List<AirMapStatusAdvisory> advisories) {
        this.advisories = new ArrayList<>();
        if (advisories == null) {
            return;
        }

        List<AirMapStatusAdvisory> redAdvisories = new ArrayList<>();
        List<AirMapStatusAdvisory> yellowAdvisories = new ArrayList<>();
        List<AirMapStatusAdvisory> greenAdvisories = new ArrayList<>();
        for (AirMapStatusAdvisory advisory : advisories) {
            if (advisory.getColor() == AirMapStatus.StatusColor.Red) {
                redAdvisories.add(advisory);
            } else if (advisory.getColor() == AirMapStatus.StatusColor.Yellow) {
                yellowAdvisories.add(advisory);
            } else if (advisory.getColor() == AirMapStatus.StatusColor.Green) {
                greenAdvisories.add(advisory);
            }
        }

        if (!redAdvisories.isEmpty()) {
            this.advisories.add(new AirMapStatusAdvisory().setId("HEADER").setColor(AirMapStatus.StatusColor.Red));
            this.advisories.addAll(redAdvisories);
        }
        if (!yellowAdvisories.isEmpty()) {
            this.advisories.add(new AirMapStatusAdvisory().setId("HEADER").setColor(AirMapStatus.StatusColor.Yellow));
            this.advisories.addAll(yellowAdvisories);
        }
        if (!greenAdvisories.isEmpty()) {
            this.advisories.add(new AirMapStatusAdvisory().setId("HEADER").setColor(AirMapStatus.StatusColor.Green));
            this.advisories.addAll(greenAdvisories);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == HEADER_TYPE) {
            View headerView = LayoutInflater.from(parent.getContext()).inflate(R.layout.airmap_header_advisory_list_item, parent, false);
            return new HeaderViewHolder(headerView);
        } else {
            View advisoryView = LayoutInflater.from(parent.getContext()).inflate(R.layout.airmap_advisory_list_item, parent, false);
            return new AdvisoryViewHolder(advisoryView);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        AirMapStatusAdvisory advisory = advisories.get(position);
        if (holder instanceof HeaderViewHolder) {
            if (advisory.getColor() == AirMapStatus.StatusColor.Red) {
                ((HeaderViewHolder) holder).headerTextView.setText(R.string.airmap_flight_strictly_regulated);
            } else if (advisory.getColor() == AirMapStatus.StatusColor.Yellow) {
                ((HeaderViewHolder) holder).headerTextView.setText(R.string.airmap_advisories);
            } else if (advisory.getColor() == AirMapStatus.StatusColor.Green) {
                ((HeaderViewHolder) holder).headerTextView.setText(R.string.airmap_informational);
            }
        } else if (holder instanceof AdvisoryViewHolder) {
            ((AdvisoryViewHolder) holder).advisoryNameTextView.setText(advisory.getName());
            if (advisory.getType() != null) {
                ((AdvisoryViewHolder) holder).advisoryTypeTextView.setText(advisory.getType().getTitle());
            } else {
                ((AdvisoryViewHolder) holder).advisoryTypeTextView.setText("");
            }
            if (advisory.getColor() == AirMapStatus.StatusColor.Red) {
                ((AdvisoryViewHolder) holder).colorBar.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.airmap_red));
            } else if (advisory.getColor() == AirMapStatus.StatusColor.Yellow) {
                ((AdvisoryViewHolder) holder).colorBar.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.airmap_yellow));
            } else if (advisory.getColor() == AirMapStatus.StatusColor.Green) {
                ((AdvisoryViewHolder) holder).colorBar.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.airmap_green));
            }
        }
    }

    @Override
    public int getItemCount() {
        return advisories.size();
    }

    @Override
    public int getItemViewType(int position) {
        return advisories.get(position).getId().equals("HEADER") ? HEADER_TYPE : ADVISORY_TYPE;
    }

    private static class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView headerTextView;
        public HeaderViewHolder(View itemView) {
            super(itemView);
            headerTextView = (TextView) itemView.findViewById(R.id.header_text);
        }
    }

    private static class AdvisoryViewHolder extends RecyclerView.ViewHolder {
        View colorBar;
        TextView advisoryNameTextView;
        TextView advisoryTypeTextView;
        public AdvisoryViewHolder(View itemView) {
            super(itemView);
            colorBar = itemView.findViewById(R.id.color_bar);
            advisoryNameTextView = (TextView) itemView.findViewById(R.id.advisory_name);
            advisoryTypeTextView = (TextView) itemView.findViewById(R.id.advisory_type);
        }
    }
}
