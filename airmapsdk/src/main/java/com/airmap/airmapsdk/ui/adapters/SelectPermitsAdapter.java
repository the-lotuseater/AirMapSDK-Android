package com.airmap.airmapsdk.ui.adapters;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.airmap.airmapsdk.R;
import com.airmap.airmapsdk.util.Utils;
import com.airmap.airmapsdk.models.permits.AirMapAvailablePermit;
import com.airmap.airmapsdk.models.permits.AirMapPilotPermit;
import com.airmap.airmapsdk.models.status.AirMapStatusPermits;
import com.airmap.airmapsdk.ui.activities.CustomPropertiesActivity;
import com.airmap.airmapsdk.util.Constants;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Vansh Gandhi on 7/19/16.
 * Copyright © 2016 AirMap, Inc. All rights reserved.
 */
public class SelectPermitsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int HEADER_VIEW_TYPE = 0;
    private static final int PERMIT_VIEW_TYPE = 1;
    private static final int INSTRUCTIONS_VIEW_TYPE = 2;

    private Activity activity;
    private List permitsAndHeadersList;
    private Set<String> applicablePermitIds;
    private Map<String, AirMapPilotPermit> walletPermitsMap;
    private SimpleDateFormat dateFormat;

    public SelectPermitsAdapter(Activity activity, AirMapStatusPermits statusPermits, List<AirMapPilotPermit> permitsFromWallet) {
        this.activity = activity;

        dateFormat = new SimpleDateFormat("M/d/yyyy");

        permitsAndHeadersList = new ArrayList<>();
        applicablePermitIds = new HashSet<>();

        walletPermitsMap = new HashMap<>();
        if (permitsFromWallet != null && !permitsFromWallet.isEmpty()) {
            for (AirMapPilotPermit permit : permitsFromWallet) {
                if ((permit.getStatus() == AirMapPilotPermit.PermitStatus.Accepted || permit.getStatus() == AirMapPilotPermit.PermitStatus.Pending) && (permit.getExpiresAt() == null || permit.getExpiresAt().after(new Date()))) {
                    // if there's already a wallet permit, use the one with the better status (accepted > pending > *)
                    AirMapPilotPermit duplicate = walletPermitsMap.get(permit.getShortDetails().getPermitId());
                    if (duplicate == null || (duplicate.getStatus() != AirMapPilotPermit.PermitStatus.Accepted)) {
                        walletPermitsMap.put(permit.getShortDetails().getPermitId(), permit);
                    }
                }
            }
        }

        List<AirMapAvailablePermit> existingPermits = new ArrayList<>();
        List<AirMapAvailablePermit> otherAvailablePermits = new ArrayList<>();
        List<AirMapAvailablePermit> nonAvailablePermits = new ArrayList<>();

        for (AirMapAvailablePermit applicablePermit : statusPermits.getApplicablePermits()) {
            applicablePermitIds.add(applicablePermit.getId());

            AirMapPilotPermit pilotPermit = walletPermitsMap.get(applicablePermit.getId());
            if (pilotPermit != null && (pilotPermit.getStatus() == AirMapPilotPermit.PermitStatus.Accepted || pilotPermit.getStatus() == AirMapPilotPermit.PermitStatus.Pending)
                    && (pilotPermit.getExpiresAt() == null || pilotPermit.getExpiresAt().after(new Date()))) {
                existingPermits.add(applicablePermit);
            } else {
                otherAvailablePermits.add(applicablePermit);
            }
        }

        for (AirMapAvailablePermit availablePermit : statusPermits.getAvailablePermits()) {
            if (!applicablePermitIds.contains(availablePermit.getId())) {
                nonAvailablePermits.add(availablePermit);
            }
        }

        // add Existing Permits header and section
        if (!existingPermits.isEmpty()) {
            permitsAndHeadersList.add(new Header("Existing Permits"));
            permitsAndHeadersList.addAll(existingPermits);
        }

        // add Other Available Permits
        if (!otherAvailablePermits.isEmpty()) {
            permitsAndHeadersList.add(new Header("Available Permits"));
            permitsAndHeadersList.addAll(otherAvailablePermits);
        }

        // add Non-Available Permits header and section
        if (!nonAvailablePermits.isEmpty()) {
            permitsAndHeadersList.add(new Header("Unavailable Permits"));
            permitsAndHeadersList.addAll(nonAvailablePermits);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case INSTRUCTIONS_VIEW_TYPE: {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_instructions, parent, false);
                return new InstructionsViewHolder(view);
            }
            case HEADER_VIEW_TYPE: {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.permit_header_item, parent, false);
                return new HeaderViewHolder(view);
            }
            case PERMIT_VIEW_TYPE: {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.available_permit_list_item, parent, false);
                return new PermitViewHolder(view);
            }
        }

        return null;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder viewHolder, int position) {
        if (viewHolder instanceof InstructionsViewHolder) {
            final InstructionsViewHolder holder = (InstructionsViewHolder) viewHolder;
            holder.titleTextView.setText(R.string.select_a_permit_title);
            holder.descriptionTextView.setText(applicablePermitIds.size() == 1 ? R.string.select_a_permit_description : R.string.select_a_permit_description_multiple);
        } else if (viewHolder instanceof HeaderViewHolder) {
            final HeaderViewHolder holder = (HeaderViewHolder) viewHolder;
            holder.titleTextView.setText(((Header) getItem(position)).name);
        } else if (viewHolder instanceof PermitViewHolder) {
            final PermitViewHolder holder = (PermitViewHolder) viewHolder;
            final AirMapAvailablePermit permit = (AirMapAvailablePermit) getItem(position);

            holder.permitNameTextView.setText(permit.getName());
            holder.permitDescriptionTextView.setText(permit.getDescription());

            AirMapPilotPermit walletPermit = walletPermitsMap.get(permit.getId());
            if (walletPermit != null && (walletPermit.getStatus() == AirMapPilotPermit.PermitStatus.Accepted || walletPermit.getStatus() == AirMapPilotPermit.PermitStatus.Pending)
                    && (walletPermit.getExpiresAt() == null || walletPermit.getExpiresAt().after(new Date()))) {
                holder.iconImageView.setVisibility(View.VISIBLE);
                holder.statusTextView.setText(activity.getString(R.string.permit_status, Utils.titleCase(walletPermit.getStatus().toString())));
                holder.statusTextView.setVisibility(View.VISIBLE);
                holder.expirationTextView.setText(activity.getString(R.string.permit_expiration,
                        walletPermit.getExpiresAt() != null ? dateFormat.format(walletPermit.getExpiresAt()) : "N/A"));
                holder.expirationTextView.setVisibility(View.VISIBLE);
            } else {
                holder.iconImageView.setVisibility(View.GONE);
                holder.statusTextView.setVisibility(View.GONE);
                holder.expirationTextView.setVisibility(View.GONE);
            }

            boolean isApplicable = applicablePermitIds.contains(permit.getId());

            if (isApplicable) {
                //enable view/text
                holder.permitNameTextView.setEnabled(true);
                holder.permitDescriptionTextView.setEnabled(true);
                holder.cardView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AirMapPilotPermit pilotPermit = walletPermitsMap.get(permit.getId());
                        if (pilotPermit != null && (pilotPermit.getStatus() == AirMapPilotPermit.PermitStatus.Accepted || pilotPermit.getStatus() == AirMapPilotPermit.PermitStatus.Pending)
                                && (pilotPermit.getExpiresAt() == null || pilotPermit.getExpiresAt().after(new Date()))) {
                            Intent data = new Intent();
                            data.putExtra(Constants.AVAILABLE_PERMIT_EXTRA, permit);
                            activity.setResult(Activity.RESULT_OK, data);
                            activity.finish();
                        } else {
                            Intent intent = new Intent(activity, CustomPropertiesActivity.class);
                            intent.putExtra(Constants.AVAILABLE_PERMIT_EXTRA, permit);
                            activity.startActivityForResult(intent, Constants.CUSTOM_PROPERTIES_REQUEST_CODE);
                        }
                    }
                });
                holder.cardView.setClickable(true);

                setElevation(holder.itemView, true);
            } else {
                //disable view/text
                holder.permitNameTextView.setEnabled(false);
                holder.permitDescriptionTextView.setEnabled(false);
                holder.cardView.setOnClickListener(null);
                holder.cardView.setClickable(false);

                setElevation(holder.itemView, false);
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void setElevation(View view, boolean elevate) {
        if (elevate) {
            view.setElevation(Utils.dpToPixels(activity, 2));
        } else {
            view.setElevation(0f);
        }
    }

    @Override
    public int getItemCount() {
        return permitsAndHeadersList.size() + 1;
    }

    public Object getItem(int position) {
        return permitsAndHeadersList.get(position - 1);
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return INSTRUCTIONS_VIEW_TYPE;
        } else if (getItem(position) instanceof Header) {
            return HEADER_VIEW_TYPE;
        } else {
            return PERMIT_VIEW_TYPE;
        }
    }

    class Header {
        public final String name;

        public Header(String name) {
            this.name = name;
        }
    }

    class PermitViewHolder extends RecyclerView.ViewHolder {

        public View cardView;
        public ImageView iconImageView;
        public TextView permitNameTextView;
        public TextView permitDescriptionTextView;
        public TextView statusTextView;
        public TextView expirationTextView;

        public PermitViewHolder(View itemView) {
            super(itemView);

            cardView = itemView.findViewById(R.id.card_view);
            iconImageView = (ImageView) itemView.findViewById(R.id.icon_image_view);
            permitNameTextView = (TextView) itemView.findViewById(R.id.permit_name_text_view);
            permitDescriptionTextView = (TextView) itemView.findViewById(R.id.permit_description_text_view);
            statusTextView = (TextView) itemView.findViewById(R.id.status_text_view);
            expirationTextView = (TextView) itemView.findViewById(R.id.expiration_text_view);
        }
    }

    class HeaderViewHolder extends RecyclerView.ViewHolder {
        public TextView titleTextView;

        public HeaderViewHolder(View itemView) {
            super(itemView);
            titleTextView = (TextView) itemView.findViewById(R.id.header_text_view);
        }
    }

    class InstructionsViewHolder extends RecyclerView.ViewHolder {
        public TextView titleTextView;
        public TextView descriptionTextView;

        public InstructionsViewHolder(View itemView) {
            super(itemView);
            titleTextView = (TextView) itemView.findViewById(R.id.title_text_view);
            descriptionTextView = (TextView) itemView.findViewById(R.id.description_text_view);
        }
    }
}
