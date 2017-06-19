package com.airmap.airmapsdk.ui.adapters;

import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.airmap.airmapsdk.R;
import com.airmap.airmapsdk.models.AirMapBaseModel;
import com.airmap.airmapsdk.models.flight.AirMapFlightFeature;
import com.airmap.airmapsdk.models.flight.FlightFeatureValue;
import com.airmap.airmapsdk.models.rules.AirMapRule;
import com.airmap.airmapsdk.ui.views.ToggleButton;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by collin@airmap.com on 5/26/17.
 */

public class ExpandableRulesAdapter extends ExpandableRecyclerAdapter<AirMapRule.Status, AirMapRule> {

    public ExpandableRulesAdapter(LinkedHashMap<AirMapRule.Status, List<AirMapRule>> rulesMap) {
        super(rulesMap);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        switch (viewType) {
            case PARENT_VIEW_TYPE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_brief_section, parent, false);
                return new SectionViewHolder(view);
            case CHILD_VIEW_TYPE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_brief_rule, parent, false);
                return new RuleViewHolder(view);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);

        switch (getItemViewType(position)) {
            case PARENT_VIEW_TYPE: {
                final AirMapRule.Status status = (AirMapRule.Status) getItem(position);
                SectionViewHolder sectionViewHolder = (SectionViewHolder) holder;
                sectionViewHolder.descriptionTextView.setText(getStatusString(status));
                sectionViewHolder.statusImageView.setImageResource(getStatusIcon(status));
                sectionViewHolder.expandImageView.setImageResource(isExpanded(status) ? R.drawable.ic_drop_down_up : R.drawable.ic_drop_down);
                break;
            }
            case CHILD_VIEW_TYPE: {
                final AirMapRule rule = (AirMapRule) getItem(position);
                RuleViewHolder ruleViewHolder = (RuleViewHolder) holder;
                ruleViewHolder.descriptionTextView.setText(rule.toString());

                break;
            }
        }
    }

    @StringRes
    private int getStatusString(AirMapRule.Status status) {
        switch (status) {
            case Conflicting:
                return R.string.conflicting_rules;
            case MissingInfo:
                return R.string.missing_info_rules;
            case InformationRules:
                return R.string.informational_rules;
            case NotConflicting:
                return R.string.not_conflicting_rules;
        }
        return -1;
    }

    @DrawableRes
    private int getStatusIcon(AirMapRule.Status status) {
        switch (status) {
            case Conflicting:
                return R.drawable.ic_restricted;
            case MissingInfo:
                return R.drawable.ic_asterisk;
            case InformationRules:
                return R.drawable.ic_asterisk_yellow;
            case NotConflicting:
                return R.drawable.ic_checkmark;
        }
        return R.drawable.com_auth0_lock_ic_check_error;
    }

    private class SectionViewHolder extends RecyclerView.ViewHolder {

        TextView descriptionTextView;
        ImageView statusImageView;
        ImageView expandImageView;

        SectionViewHolder(View itemView) {
            super(itemView);

            descriptionTextView = (TextView) itemView.findViewById(R.id.description_text_view);
            statusImageView = (ImageView) itemView.findViewById(R.id.status_badge_image_view);
            expandImageView = (ImageView) itemView.findViewById(R.id.expand_image_view);
        }
    }

    private class RuleViewHolder extends RecyclerView.ViewHolder {
        TextView descriptionTextView;

        RuleViewHolder(View itemView) {
            super(itemView);

            descriptionTextView = (TextView) itemView.findViewById(R.id.description_text_view);
        }
    }
}
