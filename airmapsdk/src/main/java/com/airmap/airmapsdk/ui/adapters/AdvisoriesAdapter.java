package com.airmap.airmapsdk.ui.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.airmap.airmapsdk.Analytics;
import com.airmap.airmapsdk.R;
import com.airmap.airmapsdk.models.status.AirMapAdvisory;
import com.airmap.airmapsdk.models.status.AirMapStatus;
import com.airmap.airmapsdk.networking.services.MappingService;
import com.airmap.airmapsdk.util.Utils;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Created by Vansh Gandhi on 11/8/16.
 * Copyright © 2016 AirMap, Inc. All rights reserved.
 */

public class AdvisoriesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements EmptyableAdapter {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_NORMAL = 1;
    private static final int TYPE_TFR = 2;
    private static final int TYPE_WILDFIRE = 3;
    private static final int TYPE_EMERGENCY = 4;

    private static final String HEADER_STRING = "header";

    private final String RED_TITLE;
    private final String YELLOW_TITLE;
    private final String GREEN_TITLE;

    private Context context;
    private List<AirMapAdvisory> advisories = new ArrayList<>();
    private DateFormat dateFormat;


    public AdvisoriesAdapter(Context context, List<AirMapAdvisory> data) {
        this.context = context;
        this.dateFormat = Utils.getDateTimeFormat();

        sort();

        RED_TITLE = context.getString(R.string.flight_strictly_regulated);
        YELLOW_TITLE = context.getString(R.string.advisories);
        GREEN_TITLE = context.getString(R.string.informational);

//        if (data.containsKey(RED_TITLE)) {
//            AirMapAdvisory header = new AirMapAdvisory();
//            header.setName(RED_TITLE);
//            header.setId(HEADER_STRING); //Hack to have only a list of Advisories, but let it know it's a header
//            advisories.add(header);
//            for (AirMapAdvisory advisory : data.get(RED_TITLE)) {
//                advisories.add(advisory);
//            }
//        }
//
//        if (data.containsKey(YELLOW_TITLE)) {
//            AirMapAdvisory header = new AirMapAdvisory();
//            header.setName(YELLOW_TITLE);
//            header.setId(HEADER_STRING); //Hack to have only a list of Advisories, but let it know it's a header
//            advisories.add(header);
//            for (AirMapAdvisory advisory : data.get(YELLOW_TITLE)) {
//                advisories.add(advisory);
//            }
//        }
//
//        if (data.containsKey(GREEN_TITLE)) {
//            AirMapAdvisory header = new AirMapAdvisory();
//            header.setName(GREEN_TITLE);
//            header.setId(HEADER_STRING); //Hack to have only a list of Advisories, but let it know it's a header
//            advisories.add(header);
//            for (AirMapAdvisory advisory : data.get(GREEN_TITLE)) {
//                advisories.add(advisory);
//            }
//        }
    }

    public void setData(List<AirMapAdvisory> data) {
        advisories = data;
        sort();

//        if (data.containsKey(RED_TITLE)) {
//            AirMapAdvisory header = new AirMapAdvisory();
//            header.setName(RED_TITLE);
//            header.setId(HEADER_STRING); //Hack to have only a list of Advisories, but let it know it's a header
//            advisories.add(header);
//            for (AirMapAdvisory advisory : data.get(RED_TITLE)) {
//                advisories.add(advisory);
//            }
//        }
//
//        if (data.containsKey(YELLOW_TITLE)) {
//            AirMapAdvisory header = new AirMapAdvisory();
//            header.setName(YELLOW_TITLE);
//            header.setId(HEADER_STRING); //Hack to have only a list of Advisories, but let it know it's a header
//            advisories.add(header);
//            for (AirMapAdvisory advisory : data.get(YELLOW_TITLE)) {
//                advisories.add(advisory);
//            }
//        }
//
//        if (data.containsKey(GREEN_TITLE)) {
//            AirMapAdvisory header = new AirMapAdvisory();
//            header.setName(GREEN_TITLE);
//            header.setId(HEADER_STRING); //Hack to have only a list of Advisories, but let it know it's a header
//            advisories.add(header);
//            for (AirMapAdvisory advisory : data.get(GREEN_TITLE)) {
//                advisories.add(advisory);
//            }
//        }

        notifyDataSetChanged();
    }

    private void sort() {
        Collections.sort(advisories, new Comparator<AirMapAdvisory>() {
            @Override
            public int compare(AirMapAdvisory o1, AirMapAdvisory o2) {
                if (o1.getColor().intValue() > o2.getColor().intValue()) {
                    return -1;
                } else if (o1.getColor().intValue() < o2.getColor().intValue()) {
                    return 1;
                } else {
                    return 0;
                }
            }
        });
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.bottom_sheet_header_item, parent, false);
            return new VHHeader(view);
        } else if (viewType == TYPE_NORMAL) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.bottom_sheet_list_item, parent, false);
            return new VHItem(view);
        } else if (viewType == TYPE_TFR) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.bottom_sheet_tfr_item, parent, false);
            return new VHTfr(view);
        } else if (viewType == TYPE_WILDFIRE) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.bottom_sheet_wildfire_item, parent, false);
            return new VHWildfire(view);
        } else if (viewType == TYPE_EMERGENCY) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.bottom_sheet_list_item, parent, false);
            return new VHEmergency(view);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        AirMapAdvisory advisory = getItem(position);
        if (holder instanceof VHHeader) {
            onBindHeaderViewHolder((VHHeader) holder, advisory);
        } else if (holder instanceof VHItem) {
            onBindItemViewHolder((VHItem) holder, advisory);
        } else if (holder instanceof VHTfr) {
            onBindTfrViewHolder((VHTfr) holder, advisory);
        } else if (holder instanceof VHWildfire) {
            onBindWildfireViewHolder((VHWildfire) holder, advisory);
        } else if (holder instanceof VHEmergency) {
            onBindEmergencyViewHolder((VHEmergency) holder, advisory);
        }
    }

    private void onBindHeaderViewHolder(VHHeader holder, AirMapAdvisory advisory) {
        holder.headerTextView.setText(advisory.getName().toUpperCase());
    }

    private void onBindItemViewHolder(final VHItem holder, final AirMapAdvisory advisory) {
        holder.nameTextView.setText(advisory.getName());
        holder.description1TextView.setText(advisory.getType().getTitle());
        holder.description1TextView.setVisibility(View.VISIBLE);
        holder.description2TextView.setVisibility(View.GONE);

        try {
            final String number = "";
//            final String number = advisory.getRequirements() != null && advisory.getRequirements().getNotice() != null ?
//                    advisory.getRequirements().getNotice().getPhoneNumber() : advisory.getAirportProperties().getPhone();

            if (number != null && number.length() >= 10) {
                holder.description2TextView.setText(formatPhoneNumber(number));
                holder.description2TextView.setVisibility(View.VISIBLE);
                holder.description2TextView.setTextColor(ContextCompat.getColor(context, R.color.colorLinkBlue));
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new AlertDialog.Builder(holder.itemView.getContext())
                                .setTitle(advisory.getName())
                                .setMessage(context.getString(R.string.do_you_want_to_call, advisory.getName()))
                                .setPositiveButton(R.string.call, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + number));
                                        final Context context = holder.description2TextView.getContext();
                                        if (intent.resolveActivity(context.getPackageManager()) != null) {
                                            context.startActivity(intent); //Only start activity if the device has a phone (e.g. A tablet might not)
                                        } else {
                                            holder.description2TextView.post(new Runnable() {
                                                @Override
                                                public void run() {
                                                    Toast.makeText(context, R.string.no_dialer_found, Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        }
                                    }
                                })
                                .setNegativeButton(android.R.string.cancel, null)
                                .show();
                    }
                });
            } else {
                holder.itemView.setOnClickListener(null);
            }

//            if (advisory.getRequirements() != null && advisory.getRequirements().getNotice() != null && advisory.getRequirements().getNotice().isDigital()) {
//                holder.description2TextView.setText(R.string.accepts_digital_notice);
//                holder.description2TextView.setVisibility(View.VISIBLE);
//                holder.description2TextView.setTextColor(ContextCompat.getColor(context, R.color.colorLinkBlue));
//            }
        } catch (NullPointerException e) {
            holder.itemView.setOnClickListener(null);
        }

        holder.colorView.setBackgroundColor(getColor(advisory.getColor()));
    }

    private void onBindTfrViewHolder(final VHTfr holder, final AirMapAdvisory advisory) {
        holder.colorView.setBackgroundColor(getColor(advisory.getColor()));
        holder.nameTextView.setText(advisory.getName());
        StringBuilder builder = new StringBuilder();
//        if (advisory.getTfrProperties().getStartTime() != null) {
//            builder.append(dateFormat.format(advisory.getTfrProperties().getStartTime()));
//        }
//        if (advisory.getTfrProperties().getEndTime() != null) {
//            if (builder.length() != 0) { //Only add dash if we added a start time
//                builder.append(" - ");
//            }
//            builder.append(dateFormat.format(advisory.getTfrProperties().getEndTime()));
//        }
        holder.dateTextView.setText(builder.toString());
        String url = null;
//        String url = advisory.getTfrProperties().getUrl();
        if (url != null && !url.isEmpty()) {
            holder.urlTextView.setText(url);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Analytics.logEvent(Analytics.Page.ADVISORIES, Analytics.Action.tap, Analytics.Label.TFR_DETAILS);

//                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(advisory.getTfrProperties().getUrl()));
//                    holder.itemView.getContext().startActivity(intent);
                }
            });
            holder.urlTextView.setVisibility(View.VISIBLE);
        } else {
            holder.itemView.setOnClickListener(null);
            holder.urlTextView.setText(null);
            holder.urlTextView.setVisibility(View.GONE);
        }

    }

    private void onBindWildfireViewHolder(VHWildfire holder, AirMapAdvisory advisory) {
//        int size = advisory.getWildfireProperties().getSize();
        holder.colorView.setBackgroundColor(getColor(advisory.getColor()));
        holder.nameTextView.setText(advisory.getType() == MappingService.AirMapAirspaceType.Wildfires ? R.string.airspace_type_wildfire : R.string.airspace_type_fire);
        String unknownSize = holder.itemView.getContext().getString(R.string.unknown_size);
//        String dateEffective = Utils.getDateTimeFormat().format(advisory.getWildfireProperties().getEffectiveDate());
//        holder.sizeTextView.setText(dateEffective + " - " + (size == -1 ? unknownSize : String.format(Locale.US, "%d acres", size)));
    }

    private void onBindEmergencyViewHolder(VHEmergency holder, AirMapAdvisory advisory) {
        holder.colorView.setBackgroundColor(getColor(advisory.getColor()));
        holder.nameTextView.setText(R.string.airspace_type_emergency);

        String city = advisory.getCity() + ", " + (TextUtils.isEmpty(advisory.getState()) ? advisory.getCountry() : advisory.getState());
        holder.description1TextView.setText(TextUtils.isEmpty(advisory.getCity()) ? advisory.getName() : city);
        holder.description1TextView.setVisibility(View.VISIBLE);
    }

    private String formatPhoneNumber(String number) {
        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
        Locale locale = Locale.getDefault();
        String country = locale != null && locale.getCountry() != null && !TextUtils.isEmpty(locale.getCountry()) ? locale.getCountry() : "US";
        try {
            Phonenumber.PhoneNumber phoneNumber = phoneUtil.parse(number, country);
            return phoneUtil.format(phoneNumber, PhoneNumberUtil.PhoneNumberFormat.NATIONAL);
        } catch (NumberParseException e) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                return PhoneNumberUtils.formatNumber(number, country);
            }
            return PhoneNumberUtils.formatNumber(number);
        }
    }

    private AirMapAdvisory getItem(int position) {
        return advisories.get(position);
    }

    @Override
    public int getItemCount() {
        return advisories.size();
    }

    @Override
    public int getItemViewType(int position) {
        AirMapAdvisory advisory = getItem(position);
        if (advisory.getId().equals(HEADER_STRING)) {
            return TYPE_HEADER;
        } else {
            switch (advisory.getType()) {
                case TFR:
                    return TYPE_TFR;
                case Wildfires:
                case Fires:
                    return TYPE_WILDFIRE;
                case Emergencies:
                    return TYPE_EMERGENCY;
                default:
                    return TYPE_NORMAL;
            }
        }
    }

    private int getColor(AirMapStatus.StatusColor statusColor) {
        switch (statusColor) {
            case Red: {
                return ContextCompat.getColor(context, R.color.red);
            }
            case Orange: {
                return ContextCompat.getColor(context, R.color.orange);
            }
            case Yellow: {
                return ContextCompat.getColor(context, R.color.yellow);
            }
            case Green:
            default: {
                return ContextCompat.getColor(context, R.color.green);
            }
        }
    }

    @Override
    public String getEmptyText() {
        return context.getString(R.string.no_advisories);
    }

    public class VHHeader extends RecyclerView.ViewHolder {
        TextView headerTextView;

        public VHHeader(View itemView) {
            super(itemView);

            headerTextView = (TextView) itemView.findViewById(R.id.header);
        }
    }

    public class VHItem extends RecyclerView.ViewHolder {
        View colorView;
        TextView nameTextView;
        TextView description1TextView;
        TextView description2TextView;

        public VHItem(View itemView) {
            super(itemView);

            colorView = itemView.findViewById(R.id.color_bar);
            nameTextView = (TextView) itemView.findViewById(R.id.name);
            description1TextView = (TextView) itemView.findViewById(R.id.organization);
            description2TextView = (TextView) itemView.findViewById(R.id.phone);
        }

    }

    public class VHTfr extends RecyclerView.ViewHolder {
        View colorView;
        TextView nameTextView;
        TextView dateTextView;
        TextView urlTextView;

        public VHTfr(View itemView) {
            super(itemView);

            colorView = itemView.findViewById(R.id.color_bar);
            nameTextView = (TextView) itemView.findViewById(R.id.name);
            dateTextView = (TextView) itemView.findViewById(R.id.date);
            urlTextView = (TextView) itemView.findViewById(R.id.url);
        }

    }

    public class VHWildfire extends RecyclerView.ViewHolder {
        View colorView;
        TextView nameTextView;
        TextView sizeTextView;

        public VHWildfire(View itemView) {
            super(itemView);

            colorView = itemView.findViewById(R.id.color_bar);
            nameTextView = (TextView) itemView.findViewById(R.id.name);
            sizeTextView = (TextView) itemView.findViewById(R.id.size);
        }

    }

    public class VHEmergency extends RecyclerView.ViewHolder {
        View colorView;
        TextView nameTextView;
        TextView description1TextView;
        TextView description2TextView;

        public VHEmergency(View itemView) {
            super(itemView);

            colorView = itemView.findViewById(R.id.color_bar);
            nameTextView = (TextView) itemView.findViewById(R.id.name);
            description1TextView = (TextView) itemView.findViewById(R.id.organization);
            description2TextView = (TextView) itemView.findViewById(R.id.phone);
        }

    }
}
