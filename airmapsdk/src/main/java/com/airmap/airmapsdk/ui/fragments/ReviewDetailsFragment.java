package com.airmap.airmapsdk.ui.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.airmap.airmapsdk.R;
import com.airmap.airmapsdk.models.flight.AirMapFlight;
import com.airmap.airmapsdk.models.shapes.AirMapPoint;
import com.airmap.airmapsdk.util.AirMapConstants;
import com.airmap.airmapsdk.util.Utils;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.Date;

import static com.airmap.airmapsdk.util.Utils.getDurationPresets;
import static com.airmap.airmapsdk.util.Utils.indexOfDurationPreset;


public class ReviewDetailsFragment extends Fragment {

    private static final String ARG_FLIGHT = "flight";

    private AirMapFlight flight;
    private boolean useMetric;

    private TextView radiusTextView;
    private TextView altitudeTextView;
    private TextView startsAtTextView;
    private TextView durationTextView;
    private TextView aircraftTextView;
    private View publicFlightView;
    private View infoButton;
    private LinearLayout radiusContainer;
    private LinearLayout aircraftContainer;

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
        flight = (AirMapFlight) getArguments().getSerializable(ARG_FLIGHT);
        useMetric = Utils.useMetric(getActivity());

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_review_details, container, false);
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
        publicFlightView = view.findViewById(R.id.public_flight_view);
        infoButton = view.findViewById(R.id.airmap_info_button);
        radiusContainer = (LinearLayout) view.findViewById(R.id.radius_container);
        aircraftContainer = (LinearLayout) view.findViewById(R.id.aircraft_container);
    }

    private void populateViews() {
        NumberFormat numberFormat = NumberFormat.getIntegerInstance();
        DateFormat dateFormat = Utils.getDateTimeFormat();

        if (flight.getGeometry() instanceof AirMapPoint) {
            String radius = Utils.getMeasurementText(getActivity(), flight.getBuffer(), useMetric);
            radiusTextView.setText(radius);
        } else {
            radiusContainer.setVisibility(View.GONE);
        }

        String altitude = Utils.getMeasurementText(getActivity(), flight.getMaxAltitude(), useMetric);
        altitudeTextView.setText(altitude);

        if (flight.getStartsAt() != null) {
            startsAtTextView.setText(dateFormat.format(flight.getStartsAt()));
        } else {
            startsAtTextView.setText(dateFormat.format(new Date()));
        }
        durationTextView.setText(getDurationText());
        if (flight.getAircraft() != null) {
            aircraftTextView.setText(flight.getAircraft().getModel().toString()); //Display only the model name
        } else {
            aircraftContainer.setVisibility(View.GONE);
        }
        publicFlightView.setVisibility(flight.isPublic() ? View.VISIBLE : View.GONE);

        infoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(AirMapConstants.INFO_URL));
                startActivity(intent);
            }
        });
    }

    public String getDurationText() {
        long difference = flight.getEndsAt().getTime() - flight.getStartsAt().getTime();
        int index = indexOfDurationPreset(difference);
        if (index != -1) {
            return Utils.getDurationText(getActivity(), getDurationPresets()[index]);
        }
        return Utils.getDurationText(getActivity(), difference);
    }

    private TextView getTextViewById(View view, @IdRes int id) {
        return (TextView) view.findViewById(id);
    }
}
