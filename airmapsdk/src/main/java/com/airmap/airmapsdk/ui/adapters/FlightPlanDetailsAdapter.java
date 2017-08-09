package com.airmap.airmapsdk.ui.adapters;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.airmap.airmapsdk.Analytics;
import com.airmap.airmapsdk.R;
import com.airmap.airmapsdk.models.AirMapBaseModel;
import com.airmap.airmapsdk.models.aircraft.AirMapAircraft;
import com.airmap.airmapsdk.models.flight.AirMapFlightFeature;
import com.airmap.airmapsdk.models.flight.AirMapFlightPlan;
import com.airmap.airmapsdk.models.flight.FlightFeatureConfiguration;
import com.airmap.airmapsdk.models.flight.FlightFeatureValue;
import com.airmap.airmapsdk.models.pilot.AirMapPilot;
import com.airmap.airmapsdk.models.rules.AirMapRule;
import com.airmap.airmapsdk.ui.activities.CreateEditAircraftActivity;
import com.airmap.airmapsdk.ui.activities.ProfileActivity;
import com.airmap.airmapsdk.ui.views.ToggleButton;
import com.airmap.airmapsdk.util.AirMapConstants;
import com.airmap.airmapsdk.util.Utils;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by collin@airmap.com on 5/22/17.
 */

public class FlightPlanDetailsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = "FlightDetailsAdapter";

    private static final int SETTINGS_VIEW_TYPE = 0;
    private static final int RULE_VIEW_TYPE = 1;
    private static final int BINARY_VIEW_TYPE = 2;
    private static final int SLIDER_VIEW_TYPE = 3;
    private static final int FIELD_VIEW_TYPE = 4;
    private static final int NEXT_BUTTON_TYPE = 5;

    private Activity activity;
    private AirMapFlightPlan flightPlan;
    private List<AirMapBaseModel> rulesAndFlightFeatures;
    private List<AirMapFlightFeature> duplicateFlightFeatures;
    private Map<String, FlightFeatureConfiguration> flightFeaturesConfigMap;
    private FlightPlanChangeListener flightPlanChangeListener;
    private AirMapPilot pilot;
    private List<AirMapAircraft> aircrafts;
    private boolean useMetric;

    public FlightPlanDetailsAdapter(Activity activity, AirMapFlightPlan flightPlan, Map<String, FlightFeatureConfiguration> flightFeaturesConfigMap, FlightPlanChangeListener flightPlanChangeListener) {
        this.activity = activity;
        this.flightPlan = flightPlan;
        this.flightFeaturesConfigMap = flightFeaturesConfigMap;
        this.flightPlanChangeListener = flightPlanChangeListener;
        this.useMetric = Utils.useMetric(activity);
    }

    public FlightPlanDetailsAdapter(Activity activity, AirMapFlightPlan flightPlan, List<AirMapBaseModel> rulesAndFlightFeatures,
                                    Map<String, FlightFeatureConfiguration> flightFeaturesConfigMap, FlightPlanChangeListener flightPlanChangeListener) {
        this.activity = activity;
        this.flightPlan = flightPlan;
        this.rulesAndFlightFeatures = rulesAndFlightFeatures;
        this.flightFeaturesConfigMap = flightFeaturesConfigMap;
        this.flightPlanChangeListener = flightPlanChangeListener;
        this.useMetric = Utils.useMetric(activity);

        stripDuplicateFlightFeatures();
    }

    public void setFlightFeatures(Map<String, FlightFeatureConfiguration> formatMap, List<AirMapBaseModel> rulesAndFlightFeatures) {
        this.flightFeaturesConfigMap = formatMap;
        this.rulesAndFlightFeatures = rulesAndFlightFeatures;
        stripDuplicateFlightFeatures();
        notifyDataSetChanged();
    }

    private void stripDuplicateFlightFeatures() {
        // not displaying altitude flight features for now
        duplicateFlightFeatures = new ArrayList<>();

        int index = 0;
        for (AirMapBaseModel ruleOrFlightFeature : new ArrayList<>(rulesAndFlightFeatures)) {
            if (ruleOrFlightFeature instanceof AirMapFlightFeature) {
                AirMapFlightFeature flightFeature = (AirMapFlightFeature) ruleOrFlightFeature;

                if (flightFeature.isAltitudeFeature()) {
                    AirMapBaseModel ruleOrFlightFeatureBefore = rulesAndFlightFeatures.get(index - 1);
                    if (ruleOrFlightFeatureBefore instanceof AirMapRule) {
                        rulesAndFlightFeatures.remove(ruleOrFlightFeatureBefore);
                    }
                    rulesAndFlightFeatures.remove(flightFeature);
                    duplicateFlightFeatures.add(flightFeature);
                }
            }
            index++;
        }
    }

    public void setPilot(AirMapPilot pilot) {
        this.pilot = pilot;
        notifyItemChanged(0);
    }

    public void setAircrafts(List<AirMapAircraft> aircrafts) {
        this.aircrafts = aircrafts;
        notifyItemChanged(0);
    }

    public void setSelectedAircraft(AirMapAircraft aircraft) {
        if (aircrafts == null) {
            aircrafts = new ArrayList<>();
        } else if (aircrafts.contains(aircraft)) {
            aircrafts.remove(aircraft);
        }

        flightPlan.setAircraftId(aircraft.getAircraftId());
        aircrafts.add(0, aircraft);
        notifyItemChanged(0);
    }

    private void onFlightPlanChanged() {
        flightPlanChangeListener.onFlightPlanChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        switch (viewType) {
            case SETTINGS_VIEW_TYPE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_flight_plan_settings, parent, false);
                return new SettingsViewHolder(view);
            case BINARY_VIEW_TYPE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_flight_plan_feature_binary, parent, false);
                return new FlightFeatureBinaryViewHolder(view);
            case SLIDER_VIEW_TYPE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_flight_plan_feature_slider, parent, false);
                return new FlightFeatureSeekbarViewHolder(view);
            case FIELD_VIEW_TYPE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_flight_plan_feature_field, parent, false);
                return new FlightFeatureFieldViewHolder(view);
            case RULE_VIEW_TYPE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_flight_plan_rule, parent, false);
                return new RuleViewHolder(view);
            case NEXT_BUTTON_TYPE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_flight_plan_next, parent, false);
                return new NextButtonViewHolder(view);
        }

        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (getItemViewType(position)) {
            case SETTINGS_VIEW_TYPE: {
                SettingsViewHolder viewHolder = (SettingsViewHolder) holder;
                viewHolder.pilotProfileTextView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(activity, ProfileActivity.class);
                        activity.startActivityForResult(intent, AirMapConstants.EDIT_PROFILE_REQUEST_CODE);
                    }
                });

                if (pilot != null) {
                    viewHolder.pilotProfileTextView.setText(String.format("%s %s", pilot.getFirstName(), pilot.getLastName()));
                }


                setupAltitudeSeekBar(viewHolder);
                setupDurationSeekBar(viewHolder);
                setupFlightDateTimePicker(viewHolder);
                setupAircraftView(viewHolder);
                break;
            }
            case RULE_VIEW_TYPE: {
                AirMapRule rule = (AirMapRule) getItem(position);
                RuleViewHolder ruleViewHolder = (RuleViewHolder) holder;
                ruleViewHolder.descriptionTextView.setText(rule.getShortText());
                ruleViewHolder.statusImageView.setImageResource(R.drawable.ic_asterisk);
                ruleViewHolder.statusImageView.setVisibility(showStatusIcon(rule) ? View.VISIBLE : View.INVISIBLE);

                break;
            }
            case SLIDER_VIEW_TYPE: {
                final AirMapFlightFeature flightFeature = (AirMapFlightFeature) getItem(position);
                final FlightFeatureConfiguration config = flightFeaturesConfigMap.get(flightFeature.getFlightFeature());
                final FlightFeatureConfiguration.ValueConfiguration valueConfig = config.getValueConfig(useMetric);
                final List<Double> presets = valueConfig.getPresets();

                FlightFeatureValue<Double> savedValue = flightPlan.getFlightFeatureValues() != null ? flightPlan.getFlightFeatureValues().get(flightFeature.getFlightFeature()) : null;

                final FlightFeatureSeekbarViewHolder seekbarViewHolder = (FlightFeatureSeekbarViewHolder) holder;
                seekbarViewHolder.descriptionTextView.setText(flightFeature.getDescription());
                seekbarViewHolder.labelTextView.setText(flightFeature.getMeasurementType().getStringRes());

                seekbarViewHolder.seekBar.setOnSeekBarChangeListener(null);
                seekbarViewHolder.seekBar.setMax(presets.size());
                seekbarViewHolder.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        FlightFeatureValue<Double> value;
                        if (progress == 0) {
                            value = new FlightFeatureValue<>(flightFeature.getFlightFeature(), null);
                            seekbarViewHolder.valueTextView.setText(R.string.null_feature_value);
                            flightPlanChangeListener.onFlightFeatureRemoved(flightFeature.getFlightFeature());
                        } else {
                            double adjustedValue = presets.get(progress - 1);
                            value = new FlightFeatureValue<>(flightFeature.getFlightFeature(), adjustedValue * valueConfig.getConversionFactor());
                            seekbarViewHolder.valueTextView.setText(getMeasurementWithUnits(adjustedValue, config));
                        }

                        if (fromUser) {
                            flightPlan.setFlightFeatureValue(value);
                            updateRuleOfFlightFeature(flightFeature);
                        }
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {}

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        onFlightPlanChanged();
                    }
                });

                int progress = 0;
                String text = holder.itemView.getContext().getString(R.string.null_feature_value);
                if (savedValue != null) {
                    double measurement = savedValue.getValue() / valueConfig.getConversionFactor();
                    progress = Utils.indexOfNearestMatch(measurement, presets) + 1;
                    text = getMeasurementWithUnits(presets.get(progress - 1), config);
                }
                seekbarViewHolder.valueTextView.setText(text);
                seekbarViewHolder.seekBar.setProgress(progress);

                break;
            }
            case BINARY_VIEW_TYPE: {
                final AirMapFlightFeature flightFeature = (AirMapFlightFeature) getItem(position);
                FlightFeatureValue<Boolean> savedValue = flightPlan.getFlightFeatureValues() != null ? flightPlan.getFlightFeatureValues().get(flightFeature.getFlightFeature()) : null;

                final FlightFeatureBinaryViewHolder binaryViewHolder = (FlightFeatureBinaryViewHolder) holder;
                binaryViewHolder.descriptionTextView.setText(flightFeature.getDescription());

                boolean noSelected = savedValue != null && !savedValue.getValue();
                binaryViewHolder.noButton.setSelected(noSelected);
                binaryViewHolder.noButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        FlightFeatureValue featureValue = new FlightFeatureValue<>(flightFeature.getFlightFeature(), false);
                        flightPlan.setFlightFeatureValue(featureValue);
                        onFlightPlanChanged();
                        updateRuleOfFlightFeature(flightFeature);
                    }
                });

                boolean yesSelected = savedValue != null && ((Boolean) savedValue.getValue());
                binaryViewHolder.yesButton.setSelected(yesSelected);
                binaryViewHolder.yesButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        FlightFeatureValue featureValue = new FlightFeatureValue<>(flightFeature.getFlightFeature(), true);
                        flightPlan.setFlightFeatureValue(featureValue);
                        onFlightPlanChanged();
                        updateRuleOfFlightFeature(flightFeature);
                    }
                });

                break;
            }
            case NEXT_BUTTON_TYPE: {
                NextButtonViewHolder nextButtonViewHolder = (NextButtonViewHolder) holder;
                nextButtonViewHolder.nextButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        flightPlanChangeListener.onFlightPlanSubmit();
                    }
                });
                break;
            }
        }
    }

    private void updateRuleOfFlightFeature(AirMapFlightFeature flightFeature) {
        // update the status icon for this feature's rule
        int index = 1;
        for (AirMapBaseModel ruleOrFeature : rulesAndFlightFeatures) {
            if (ruleOrFeature instanceof AirMapRule) {
                AirMapRule rule = (AirMapRule) ruleOrFeature;
                if (rule.getFlightFeatures().contains(flightFeature)) {
                    notifyItemChanged(index);
                    break;
                }
            }
            index++;
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return SETTINGS_VIEW_TYPE;
        }

        if (position == getItemCount() - 1) {
            return NEXT_BUTTON_TYPE;
        }

        AirMapBaseModel item = getItem(position);
        if (item instanceof AirMapFlightFeature) {
            switch (((AirMapFlightFeature) item).getInputType()) {
                case Double:
                    return SLIDER_VIEW_TYPE;
                case Boolean:
                default:
                    return BINARY_VIEW_TYPE;
            }
        } else {
            return RULE_VIEW_TYPE;
        }
    }

    private boolean showStatusIcon(AirMapRule rule) {
        for (AirMapFlightFeature flightFeature : rule.getFlightFeatures()) {
            if (flightPlan.getFlightFeatureValues() == null || !flightPlan.getFlightFeatureValues().containsKey(flightFeature.getFlightFeature())) {
                return true;
            }
        }

        return false;
    }

    private String getMeasurementWithUnits(double value, FlightFeatureConfiguration flightFeatureConfig) {
        String units = flightFeatureConfig.getValueConfig(useMetric).getUnit();
        //TODO: i18n support?
        if (value == (long) value) {
            return String.format("%d %s", (int) value, units);
        } else if (value % .5 == 0) {
            return String.format("%.1f %s", value, units);
        } else {
            return String.format("%.2f %s", value, units);
        }
    }

    public AirMapBaseModel getItem(int position) {
        return rulesAndFlightFeatures.get(position - 1);
    }

    @Override
    public int getItemCount() {
        return rulesAndFlightFeatures != null ? rulesAndFlightFeatures.size() + 2 : 2;
    }

    private void setupAltitudeSeekBar(final SettingsViewHolder holder) {
        final FlightFeatureConfiguration config = flightFeaturesConfigMap.get("altitude");
        final FlightFeatureConfiguration.ValueConfiguration valueConfig = config.getValueConfig(useMetric);
        final List<Double> altitudes = valueConfig.getPresets();
        holder.altitudeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                double altitude = altitudes.get(progress);
                holder.altitudeValueTextView.setText(getMeasurementWithUnits(altitude, config));

                if (fromUser) {
                    float altitudeInMeters = (float) (altitude * valueConfig.getConversionFactor());
                    flightPlan.setMaxAltitude(altitudeInMeters);
                    for (AirMapFlightFeature flightFeature : duplicateFlightFeatures) {
                        if (flightFeature.isAltitudeFeature()) {
                            flightPlan.setFlightFeatureValue(new FlightFeatureValue<>(flightFeature.getFlightFeature(), altitudeInMeters));
                        }
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                onFlightPlanChanged();
            }
        });

        // flight plan defaults to max altitude if not set
        if (flightPlan.getMaxAltitude() == 0) {
            flightPlan.setMaxAltitude((float) (valueConfig.getDefaultValue() * valueConfig.getConversionFactor()));
            onFlightPlanChanged();
        }

        int progress = Utils.indexOfNearestMatch(flightPlan.getMaxAltitude() / valueConfig.getConversionFactor(), altitudes);
        holder.altitudeSeekBar.setMax(altitudes.size() - 1);
        for (AirMapFlightFeature flightFeature : duplicateFlightFeatures) {
            if (flightFeature.isAltitudeFeature()) {
                flightPlan.setFlightFeatureValue(new FlightFeatureValue<>(flightFeature.getFlightFeature(), flightPlan.getMaxAltitude()));
            }
        }
        holder.altitudeSeekBar.setProgress(progress);
    }

    private void setupDurationSeekBar(final SettingsViewHolder holder) {
        final FlightFeatureConfiguration config = flightFeaturesConfigMap.get("duration");
        final FlightFeatureConfiguration.ValueConfiguration valueConfig = config.getImperialValueConfig();
        final List<Double> durationsInMinutes = valueConfig.getPresets();

        holder.durationSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int durationInMillis = (int) (durationsInMinutes.get(progress) * 60 * 1000);
                holder.durationValueTextView.setText(Utils.getDurationText(activity, durationInMillis));

                if (fromUser) {
                    flightPlan.setDurationInMillis(durationInMillis);
                    long startTime = flightPlan.getStartsAt() != null ? flightPlan.getStartsAt().getTime() : new Date().getTime();
                    flightPlan.setEndsAt(new Date(startTime + durationInMillis));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                onFlightPlanChanged();
            }
        });

        if (flightPlan.getEndsAt() == null) {
            long defaultDuration = (long) (valueConfig.getDefaultValue() * 60 * 1000);
            flightPlan.setDurationInMillis(defaultDuration);
            flightPlan.setEndsAt(new Date(System.currentTimeMillis() + defaultDuration));
            onFlightPlanChanged();
        }
        long startTime = flightPlan.getStartsAt() != null ? flightPlan.getStartsAt().getTime() : new Date().getTime();
        double durationInMins = (flightPlan.getEndsAt().getTime() - startTime) / (60 * 1000);
        int progress = Utils.indexOfNearestMatch(durationInMins, durationsInMinutes);

        holder.durationSeekBar.setMax(durationsInMinutes.size() - 1);
        holder.durationSeekBar.setProgress(progress);
    }

    private void setupFlightDateTimePicker(final SettingsViewHolder holder) {
        final FlightFeatureConfiguration config = flightFeaturesConfigMap.get("duration");
        final FlightFeatureConfiguration.ValueConfiguration valueConfig = config.getImperialValueConfig();
        final List<Double> durationsInMinutes = valueConfig.getPresets();

        final Calendar flightDate = Calendar.getInstance();
        flightDate.setTime(flightPlan.getStartsAt() == null ? new Date() : flightPlan.getStartsAt());

        DateFormat format = Utils.getDateTimeFormat();
        holder.startsAtTextView.setText(flightPlan.getStartsAt() == null ?
                activity.getString(R.string.now) : format.format(flightPlan.getStartsAt()));

        holder.startsAtTouchTarget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Use the currently selected time as the default values for the picker
                final int nowHour = flightDate.get(Calendar.HOUR_OF_DAY);
                final int nowMinute = flightDate.get(Calendar.MINUTE);
                final int nowYear = flightDate.get(Calendar.YEAR);
                final int nowMonth = flightDate.get(Calendar.MONTH);
                final int nowDay = flightDate.get(Calendar.DAY_OF_MONTH);
                DatePickerDialog dialog = new DatePickerDialog(activity, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, final int year, final int monthOfYear, final int dayOfMonth) {
                        new TimePickerDialog(activity, new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                flightDate.set(year, monthOfYear, dayOfMonth, hourOfDay, minute);
                                flightDate.set(Calendar.SECOND, 0);
                                flightDate.set(Calendar.MILLISECOND, 0);

                                // don't allow the user to pick a time in the past
                                if (flightDate.getTime().before(new Date())) {
                                    if (activity!= null) {
                                        Toast.makeText(activity, R.string.only_schedule_present_future, Toast.LENGTH_SHORT).show();
                                    }
                                    flightDate.setTime(new Date());
                                }

                                flightPlan.setStartsAt(flightDate.getTime());
                                long durationInMillis = (long) (durationsInMinutes.get(holder.durationSeekBar.getProgress()) * valueConfig.getConversionFactor());
                                Date correctedEndTime = new Date(flightDate.getTime().getTime() + durationInMillis);
                                flightPlan.setEndsAt(correctedEndTime);

                                DateFormat format = Utils.getDateTimeFormat();
                                holder.startsAtTextView.setText(flightPlan.getStartsAt() == null ?
                                        activity.getString(R.string.now) : format.format(flightPlan.getStartsAt()));

                                onFlightPlanChanged();
                            }
                        }, nowHour, nowMinute, false).show();
                    }
                }, nowYear, nowMonth, nowDay);
                Date now = new Date();
                long sevenDays = 1000 * 60 * 60 * 24 * 7;
                dialog.getDatePicker().setMinDate(now.getTime() - 10000); //Subtract a second because of a crash on older devices/api levels
                dialog.getDatePicker().setMaxDate(now.getTime() + sevenDays);
                dialog.show();

                Analytics.logEvent(Analytics.Page.DETAILS_CREATE_FLIGHT, Analytics.Action.tap, Analytics.Label.FLIGHT_START_TIME);
            }
        });
    }

    private void setupAircraftView(final SettingsViewHolder holder) {
        if (!TextUtils.isEmpty(flightPlan.getAircraftId())) {
            for (AirMapAircraft aircraft : aircrafts) {
                if (aircraft.getAircraftId().equals(flightPlan.getAircraftId())) {
                    holder.aircraftTextView.setText(aircraft.getNickname());
                    break;
                }
            }
        }

        holder.aircraftTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Analytics.logEvent(Analytics.Page.DETAILS_CREATE_FLIGHT, Analytics.Action.tap, Analytics.Label.SELECT_AIRCRAFT);

                if (aircrafts == null || aircrafts.isEmpty()) {
                    Analytics.logEvent(Analytics.Page.SELECT_AIRCRAFT, Analytics.Action.tap, Analytics.Label.NEW_AIRCRAFT);

                    Intent intent = new Intent(activity, CreateEditAircraftActivity.class);
                    activity.startActivityForResult(intent, AirMapConstants.CREATE_AIRCRAFT_REQUEST_CODE);
                } else {
                    new AlertDialog.Builder(activity)
                            .setTitle(R.string.select_aircraft)
                            .setAdapter(new AircraftAdapter(activity, aircrafts), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int position) {
                                    if (aircrafts.get(position).getAircraftId().equals("add_aircraft")) {
                                        Analytics.logEvent(Analytics.Page.SELECT_AIRCRAFT, Analytics.Action.tap, Analytics.Label.NEW_AIRCRAFT);

                                        Intent intent = new Intent(activity, CreateEditAircraftActivity.class);
                                        activity.startActivityForResult(intent, AirMapConstants.CREATE_AIRCRAFT_REQUEST_CODE);
                                    } else {
                                        holder.aircraftTextView.setText(aircrafts.get(position).getNickname());
                                        flightPlan.setAircraftId(aircrafts.get(position).getAircraftId());
                                        onFlightPlanChanged();
                                    }
                                    dialogInterface.dismiss();
                                }
                            })
                            .setNeutralButton(R.string.create_aircraft, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Analytics.logEvent(Analytics.Page.SELECT_AIRCRAFT, Analytics.Action.tap, Analytics.Label.NEW_AIRCRAFT);

                                    Intent intent = new Intent(activity, CreateEditAircraftActivity.class);
                                    activity.startActivityForResult(intent, AirMapConstants.CREATE_AIRCRAFT_REQUEST_CODE);
                                }
                            })
                            .show();
                }
            }
        });
    }

    private class SettingsViewHolder extends RecyclerView.ViewHolder {
        View altitudeView;
        TextView altitudeValueTextView;
        SeekBar altitudeSeekBar;
        View startsAtTouchTarget;
        TextView startsAtTextView;
        TextView durationValueTextView;
        SeekBar durationSeekBar;
        TextView pilotProfileTextView;
        TextView aircraftTextView;

        SettingsViewHolder(View itemView) {
            super(itemView);
            altitudeView = itemView.findViewById(R.id.altitude_view);
            altitudeValueTextView = (TextView) itemView.findViewById(R.id.altitude_value);
            altitudeSeekBar = (SeekBar) itemView.findViewById(R.id.altitude_seekbar);
            startsAtTouchTarget = itemView.findViewById(R.id.date_time_picker_touch_target);
            startsAtTextView = (TextView) itemView.findViewById(R.id.time_value);
            durationValueTextView = (TextView) itemView.findViewById(R.id.duration_value);
            durationSeekBar = (SeekBar) itemView.findViewById(R.id.duration_seekbar);
            pilotProfileTextView = (TextView) itemView.findViewById(R.id.pilot_profile_text);
            aircraftTextView = (TextView) itemView.findViewById(R.id.aircraft_label);
        }
    }

    private class RuleViewHolder extends RecyclerView.ViewHolder {
        TextView descriptionTextView;
        ImageView statusImageView;

        RuleViewHolder(View itemView) {
            super(itemView);

            descriptionTextView = (TextView) itemView.findViewById(R.id.description_text_view);
            statusImageView = (ImageView) itemView.findViewById(R.id.status_badge_image_view);
        }
    }

    private class FlightFeatureBinaryViewHolder extends RecyclerView.ViewHolder {
        TextView descriptionTextView;
        ToggleButton noButton;
        ToggleButton yesButton;

        FlightFeatureBinaryViewHolder(View itemView) {
            super(itemView);

            descriptionTextView = (TextView) itemView.findViewById(R.id.description_text_view);
            noButton = (ToggleButton) itemView.findViewById(R.id.no_button);
            yesButton = (ToggleButton) itemView.findViewById(R.id.yes_button);
        }
    }

    private class FlightFeatureSeekbarViewHolder extends RecyclerView.ViewHolder {
        TextView descriptionTextView;
        TextView labelTextView;
        TextView valueTextView;
        SeekBar seekBar;

        FlightFeatureSeekbarViewHolder(View itemView) {
            super(itemView);

            descriptionTextView = (TextView) itemView.findViewById(R.id.description_text_view);
            labelTextView = (TextView) itemView.findViewById(R.id.label_text_view);
            valueTextView = (TextView) itemView.findViewById(R.id.value_text_view);
            seekBar = (SeekBar) itemView.findViewById(R.id.seekbar);
        }
    }

    private class FlightFeatureFieldViewHolder extends RecyclerView.ViewHolder {
        TextView descriptionTextView;
        EditText editText;

        FlightFeatureFieldViewHolder(View itemView) {
            super(itemView);
            descriptionTextView = (TextView) itemView.findViewById(R.id.description_text_view);
            editText = (EditText) itemView.findViewById(R.id.edit_text);
        }
    }

    private class NextButtonViewHolder extends RecyclerView.ViewHolder {
        Button nextButton;

        NextButtonViewHolder(View itemView) {
            super(itemView);
            nextButton = (Button) itemView.findViewById(R.id.next_button);
        }
    }

    public interface FlightPlanChangeListener {
        void onFlightPlanChanged();
        void onFlightFeatureRemoved(String flightFeature);
        void onFlightPlanSubmit();
    }
}
