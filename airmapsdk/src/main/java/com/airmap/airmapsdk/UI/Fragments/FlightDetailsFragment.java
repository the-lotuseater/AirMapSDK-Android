package com.airmap.airmapsdk.UI.Fragments;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.airmap.airmapsdk.AirMapException;
import com.airmap.airmapsdk.Models.Aircraft.AirMapAircraft;
import com.airmap.airmapsdk.Models.Flight.AirMapFlight;
import com.airmap.airmapsdk.Models.Status.AirMapStatus;
import com.airmap.airmapsdk.Models.Status.AirMapStatusAdvisory;
import com.airmap.airmapsdk.Networking.Callbacks.AirMapCallback;
import com.airmap.airmapsdk.Networking.Services.AirMap;
import com.airmap.airmapsdk.R;
import com.airmap.airmapsdk.UI.Activities.CreateEditAircraftActivity;
import com.airmap.airmapsdk.UI.Activities.CreateFlightActivity;
import com.airmap.airmapsdk.UI.Activities.ProfileActivity;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.annotations.PolygonOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.airmap.airmapsdk.Utils.getAltitudePresets;
import static com.airmap.airmapsdk.Utils.getCirclePolygon;
import static com.airmap.airmapsdk.Utils.getDurationPresets;
import static com.airmap.airmapsdk.Utils.getRadiusPresets;
import static com.airmap.airmapsdk.Utils.indexOfDurationPreset;
import static com.airmap.airmapsdk.Utils.indexOfMeterPreset;

public class FlightDetailsFragment extends Fragment implements OnMapReadyCallback {

    private static final int REQUEST_CREATE_AIRCRAFT = 1;
    private static final String addAircraftText = "Add Aircraft";
    private static final String nicknameKey = "nickname";
    private static final String modelKey = "model";
    private static final String aircraftKey = "aircraft";

    private OnFragmentInteractionListener mListener;
    private MapboxMap map;

    //Views
    private MapView mapView;
    private TextView radiusValueTextView;
    private SeekBar radiusSeekBar;
    private TextView altitudeValueTextView;
    private SeekBar altitudeSeekBar;
    private RelativeLayout startsAtTouchTarget;
    private TextView startsAtTextView;
    private TextView durationValueTextView;
    private SeekBar durationSeekBar;
    private TextView pilotProfileTextView;
    private Spinner aircraftSpinner;
    private SwitchCompat shareAirMapSwitch;
    private Button saveNextButton;
    private FrameLayout progressBarContainer;

    public FlightDetailsFragment() {
        // Required empty public constructor
    }

    public static FlightDetailsFragment newInstance() {
        return new FlightDetailsFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.airmap_fragment_flight_details, container, false);
        initializeViews(view);
        setupAircraftSpinner();
        updateStartsAtTextView();
        setupMap(savedInstanceState);
        //SeekBars are set up once the map is set up
        setupFlightDateTimePicker();
        setupOnClickListeners();
        setupSwitches();
        updateSaveNextButtonText();
        return view;
    }

    private void initializeViews(View view) {
        mapView = (MapView) view.findViewById(R.id.airmap_map);
        radiusValueTextView = (TextView) view.findViewById(R.id.radius_value);
        radiusSeekBar = (SeekBar) view.findViewById(R.id.radius_seekbar);
        altitudeValueTextView = (TextView) view.findViewById(R.id.altitude_value);
        altitudeSeekBar = (SeekBar) view.findViewById(R.id.altitude_seekbar);
        startsAtTouchTarget = (RelativeLayout) view.findViewById(R.id.date_time_picker_touch_target);
        startsAtTextView = (TextView) view.findViewById(R.id.time_value);
        durationValueTextView = (TextView) view.findViewById(R.id.duration_value);
        durationSeekBar = (SeekBar) view.findViewById(R.id.duration_seekbar);
        pilotProfileTextView = (TextView) view.findViewById(R.id.pilot_profile_text);
        aircraftSpinner = (Spinner) view.findViewById(R.id.aircraft_spinner);
        shareAirMapSwitch = (SwitchCompat) view.findViewById(R.id.share_airmap_switch);
        saveNextButton = (Button) view.findViewById(R.id.save_next_button);
        progressBarContainer = (FrameLayout) view.findViewById(R.id.progress_bar_container);
    }

    private void setupMap(Bundle savedInstanceState) {
        mapView.getMapAsync(this);
        mapView.onCreate(savedInstanceState);
    }

    @Override
    public void onMapReady(MapboxMap mapboxMap) {
        map = mapboxMap;
        LatLng position = new LatLng(mListener.getFlight().getCoordinate().getLatitude(), mListener.getFlight().getCoordinate().getLongitude());
        map.setCameraPosition(new CameraPosition.Builder().target(position).zoom(14).build());
        Icon icon = IconFactory.getInstance(getContext()).fromResource(R.drawable.airmap_flight_marker);
        map.addMarker(new MarkerOptions().position(position).icon(icon));
        mapView.post(new Runnable() {
            @Override
            public void run() {
                setupSeekBars();
            }
        });
    }

    private void setupSwitches() {
        shareAirMapSwitch.setChecked(mListener.getFlight().isPublic());
        shareAirMapSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mListener.getFlight().setPublic(isChecked);
            }
        });
    }

    private void setupOnClickListeners() {
        saveNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveNextButton.post(new Runnable() {
                    @Override
                    public void run() {
                        progressBarContainer.setVisibility(View.VISIBLE);
                    }
                });
                String text = saveNextButton.getText().toString();
                if (text.equals(getString(R.string.airmap_save))) {
                    onSaveButton();
                } else if (text.equals(getString(R.string.airmap_next))) {
                    onNextButton();
                }
            }
        });

        pilotProfileTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), ProfileActivity.class);
                if (getActivity() != null) {
                    if (getActivity().getIntent().hasExtra(CreateFlightActivity.KEY_VALUE_EXTRAS)) {
                        intent.putExtra(CreateFlightActivity.KEY_VALUE_EXTRAS, getActivity().getIntent().getSerializableExtra(CreateFlightActivity.KEY_VALUE_EXTRAS));
                    }
                }
                startActivity(intent);
            }
        });
    }

    private void setupSeekBars() {
        final int radiusIndex = indexOfMeterPreset(mListener.getFlight().getBuffer(), getRadiusPresets());
        final int altitudeIndex = indexOfMeterPreset(mListener.getFlight().getMaxAltitude(), getAltitudePresets());
        final int durationIndex = indexOfDurationPreset(mListener.getFlight().getEndsAt().getTime() - mListener.getFlight().getStartsAt().getTime());
        final int animationDuration = 250;

        int radiusAnimateTo = (int) (((float) radiusIndex / getRadiusPresets().length) * 100);
        ObjectAnimator radiusAnimator = ObjectAnimator.ofInt(radiusSeekBar, "progress", radiusAnimateTo);
        radiusAnimator.setDuration(animationDuration);
        radiusAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        radiusAnimator.start();
        radiusAnimator.addListener(new AnimationListener() {
            @Override
            public void onAnimationEnd(Animator animation) {
                radiusSeekBar.setOnSeekBarChangeListener(new SeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        if (oldPolygon != null && map != null) {
                            map.removePolygon(oldPolygon.getPolygon());
                        }
                        oldPolygon = getCirclePolygon(getRadiusPresets()[seekBar.getProgress()].value.doubleValue(), mListener.getFlight().getCoordinate());
                        if (map != null) {
                            map.addPolygon(oldPolygon);
                        }
                        radiusValueTextView.setText(getRadiusPresets()[progress].label);
                        mListener.getFlight().setBuffer(getRadiusPresets()[radiusSeekBar.getProgress()].value.doubleValue());
                    }
                });
                radiusSeekBar.setMax(getRadiusPresets().length - 1);
                radiusSeekBar.setProgress(radiusIndex);
            }
        });

        int altitudeAnimateTo = (int) (((float) altitudeIndex / getAltitudePresets().length) * 100);
        ObjectAnimator altitudeAnimator = ObjectAnimator.ofInt(altitudeSeekBar, "progress", altitudeAnimateTo);
        altitudeAnimator.setDuration(animationDuration);
        altitudeAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        altitudeAnimator.start();
        altitudeAnimator.addListener(new AnimationListener() {
            @Override
            public void onAnimationEnd(Animator animation) {
                altitudeSeekBar.setOnSeekBarChangeListener(new SeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        altitudeValueTextView.setText(getAltitudePresets()[progress].label);
                        mListener.getFlight().setMaxAltitude(getAltitudePresets()[altitudeSeekBar.getProgress()].value.doubleValue());
                    }
                });
                altitudeSeekBar.setMax(getAltitudePresets().length - 1);
                altitudeSeekBar.setProgress(altitudeIndex);
            }
        });

        int durationAnimateTo = (int) (((float) durationIndex / getDurationPresets().length) * 100);
        ObjectAnimator durationAnimator = ObjectAnimator.ofInt(durationSeekBar, "progress", durationAnimateTo);
        durationAnimator.setDuration(animationDuration);
        durationAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        durationAnimator.start();
        durationAnimator.addListener(new AnimationListener() {
            @Override
            public void onAnimationEnd(Animator animation) {
                durationSeekBar.setOnSeekBarChangeListener(new SeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        durationValueTextView.setText(getDurationPresets()[progress].label);
                        Date endsAt = new Date(mListener.getFlight().getStartsAt().getTime() + getDurationPresets()[durationSeekBar.getProgress()].value.longValue());
                        mListener.getFlight().setEndsAt(endsAt);
                    }
                });
                durationSeekBar.setMax(getDurationPresets().length - 1);
                durationSeekBar.setProgress(durationIndex);
            }
        });

    }

    private void setupFlightDateTimePicker() {
        final Calendar flightDate = Calendar.getInstance();
        flightDate.setTime(mListener.getFlight().getStartsAt() == null ? new Date() : mListener.getFlight().getStartsAt());
        updateStartsAtTextView();
        startsAtTouchTarget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Use the currently selected time as the default values for the picker
                final int nowHour = flightDate.get(Calendar.HOUR_OF_DAY);
                final int nowMinute = flightDate.get(Calendar.MINUTE);
                final int nowYear = flightDate.get(Calendar.YEAR);
                final int nowMonth = flightDate.get(Calendar.MONTH);
                final int nowDay = flightDate.get(Calendar.DAY_OF_MONTH);
                DatePickerDialog dialog = new DatePickerDialog(getContext(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, final int year, final int monthOfYear, final int dayOfMonth) {
                        new TimePickerDialog(getContext(), new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                flightDate.set(year, monthOfYear, dayOfMonth, hourOfDay, minute);
                                mListener.getFlight().setStartsAt(flightDate.getTime());
                                Date correctedEndTime = new Date(flightDate.getTime().getTime() + getDurationPresets()[durationSeekBar.getProgress()].value.longValue());
                                mListener.getFlight().setEndsAt(correctedEndTime);
                                updateStartsAtTextView();
                                mListener.flightChanged();
                            }
                        }, nowHour, nowMinute, false).show();
                        updateSaveNextButtonText();
                    }
                }, nowYear, nowMonth, nowDay);
                Date now = new Date();
                long sevenDays = 1000 * 60 * 60 * 24 * 7;
                dialog.getDatePicker().setMinDate(now.getTime() - 10000); //Subtract a second because of a crash on older devices/api levels
                dialog.getDatePicker().setMaxDate(now.getTime() + sevenDays);
                dialog.show();
            }
        });
    }

    private void updateStartsAtTextView() {
        if (mListener.getFlight().getStartsAt() == null) {
            mListener.getFlight().setStartsAt(new Date());
        }
        SimpleDateFormat format = new SimpleDateFormat("MM/dd/yy h:mm a", Locale.US);
        Date date = mListener.getFlight().getStartsAt();
        startsAtTextView.setText(format.format(date));
    }

    private void setupAircraftSpinner() {
        aircraftSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Map<String, Object> map = (Map<String, Object>) parent.getItemAtPosition(position);
                if (addAircraftText.equals(map.get("nickname"))) {
                    mListener.getFlight().setAircraft(null);
                    Intent intent = new Intent(getContext(), CreateEditAircraftActivity.class);
                    startActivityForResult(intent, REQUEST_CREATE_AIRCRAFT);
                } else {
                    mListener.getFlight().setAircraft((AirMapAircraft) map.get(aircraftKey));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        setupAircraftSpinnerAdapter();
    }

    private void setupAircraftSpinnerAdapter() {
        AirMap.getAircraft(new AirMapCallback<List<AirMapAircraft>>() {
            @Override
            public void onSuccess(final List<AirMapAircraft> response) {
                final int index = response.indexOf(mListener.getFlight().getAircraft());
                final List<Map<String, Object>> data = new ArrayList<>();
                Map<String, Object> blankValue = new HashMap<>();
                blankValue.put(nicknameKey, "Select Aircraft");
                blankValue.put(modelKey, "");
                data.add(blankValue);
                for (AirMapAircraft aircraft : response) {
                    Map<String, Object> map = new HashMap<>();
                    map.put(nicknameKey, aircraft.getNickname());
                    map.put(modelKey, aircraft.getModel().toString());
                    map.put(aircraftKey, aircraft);
                    data.add(map);
                }
                Map<String, Object> add = new HashMap<>();
                add.put(nicknameKey, addAircraftText);
                add.put(modelKey, "+");
                data.add(add);
                aircraftSpinner.post(new Runnable() {
                    @Override
                    public void run() {
                        aircraftSpinner.setAdapter(new SimpleAdapter(getContext(), data, android.R.layout.simple_list_item_2, new String[]{"nickname", "model"}, new int[]{android.R.id.text1, android.R.id.text2}));
                        aircraftSpinner.setSelection(index == -1 ? 0 : index);
                    }
                });
            }

            @Override
            public void onError(AirMapException e) {
                aircraftSpinner.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getContext(), "Error retrieving user aircraft", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void onSaveButton() {
        AirMap.createFlight(mListener.getFlight(), new AirMapCallback<AirMapFlight>() {
            @Override
            public void onSuccess(AirMapFlight response) {
                hideProgressBar();
                saveNextButton.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getContext(), "Successfully created flight!", Toast.LENGTH_SHORT).show();
                    }
                });
                mListener.flightDetailsSaveClicked(response);
            }

            @Override
            public void onError(AirMapException e) {
                hideProgressBar();
                saveNextButton.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getContext(), "Error creating flight", Toast.LENGTH_SHORT).show();
                    }
                });
                e.printStackTrace();
            }
        });
    }

    private void onNextButton() {
        AirMapFlight flight = mListener.getFlight();
        AirMap.checkCoordinate(flight.getCoordinate(), flight.getBuffer(), null, null, true, flight.getStartsAt(), new AirMapCallback<AirMapStatus>() {
            @Override
            public void onSuccess(AirMapStatus response) {
                hideProgressBar();
                mListener.flightDetailsNextClicked(response);
            }

            @Override
            public void onError(final AirMapException e) {
                hideProgressBar();
                saveNextButton.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void updateSaveNextButtonText() {
        AirMapFlight flight = mListener.getFlight();
        AirMap.checkCoordinate(flight.getCoordinate(), flight.getBuffer(), null, null, false, flight.getStartsAt(), new AirMapCallback<AirMapStatus>() {
            @Override
            public void onSuccess(AirMapStatus response) {
                List<AirMapStatusAdvisory> advisories = response.getAdvisories();
                boolean requiresPermitOrNotice = false;
                for (AirMapStatusAdvisory advisory : advisories) {
                    if (advisory.getRequirements() != null) {
                        if (advisory.getRequirements().getPermit() != null &&
                                advisory.getRequirements().getPermit().getTypes() != null &&
                                !advisory.getRequirements().getPermit().getTypes().isEmpty()) {
                            requiresPermitOrNotice = true;
                        } else if (advisory.getRequirements().getNotice() != null &&
                                !advisory.getRequirements().getNotice().getPhoneNumber().isEmpty()) {
                            requiresPermitOrNotice = true;
                        }
                    }
                }

                updateButtonText(requiresPermitOrNotice ? R.string.airmap_next : R.string.airmap_save);
            }

            @Override
            public void onError(AirMapException e) {
                updateButtonText(R.string.airmap_save);
            }
        });
    }

    private void updateButtonText(@StringRes final int id) {
        saveNextButton.post(new Runnable() {
            @Override
            public void run() {
                saveNextButton.setText(id);
            }
        });
    }

    private void hideProgressBar() {
        progressBarContainer.post(new Runnable() {
            @Override
            public void run() {
                progressBarContainer.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CREATE_AIRCRAFT) {
            if (resultCode == Activity.RESULT_OK) {
                setupAircraftSpinnerAdapter(); //Refresh the spinner options
            }
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    public abstract class SeekBarChangeListener implements SeekBar.OnSeekBarChangeListener {

        protected PolygonOptions oldPolygon;

        @Override
        public abstract void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser);

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            updateSaveNextButtonText();
            mListener.flightChanged();
        }
    }

    public abstract class AnimationListener implements Animator.AnimatorListener {
        @Override
        public void onAnimationStart(Animator animation) {

        }

        @Override
        public abstract void onAnimationEnd(Animator animation);

        @Override
        public void onAnimationCancel(Animator animation) {

        }

        @Override
        public void onAnimationRepeat(Animator animation) {

        }
    }

    public interface OnFragmentInteractionListener {
        AirMapFlight getFlight();

        void flightDetailsSaveClicked(AirMapFlight response);

        void flightDetailsNextClicked(AirMapStatus flightStatus);

        void flightChanged();
    }
}