package com.airmap.airmapsdktest.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.SeekBar;

import com.airmap.airmapsdk.AirMapException;
import com.airmap.airmapsdk.AirMapLog;
import com.airmap.airmapsdk.models.Coordinate;
import com.airmap.airmapsdk.models.flight.AirMapFlightBriefing;
import com.airmap.airmapsdk.models.flight.AirMapFlightFeature;
import com.airmap.airmapsdk.models.flight.AirMapFlightPlan;
import com.airmap.airmapsdk.models.rules.AirMapRule;
import com.airmap.airmapsdk.models.rules.AirMapRuleset;
import com.airmap.airmapsdk.models.shapes.AirMapGeometry;
import com.airmap.airmapsdk.models.shapes.AirMapPolygon;
import com.airmap.airmapsdk.networking.callbacks.AirMapCallback;
import com.airmap.airmapsdk.networking.services.AirMap;
import com.airmap.airmapsdk.ui.adapters.ExpandableRulesAdapter;
import com.airmap.airmapsdk.ui.views.RulesetsEvaluator;
import com.airmap.airmapsdk.util.AirMapConstants;
import com.airmap.airmapsdk.util.BriefingEvaluator;
import com.airmap.airmapsdktest.R;
import com.airmap.airmapsdktest.ui.FlightPlanDetailsAdapter;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by collin@airmap.com on 9/27/17.
 */

public class FlightBriefDemoActivity extends BaseActivity {

    private static final String TAG = "FlightBriefActivity";

    private Toolbar toolbar;
    private RecyclerView rulesRecyclerView;
    private View loadingView;

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

        String flightPlanId = getIntent().getStringExtra(AirMapConstants.FLIGHT_PLAN_ID_EXTRA);
        AirMap.getFlightBrief(flightPlanId, new AirMapCallback<AirMapFlightBriefing>() {
            @Override
            protected void onSuccess(AirMapFlightBriefing briefing) {
                loadingView.setVisibility(View.GONE);

                // calculate which rules are being violated, followed, etc
                LinkedHashMap<AirMapRule.Status, List<AirMapRule>> sortedRulesMap = BriefingEvaluator.computeRulesViolations(briefing);

                ExpandableRulesAdapter rulesRecyclerAdapter = new ExpandableRulesAdapter(sortedRulesMap);
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

}
