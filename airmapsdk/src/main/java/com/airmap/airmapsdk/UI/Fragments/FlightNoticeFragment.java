package com.airmap.airmapsdk.ui.fragments;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.airmap.airmapsdk.Analytics;
import com.airmap.airmapsdk.R;
import com.airmap.airmapsdk.models.flight.AirMapFlight;
import com.airmap.airmapsdk.models.permits.AirMapPermitIssuer;
import com.airmap.airmapsdk.models.status.AirMapStatus;
import com.airmap.airmapsdk.models.status.AirMapStatusAdvisory;
import com.airmap.airmapsdk.models.status.AirMapStatusRequirementNotice;
import com.airmap.airmapsdk.ui.activities.WebActivity;
import com.airmap.airmapsdk.util.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FlightNoticeFragment extends Fragment {

    private OnFragmentInteractionListener mListener;

    private TextView submitNoticeSwitch;
    private ListView digitalNoticeListView;
    private TextView notDigitalLabelTextView;
    private ListView notDigitalNoticeListView;
    private Button nextButton;

    private List<AirMapStatusRequirementNotice> digitalNotices;
    private Set<String> digitalNoticeNames;
    private List<AirMapStatusRequirementNotice> notDigitalNotices;
    private List<String> notDigitalNoticeNames;
    private List<String> notDigitalNoticeTypes;

    public FlightNoticeFragment() {
        // Required empty public constructor
    }

    public static FlightNoticeFragment newInstance() {
        FlightNoticeFragment fragment = new FlightNoticeFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.airmap_fragment_flight_notice, container, false);
        initializeViews(view);
        getNotices();
        setupDigitalNoticeList();
        setupNotDigitalNoticeList();
        continueToNextFragmentIfNecessary();
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onNextButton();
            }
        });
        return view;
    }

    private void initializeViews(View view) {
        submitNoticeSwitch = (TextView) view.findViewById(R.id.submit_notice_switch);
        digitalNoticeListView = (ListView) view.findViewById(R.id.digital_notice_list);
        notDigitalLabelTextView = (TextView) view.findViewById(R.id.not_digital_label);
        notDigitalLabelTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), WebActivity.class);
                intent.putExtra(Constants.URL_EXTRA, Constants.FAQ_URL);
                intent.putExtra(Intent.EXTRA_TITLE, "FAQ");
                startActivity(intent);
            }
        });
        notDigitalNoticeListView = (ListView) view.findViewById(R.id.not_digital_list);
        nextButton = (Button) view.findViewById(R.id.next_button);
    }

    private void getNotices() {
        digitalNotices = new ArrayList<>();
        digitalNoticeNames = new HashSet<>();
        notDigitalNotices = new ArrayList<>();
        notDigitalNoticeNames = new ArrayList<>();
        notDigitalNoticeTypes = new ArrayList<>();
        AirMapStatus status = mListener.getFlightStatus();
        for (AirMapStatusAdvisory advisory : status.getAdvisories()) {
            if (advisory.getRequirements() != null && advisory.getRequirements().getNotice() != null) {
                AirMapStatusRequirementNotice notice = advisory.getRequirements().getNotice();
                if (notice.isDigital() || !TextUtils.isEmpty(advisory.getOrganizationId())) {
                    digitalNotices.add(notice);

                    boolean useAdvisoryName = true;
                    if (!TextUtils.isEmpty(advisory.getOrganizationId())) {
                        for (AirMapPermitIssuer issuer : status.getOrganizations()) {
                            if (advisory.getOrganizationId().equals(issuer.getId())) {
                                digitalNoticeNames.add(issuer.getName());
                                useAdvisoryName = false;
                            }
                        }
                    }

                    if (useAdvisoryName) {
                        digitalNoticeNames.add(advisory.getName());
                    }
                } else if (notice.isNoticeRequired()) {
                    notDigitalNotices.add(notice);
                    notDigitalNoticeNames.add(advisory.getName());
                    notDigitalNoticeTypes.add(getString(advisory.getType().getTitle()));
                }
            }
        }

        // If there aren't any advisories that accept digital notice, then set notify to false
        if (digitalNotices.size() == 0) {
            submitNoticeSwitch.setVisibility(View.GONE);
            mListener.getFlight().setNotify(false);
        } else {
            mListener.getFlight().setNotify(true);
        }
    }

    private void setupDigitalNoticeList() {
        AirMapStatus status = mListener.getFlightStatus();
        if (digitalNoticeNames.isEmpty() || !status.getApplicablePermits().isEmpty()) {
            submitNoticeSwitch.setVisibility(View.GONE);
        }

        if (!digitalNoticeNames.isEmpty()) {
            digitalNoticeListView.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, new ArrayList<>(digitalNoticeNames)));
        }
    }

    private void setupNotDigitalNoticeList() {
        if (notDigitalNoticeNames.isEmpty()) {
            notDigitalLabelTextView.setVisibility(View.GONE);
        } else {
            List<Map<String, String>> list = new ArrayList<>();
            for (int i = 0; i < notDigitalNotices.size(); i++) {
                Map<String, String> map = new HashMap<>();
                map.put("name", notDigitalNoticeNames.get(i));
                String number = notDigitalNotices.get(i).getPhoneNumber();
                if (number == null || number.length() < 10) {
                    number = getString(R.string.no_phone_number_provided);
                } else {
                    number = PhoneNumberUtils.formatNumber(number);
                }
                map.put("type", notDigitalNoticeTypes.get(i));
                map.put("phone", number);
                list.add(map);
            }
            notDigitalNoticeListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    String number = notDigitalNotices.get(position).getPhoneNumber();
                    if (number != null && number.length() >= 10) {
                        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + number));
                        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                            startActivity(intent);
                        }
                    }
                }
            });
            notDigitalNoticeListView.setAdapter(new SimpleAdapter(getContext(), list, R.layout.flight_notice_not_digital_list_item, new String[]{"name", "type", "phone"}, new int[]{R.id.authority_name, R.id.authority_type, R.id.phone_number}));
        }
    }

    //If there are no digital notices or non-digital airports that need notice, go to next fragment
    private void continueToNextFragmentIfNecessary() {
        if (notDigitalLabelTextView.getVisibility() == View.GONE && submitNoticeSwitch.getVisibility() == View.GONE) {
            onNextButton();
        }
    }

    private void onNextButton() {
        Analytics.logEvent(Analytics.Page.NOTICES_CREATE_FLIGHT, Analytics.Action.tap, Analytics.Label.REVIEW);

        mListener.onFlightNoticeNextButtonClicked();
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

    public interface OnFragmentInteractionListener {
        AirMapFlight getFlight();

        AirMapStatus getFlightStatus();

        void onFlightNoticeNextButtonClicked();
    }
}
