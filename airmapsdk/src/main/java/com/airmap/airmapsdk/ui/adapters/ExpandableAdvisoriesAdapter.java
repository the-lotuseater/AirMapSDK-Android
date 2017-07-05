package com.airmap.airmapsdk.ui.adapters;

import android.content.Context;
import android.graphics.PorterDuff;
import android.support.annotation.ColorRes;
import android.support.annotation.IntegerRes;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.airmap.airmapsdk.R;
import com.airmap.airmapsdk.models.status.AirMapAdvisory;
import com.airmap.airmapsdk.models.status.AirMapStatus;
import com.airmap.airmapsdk.networking.services.MappingService;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by collin@airmap.com on 5/30/17.
 */

public class ExpandableAdvisoriesAdapter extends ExpandableRecyclerAdapter<MappingService.AirMapAirspaceType, AirMapAdvisory> {

    public ExpandableAdvisoriesAdapter(LinkedHashMap<MappingService.AirMapAirspaceType, List<AirMapAdvisory>> advisories) {
        super(advisories);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case PARENT_VIEW_TYPE: {
                return new AirspaceTypeViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_advisory_type, parent, false));
            }
            case CHILD_VIEW_TYPE: {
                return new AdvisoryViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_advisory, parent, false));
            }
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);

        if (holder instanceof AirspaceTypeViewHolder) {
            MappingService.AirMapAirspaceType type = (MappingService.AirMapAirspaceType) getItem(position);
            Context context = holder.itemView.getContext();
            String typeText = context.getString(type.getTitle());
            String typeAndQuantityText = context.getString(R.string.advisory_type_quantity, typeText, dataMap.get(type).size());
            AirMapStatus.StatusColor color = calculateStatusColor(type);

            ((AirspaceTypeViewHolder) holder).backgroundView.setBackgroundColor(ContextCompat.getColor(context, color.getColorRes()));
            ((AirspaceTypeViewHolder) holder).textView.setText(typeAndQuantityText);
            ((AirspaceTypeViewHolder) holder).textView.setTextColor(ContextCompat.getColor(context, getTextColor(color)));
            ((AirspaceTypeViewHolder) holder).expandImageView.setImageResource(isExpanded(type) ? R.drawable.ic_drop_down_up : R.drawable.ic_drop_down);
            ((AirspaceTypeViewHolder) holder).expandImageView.setColorFilter(ContextCompat.getColor(context, getTextColor(color)), PorterDuff.Mode.SRC_ATOP);

        } else if (holder instanceof AdvisoryViewHolder) {
            AirMapAdvisory advisory = (AirMapAdvisory) getItem(position);
            ((AdvisoryViewHolder) holder).backgroundView.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), advisory.getColor().getColorRes()));
            ((AdvisoryViewHolder) holder).titleTextView.setText(advisory.getName());
        }
    }


    @Override
    protected void toggleExpandingViewHolder(final RecyclerView.ViewHolder holder, final boolean expanded) {
        ((AirspaceTypeViewHolder) holder).expandImageView.setImageResource(expanded ? R.drawable.ic_drop_down_up : R.drawable.ic_drop_down);
    }

    private AirMapStatus.StatusColor calculateStatusColor(MappingService.AirMapAirspaceType type) {
        AirMapStatus.StatusColor color = AirMapStatus.StatusColor.Green;
        for (AirMapAdvisory advisory : dataMap.get(type)) {
            if (advisory.getColor() == AirMapStatus.StatusColor.Red) {
                color = AirMapStatus.StatusColor.Red;
                break;
            } else if (advisory.getColor() == AirMapStatus.StatusColor.Orange) {
                color = AirMapStatus.StatusColor.Orange;
            } else if (advisory.getColor() == AirMapStatus.StatusColor.Yellow && color == AirMapStatus.StatusColor.Green) {
                color = AirMapStatus.StatusColor.Yellow;
            }
        }

        return color;
    }

    @ColorRes
    private int getTextColor(AirMapStatus.StatusColor statusColor) {
        if (statusColor == AirMapStatus.StatusColor.Yellow) {
            return R.color.font_black;
        }

        return R.color.font_white;
    }

    private class AirspaceTypeViewHolder extends RecyclerView.ViewHolder {
        View backgroundView;
        TextView textView;
        ImageView expandImageView;

        AirspaceTypeViewHolder(View itemView) {
            super(itemView);

            backgroundView = itemView.findViewById(R.id.background_view);
            textView = (TextView) itemView.findViewById(R.id.title_text_view);
            expandImageView = (ImageView) itemView.findViewById(R.id.expand_image_view);
        }
    }

    private class AdvisoryViewHolder extends RecyclerView.ViewHolder {
        View backgroundView;
        TextView titleTextView;

        AdvisoryViewHolder(View itemView) {
            super(itemView);

            backgroundView = itemView.findViewById(R.id.background_view);
            titleTextView = (TextView) itemView.findViewById(R.id.title_text_view);
        }
    }
}
