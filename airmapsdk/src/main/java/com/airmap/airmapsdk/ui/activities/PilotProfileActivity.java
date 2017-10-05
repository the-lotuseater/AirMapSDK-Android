package com.airmap.airmapsdk.ui.activities;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.airmap.airmapsdk.AirMapException;
import com.airmap.airmapsdk.R;
import com.airmap.airmapsdk.models.pilot.AirMapPilot;
import com.airmap.airmapsdk.networking.callbacks.AirMapCallback;
import com.airmap.airmapsdk.networking.services.AirMap;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.text.NumberFormat;

/**
 * Activity for viewing another pilot's profile (not your own)
 * @see com.airmap.airmapsdk.ui.activities.ProfileActivity
 */
public class PilotProfileActivity extends AppCompatActivity {

    public static final String ARG_PILOT_ID = "pilotId";

    private Toolbar toolbar;
    private ImageView profileImageView;
    private TextView nameTextView;
    private TextView usernameTextView;
    private TextView flightCounterTextView;
    private TextView aircraftCounterTextView;
    private AirMapPilot profile;
    int sizeInDp;
    float scale;
    int dpAsPixels;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.airmap_activity_pilot_profile);
        initializeViews();
        setSupportActionBar(toolbar);
        //noinspection ConstantConditions
        getSupportActionBar().setTitle(R.string.airmap_title_activity_profile);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        String pilotId = getIntent().getStringExtra(ARG_PILOT_ID);
        if (pilotId == null || pilotId.isEmpty()) {
            pilotId = AirMap.getUserId();
        }
        getPilot(pilotId);
        sizeInDp = 16;
        scale = getResources().getDisplayMetrics().density;
        dpAsPixels = (int) (sizeInDp * scale + 0.5f);
    }

    private void getPilot(final String pilotId) {
        AirMap.getPilot(pilotId, new AirMapCallback<AirMapPilot>() {
            @Override
            public void onSuccess(AirMapPilot response) {
                if (!isFinishing()) {
                    profile = response;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            populateViews();
                        }
                    });
                }
            }

            @Override
            public void onError(final AirMapException e) {
                if (!isFinishing()) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Snackbar.make(toolbar, R.string.error_getting_profile, Snackbar.LENGTH_LONG)
                                    .setAction(R.string.retry, new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            getPilot(pilotId);
                                        }
                                    })
                                    .show();
                        }
                    });
                    Log.e("ProfileFragment", e.getMessage(), e);
                }
            }
        });
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        profileImageView = findViewById(R.id.profile_image);
        nameTextView = findViewById(R.id.name);
        usernameTextView = findViewById(R.id.username);
        flightCounterTextView = findViewById(R.id.flight_counter_text);
        aircraftCounterTextView = findViewById(R.id.aircraft_counter_text);
    }

    private void populateViews() {
        Glide.with(this)
                .load(profile.getPictureUrl())
                .listener(new RequestListener<String, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        if (profileImageView != null && resource != null) {
                            profileImageView.setImageDrawable(resource);
                        }
                        return true;
                    }
                })
                .placeholder(R.drawable.airmap_profile_default)
                .into(profileImageView);

        findViewById(R.id.progress_bar_container).setVisibility(View.GONE);

        nameTextView.setText(String.format("%s %s", profile.getFirstName(), profile.getLastName()));
        nameTextView.setVisibility(TextUtils.isEmpty(profile.getFirstName()) ? View.GONE : View.VISIBLE);
        usernameTextView.setText(profile.getUsername());

        NumberFormat format = NumberFormat.getIntegerInstance();
        try {
            aircraftCounterTextView.setText(format.format(profile.getStats().getAircraftStats().getTotal()));
        } catch (Exception e) {
            e.printStackTrace(); //Probably some NPE
            aircraftCounterTextView.setText("-");
        }

        try {
            flightCounterTextView.setText(format.format(profile.getStats().getFlightStats().getTotal()));
        } catch (Exception e) {
            e.printStackTrace(); //Probably some NPE
            flightCounterTextView.setText("-");
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
