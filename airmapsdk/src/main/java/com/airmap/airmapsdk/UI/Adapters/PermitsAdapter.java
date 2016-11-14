package com.airmap.airmapsdk.ui.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.airmap.airmapsdk.AirMapException;
import com.airmap.airmapsdk.AirMapLog;
import com.airmap.airmapsdk.models.permits.AirMapAvailablePermit;
import com.airmap.airmapsdk.models.permits.AirMapPilotPermit;
import com.airmap.airmapsdk.models.status.AirMapStatusPermits;
import com.airmap.airmapsdk.networking.callbacks.AirMapCallback;
import com.airmap.airmapsdk.networking.services.AirMap;
import com.airmap.airmapsdk.R;
import com.airmap.airmapsdk.ui.fragments.ListPermitsFragment;
import com.airmap.airmapsdk.ui.views.PermitRadioButton;
import com.airmap.airmapsdk.ui.views.PermitRadioGroup;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Vansh Gandhi on 7/19/16.
 * Copyright © 2016 AirMap, Inc. All rights reserved.
 */
public class PermitsAdapter extends RecyclerView.Adapter<PermitsAdapter.ViewHolder> {

    private ArrayList<AirMapStatusPermits> statusPermits;
    private ArrayList<AirMapAvailablePermit> enabledPermits;
    private ArrayList<AirMapAvailablePermit> selectedPermits;
    private ListPermitsFragment.OnFragmentInteractionListener mListener;
    private ListPermitsFragment fragment;

    public PermitsAdapter(ArrayList<AirMapStatusPermits> statusPermits, ArrayList<AirMapPilotPermit> permitsFromWallet, ListPermitsFragment listPermitsFragment, ListPermitsFragment.OnFragmentInteractionListener mListener) {
        this.statusPermits = statusPermits;
        this.mListener = mListener;
        this.fragment = listPermitsFragment;
        enabledPermits = new ArrayList<>();
        selectedPermits = new ArrayList<>();

        List<String> permitIds = new ArrayList<>();
        for (AirMapPilotPermit permit : permitsFromWallet) {
            permitIds.add(permit.getShortDetails().getPermitId());
        }
        if (!permitIds.isEmpty()) {
            AirMap.getPermits(permitIds, null, new AirMapCallback<List<AirMapAvailablePermit>>() { //So that we can get other information about the permit, such as its name
                @Override
                public void onSuccess(final List<AirMapAvailablePermit> response) {
                    if (fragment != null && fragment.getActivity() != null) {
                        fragment.getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                addEnabledPermits(response);
                            }
                        });
                    }
                }

                @Override
                public void onError(AirMapException e) {
                    e.printStackTrace();
                    AirMapLog.e("PermitsAdapter", e.getMessage());
                }
            });
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.permit_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.permit = getItem(position);
        holder.permitRadioGroup.removeAllViews();
        holder.enabledPermits = new ArrayList<>();
        holder.permitRadioGroup.setOnCheckedChangeListener(new PermitRadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(PermitRadioGroup group, int checkedId) {
                PermitRadioButton radioButton = (PermitRadioButton) holder.permitRadioGroup.findViewById(checkedId);
                if (radioButton.isChecked()) {
                    int index = holder.permitRadioGroup.indexOfChild(radioButton);
                    if (holder.checkedPermit != null) {
                        selectedPermits.remove(holder.checkedPermit);
                    }
                    holder.checkedPermit = holder.enabledPermits.get(index);
                    if (!selectedPermits.contains(holder.checkedPermit)) {
                        selectedPermits.add(holder.checkedPermit);
                        fragment.updateSummaryText();
                        fragment.updateNextButton();
                    }
                }
            }
        });

        for (AirMapAvailablePermit permit : holder.permit.getApplicablePermits()) { //For each possible permit type that this authority has for this flight
            if (enabledPermits.contains(permit)) { //Check if that permit has been enabled (either through decision flow or from user's wallet)
                holder.enabledPermits.add(enabledPermits.get(enabledPermits.indexOf(permit)));
                final PermitRadioButton button = new PermitRadioButton(holder.permitRadioGroup.getContext()); //Make a RadioButton for that enabled permit
                button.setTitle(permit.getName());
                button.setDescription(permit.getDescription());

                holder.permitRadioGroup.addView(button);
                if (permit.equals(holder.checkedPermit) || selectedPermits.contains(permit)) {
                    holder.checkedPermit = permit;
                    holder.permitRadioGroup.check(button.getId());
                }
            }
        }

        // if only one permit available already in wallet, preselect that permit
        if (holder.permitRadioGroup.getChildCount() == 1) {
            holder.permitRadioGroup.check(holder.permitRadioGroup.getChildAt(0).getId());
        }

        holder.permitAuthorityTextView.setText(holder.permit.getAuthorityName());
        holder.selectPermitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.selectPermit(holder.permit);
            }
        });
        holder.selectPermitButton.setText(holder.enabledPermits.isEmpty() ? fragment.getString(R.string.airmap_select_a_permit) : fragment.getString(R.string.airmap_select_different_permit));
    }

    @Override
    public int getItemCount() {
        return statusPermits.size();
    }

    public AirMapStatusPermits getItem(int position) {
        return statusPermits.get(position);
    }

    public ArrayList<AirMapAvailablePermit> getEnabledPermits() {
        return enabledPermits;
    }

    public ArrayList<AirMapAvailablePermit> getSelectedPermits() {
        return selectedPermits;
    }

    public void addEnabledPermit(AirMapAvailablePermit permit) {
        if (!enabledPermits.contains(permit)) {
            enabledPermits.add(permit);
            notifyDataSetChanged();
        }
    }

    public void addSelectedPermit(AirMapAvailablePermit permit) {
        Iterator<AirMapAvailablePermit> iterator = selectedPermits.iterator();
        AirMapAvailablePermit permitToRemove = null;
        while (iterator.hasNext()) {
            AirMapAvailablePermit selectedPermit = iterator.next();
            if (selectedPermit.getOrganizationId().equals(permit.getOrganizationId())) {
                permitToRemove = selectedPermit;
                break;
            }
        }
        if (permitToRemove != null) {
            selectedPermits.remove(permitToRemove);
        }
        selectedPermits.add(permit);
        notifyDataSetChanged();
    }

    public void addEnabledPermits(List<AirMapAvailablePermit> permits) {
        for (AirMapAvailablePermit permit : permits) {
            if (!enabledPermits.contains(permit)) {
                enabledPermits.add(permit);
            }
        }
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView permitAuthorityTextView;
        public PermitRadioGroup permitRadioGroup;
        public Button selectPermitButton;
        public AirMapStatusPermits permit;
        public ArrayList<AirMapAvailablePermit> enabledPermits;
        public AirMapAvailablePermit checkedPermit;

        public ViewHolder(View itemView) {
            super(itemView);
            permitAuthorityTextView = (TextView) itemView.findViewById(R.id.permit_authority);
            permitRadioGroup= (PermitRadioGroup) itemView.findViewById(R.id.permit_radio_group);
            selectPermitButton = (Button) itemView.findViewById(R.id.select_permit_button);
        }
    }
}
