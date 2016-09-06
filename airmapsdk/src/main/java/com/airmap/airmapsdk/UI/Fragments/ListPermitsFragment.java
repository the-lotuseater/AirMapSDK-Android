package com.airmap.airmapsdk.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.airmap.airmapsdk.models.permits.AirMapAvailablePermit;
import com.airmap.airmapsdk.models.permits.AirMapPilotPermit;
import com.airmap.airmapsdk.models.status.AirMapStatusPermits;
import com.airmap.airmapsdk.R;
import com.airmap.airmapsdk.ui.adapters.PermitsAdapter;

import java.util.ArrayList;
import java.util.Locale;

public class ListPermitsFragment extends Fragment {

    private OnFragmentInteractionListener mListener;

    private TextView summaryTextView;
    private RecyclerView recyclerView;
    private PermitsAdapter adapter;
    private Button nextButton;

    public ListPermitsFragment() {
        // Required empty public constructor
    }

    public static ListPermitsFragment newInstance() {
        return new ListPermitsFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.airmap_fragment_list_permits, container, false);
        initializeViews(view);
        setupRecyclerView();
        updateSummaryText();
        updateNextButton();
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onListPermitsNextClicked(adapter.getSelectedPermits());
            }
        });
        return view;
    }

    private void initializeViews(View view) {
        recyclerView = (RecyclerView) view.findViewById(R.id.permit_list);
        nextButton = (Button) view.findViewById(R.id.next_button);
        summaryTextView = (TextView) view.findViewById(R.id.summary);
    }

    private void setupRecyclerView() {
        if (adapter == null) {
            adapter = new PermitsAdapter(mListener.getStatusPermits(), mListener.getPermitsFromWallet(), this, mListener);
            recyclerView.setAdapter(adapter);
        }
    }

    public void onEnabledPermit(AirMapAvailablePermit permit) {
        adapter.addEnabledPermit(permit);
        updateSummaryText();
        updateNextButton();
    }

    public void updateNextButton() {
        nextButton.setEnabled(adapter.getSelectedPermits().size() == adapter.getItemCount());
    }

    public void updateSummaryText() {
        String template = "You have selected %d of %d permits required for this flight";
        String summary = String.format(Locale.US, template, adapter.getSelectedPermits().size(), adapter.getItemCount());
        summaryTextView.setText(summary);
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
        setupRecyclerView();
    }

    public interface OnFragmentInteractionListener {
        ArrayList<AirMapStatusPermits> getStatusPermits();

        ArrayList<AirMapPilotPermit> getPermitsFromWallet();

        void showDecisionFlow(AirMapStatusPermits permit);

        void onListPermitsNextClicked(ArrayList<AirMapAvailablePermit> selectedPermits);
    }
}
