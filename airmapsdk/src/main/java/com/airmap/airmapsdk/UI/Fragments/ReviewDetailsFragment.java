package com.airmap.airmapsdk.UI.Fragments;

import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.airmap.airmapsdk.Models.Flight.AirMapFlight;
import com.airmap.airmapsdk.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static com.airmap.airmapsdk.Utils.getDurationPresets;
import static com.airmap.airmapsdk.Utils.indexOfDurationPreset;
import static com.airmap.airmapsdk.Utils.metersToFeet;


public class ReviewDetailsFragment extends Fragment {

    private static final String ARG_FLIGHT = "flight";

    private AirMapFlight flight;
    private TextView radiusTextView;
    private TextView altitudeTextView;
    private TextView startsAtTextView;
    private TextView durationTextView;
    private TextView aircraftTextView;
    private TextView publicFlightTextView;

    public ReviewDetailsFragment() {
        // Required empty public constructor
    }

    public static ReviewDetailsFragment newInstance(AirMapFlight flight) {
        ReviewDetailsFragment fragment = new ReviewDetailsFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_FLIGHT, flight);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_review_details, container, false);
        this.flight = (AirMapFlight) getArguments().getSerializable(ARG_FLIGHT);
        initializeViews(view);
        populateViews();
        return view;
    }

    private void initializeViews(View view) {
        radiusTextView = getTextViewById(view, R.id.radius_value);
        altitudeTextView = getTextViewById(view, R.id.altitude_value);
        startsAtTextView = getTextViewById(view, R.id.starts_at_value);
        durationTextView = getTextViewById(view, R.id.duration_value);
        aircraftTextView = getTextViewById(view, R.id.aircraft_value);
        publicFlightTextView = getTextViewById(view, R.id.public_flight_value);
    }

    private void populateViews() {
        SimpleDateFormat format = new SimpleDateFormat("MM/dd/yy h:mm a", Locale.US);
        radiusTextView.setText(String.format(Locale.US, "%d ft", Math.round(metersToFeet(flight.getBuffer()))));
        altitudeTextView.setText(String.format(Locale.US, "%d ft", Math.round(metersToFeet(flight.getMaxAltitude()))));
        if (flight.getStartsAt() != null) {
            startsAtTextView.setText(format.format(flight.getStartsAt()));
        } else {
            startsAtTextView.setText(format.format(new Date()));
        }
        durationTextView.setText(getDurationText());
        if (flight.getAircraft() != null) {
            aircraftTextView.setText(flight.getAircraft().getModel().toString()); //Display only the model name
        }
        publicFlightTextView.setText(flight.isPublic() ? "Yes" : "No");
    }

    public String getDurationText() {
        long difference = flight.getEndsAt().getTime() - flight.getStartsAt().getTime();
        int index = indexOfDurationPreset(difference);
        if (index != -1){
            return getDurationPresets()[index].label;
        }
        return String.format(Locale.US, "%d seconds", difference/1000);
    }

    private TextView getTextViewById(View view, @IdRes int id) {
        return (TextView) view.findViewById(id);
    }
}
