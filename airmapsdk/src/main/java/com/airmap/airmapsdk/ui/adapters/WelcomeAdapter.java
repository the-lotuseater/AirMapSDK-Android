package com.airmap.airmapsdk.ui.adapters;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.airmap.airmapsdk.R;
import com.airmap.airmapsdk.models.welcome.AirMapWelcomeResult;
import com.airmap.airmapsdk.ui.activities.WebActivity;
import com.airmap.airmapsdk.ui.activities.WelcomeDetailsActivity;
import com.airmap.airmapsdk.util.AirMapConstants;

import java.util.List;

/**
 * Created by Vansh Gandhi on 7/19/16.
 * Copyright © 2016 AirMap, Inc. All rights reserved.
 */
public class WelcomeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int RESULT_VIEW_TYPE = 1;

    private Activity activity;
    private List<AirMapWelcomeResult> welcomeResults;

    public WelcomeAdapter(Activity activity, List<AirMapWelcomeResult> welcomeResults) {
        this.activity = activity;
        this.welcomeResults = welcomeResults;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case RESULT_VIEW_TYPE: {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_welcome_result, parent, false);
                return new ResultViewHolder(view);
            }
        }

        return null;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder viewHolder, int position) {
        final ResultViewHolder holder = (ResultViewHolder) viewHolder;
        final AirMapWelcomeResult welcomeResult = getItem(position);

        holder.nameTextView.setText(welcomeResult.getJurisdictionName());

        final String description = TextUtils.isEmpty(welcomeResult.getSummary()) ? welcomeResult.getText() : welcomeResult.getSummary();
        holder.descriptionTextView.setText(description);

        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Automatically open the Web view if the welcome result has a url
                if (!TextUtils.isEmpty(welcomeResult.getUrl())) {
                    Intent intent = new Intent(activity, WebActivity.class);
                    intent.putExtra(Intent.EXTRA_TITLE, welcomeResult.getJurisdictionName());
                    intent.putExtra(AirMapConstants.URL_EXTRA, welcomeResult.getUrl());
                    activity.startActivity(intent);
                } else {
                    Intent intent = new Intent(activity, WelcomeDetailsActivity.class);
                    intent.putExtra(AirMapConstants.WELCOME_RESULT_EXTRA, welcomeResult);
                    activity.startActivity(intent);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return welcomeResults.size();
    }

    public AirMapWelcomeResult getItem(int position) {
        return welcomeResults.get(position);
    }

    @Override
    public int getItemViewType(int position) {
        return RESULT_VIEW_TYPE;
    }

    class ResultViewHolder extends RecyclerView.ViewHolder {

        public View cardView;
        public TextView nameTextView;
        public TextView descriptionTextView;

        public ResultViewHolder(View itemView) {
            super(itemView);

            cardView = itemView.findViewById(R.id.card_view);
            nameTextView = (TextView) itemView.findViewById(R.id.name_text_view);
            descriptionTextView = (TextView) itemView.findViewById(R.id.description_text_view);
        }
    }
}
