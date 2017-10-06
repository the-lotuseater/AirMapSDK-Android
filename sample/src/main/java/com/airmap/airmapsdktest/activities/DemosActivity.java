package com.airmap.airmapsdktest.activities;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

import com.airmap.airmapsdk.AirMapException;
import com.airmap.airmapsdk.AirMapLog;
import com.airmap.airmapsdk.Auth;
import com.airmap.airmapsdk.models.pilot.AirMapPilot;
import com.airmap.airmapsdk.networking.callbacks.AirMapCallback;
import com.airmap.airmapsdk.networking.callbacks.LoginCallback;
import com.airmap.airmapsdk.networking.services.AirMap;
import com.airmap.airmapsdk.util.AirMapConstants;
import com.airmap.airmapsdktest.R;

/**
 * Created by collin@airmap.com on 9/8/17.
 */

public class DemosActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "DemosActivity";

    private Toolbar toolbar;
    private CardView mapCardView;
    private CardView loginCardView;
    private CardView anonymousLoginCardView;
    private CardView flightPlanCardView;
    private CardView briefingCardView;
    private CardView trafficCardView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demos);

        setupViews();
    }

    @Override
    protected void onResume() {
        super.onResume();

        refreshAccessToken();
    }

    private void setupViews() {
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(getTitle());
        setSupportActionBar(toolbar);

        mapCardView = findViewById(R.id.map_card_view);
        mapCardView.setOnClickListener(this);

        loginCardView = findViewById(R.id.login_card_view);
        loginCardView.setOnClickListener(this);

        anonymousLoginCardView = findViewById(R.id.anonymous_login_card_view);
        anonymousLoginCardView.setOnClickListener(this);

        flightPlanCardView = findViewById(R.id.flight_plan_card_view);
        flightPlanCardView.setOnClickListener(this);

        briefingCardView = findViewById(R.id.briefing_card_view);
        briefingCardView.setOnClickListener(this);

        trafficCardView = findViewById(R.id.traffic_card_view);
        trafficCardView.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.map_card_view) {
            startActivity(new Intent(this, MapDemoActivity.class));
        } else if (view.getId() == R.id.login_card_view) {
            AirMap.showLogin(this, new LoginCallback() {
                @Override
                public void onSuccess(AirMapPilot pilot) {
                    AirMapLog.v(TAG, "Auth Token is: " + AirMap.getAuthToken());
                    Toast.makeText(DemosActivity.this, "Logged in as " + pilot.getUsername(), Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onError(AirMapException e) {
                    AirMapLog.e(TAG, e.getDetailedMessage(), e);
                    Toast.makeText(DemosActivity.this, "Login failed", Toast.LENGTH_SHORT).show();
                }
            });
        } else if (view.getId() == R.id.anonymous_login_card_view) {
            startActivity(new Intent(this, AnonymousLoginDemoActivity.class));
        } else if (view.getId() == R.id.traffic_card_view) {
            startActivity(new Intent(this, TrafficActivity.class));
        } else if (view.getId() == R.id.flight_plan_card_view) {
            startActivity(new Intent(this, FlightPlanDemoActivity.class));
        } else {
            String flightPlanId = PreferenceManager.getDefaultSharedPreferences(this).getString(AirMapConstants.FLIGHT_PLAN_ID_EXTRA, null);

            Intent intent = new Intent(this, FlightBriefDemoActivity.class);
            intent.putExtra(AirMapConstants.FLIGHT_PLAN_ID_EXTRA, flightPlanId);
            startActivity(intent);
        }
    }

    private void refreshAccessToken() {
        AirMap.refreshAccessToken(new AirMapCallback<Void>() {
            @Override
            public void onSuccess(Void response) {
                if (!isActive()) {
                    return;
                }

                AirMapLog.e(TAG, "Refreshing access token success");
            }

            @Override
            public void onError(AirMapException e) {
                if (!isActive()) {
                    return;
                }

                AirMapLog.e(TAG, "Refreshing access token failed", e);
            }
        });
    }
}
