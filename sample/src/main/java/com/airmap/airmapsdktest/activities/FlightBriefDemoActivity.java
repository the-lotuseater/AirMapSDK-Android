package com.airmap.airmapsdktest.activities;

import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.airmap.airmapsdk.AirMapException;
import com.airmap.airmapsdk.AirMapLog;
import com.airmap.airmapsdk.models.flight.AirMapFlightBriefing;
import com.airmap.airmapsdk.models.flight.AirMapFlightPlan;
import com.airmap.airmapsdk.models.rules.AirMapRule;
import com.airmap.airmapsdk.networking.callbacks.AirMapCallback;
import com.airmap.airmapsdk.networking.services.AirMap;
import com.airmap.airmapsdk.ui.adapters.ExpandableRulesAdapter;
import com.airmap.airmapsdk.util.AirMapConstants;
import com.airmap.airmapsdk.util.BriefingEvaluator;
import com.airmap.airmapsdktest.R;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by collin@airmap.com on 9/27/17.
 */

public class FlightBriefDemoActivity extends BaseActivity {

    private static final String TAG = "FlightBriefActivity";

    private Toolbar toolbar;
    private RecyclerView rulesRecyclerView;
    private View loadingView;

    private String flightPlanId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flight_brief);

        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(getTitle());
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        rulesRecyclerView = findViewById(R.id.rules_recycler_view);
        rulesRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        loadingView = findViewById(R.id.loading_view);

        flightPlanId = getIntent().getStringExtra(AirMapConstants.FLIGHT_PLAN_ID_EXTRA);
        AirMap.getFlightBrief(flightPlanId, new AirMapCallback<AirMapFlightBriefing>() {
            @Override
            protected void onSuccess(AirMapFlightBriefing briefing) {
                loadingView.setVisibility(View.GONE);

                // calculate which rules are being violated, followed, etc
                LinkedHashMap<AirMapRule.Status, List<AirMapRule>> sortedRulesMap = BriefingEvaluator.computeRulesViolations(briefing);

                ExpandableRulesAdapter rulesRecyclerAdapter = new BriefingAdapter(sortedRulesMap);
                rulesRecyclerView.setAdapter(rulesRecyclerAdapter);
            }

            @Override
            protected void onError(AirMapException e) {
                AirMapLog.e(TAG, "flight brief failed", e);
                if (!isActive()) {
                    return;
                }

                showErrorDialog("An error occurred while creating your flight plan or retrieving your flight brief.");
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // only show Fly option if user has flight plan
        if (!TextUtils.isEmpty(flightPlanId)) {
            getMenuInflater().inflate(R.menu.brief, menu);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == R.id.fly) {
            AirMap.submitFlightPlan(flightPlanId, new AirMapCallback<AirMapFlightPlan>() {
                @Override
                protected void onSuccess(AirMapFlightPlan response) {
                    AirMapLog.e(TAG, "Successfully created " + response.getFlightId() + " from flight plan");
                    if (!isActive()) {
                        return;
                    }

                    Toast.makeText(FlightBriefDemoActivity.this, "Flight created!", Toast.LENGTH_SHORT).show();
                }

                @Override
                protected void onError(AirMapException e) {
                    AirMapLog.e(TAG, "Failed to create flight from flight plan", e);
                    if (!isActive()) {
                        return;
                    }

                    Toast.makeText(FlightBriefDemoActivity.this, "Failed to create flight", Toast.LENGTH_SHORT).show();
                }
            });
        }

        return super.onOptionsItemSelected(menuItem);
    }

    /**
     *  You can customize the UI of the briefing rows by overriding onCreateViewHolder
     */
    private class BriefingAdapter extends ExpandableRulesAdapter {

        BriefingAdapter(LinkedHashMap<AirMapRule.Status, List<AirMapRule>> rulesMap) {
            super(rulesMap);
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view;
            switch (viewType) {
                case PARENT_VIEW_TYPE:
                    view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_rule_section, parent, false);
                    return new SectionViewHolder(view);
                case CHILD_VIEW_TYPE:
                    view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_rule, parent, false);
                    return new RuleViewHolder(view);
            }
            return super.onCreateViewHolder(parent, viewType);
        }
    }
}
