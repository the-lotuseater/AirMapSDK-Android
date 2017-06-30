package com.airmap.airmapsdk.ui.activities;


import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.airmap.airmapsdk.R;
import com.airmap.airmapsdk.models.welcome.AirMapWelcomeResult;
import com.airmap.airmapsdk.ui.adapters.WelcomeAdapter;
import com.airmap.airmapsdk.util.AirMapConstants;

import java.util.List;

public class WelcomeActivity extends AppCompatActivity {

    private static final String TAG = "WelcomeActivity";

    private RecyclerView recyclerView;
    private WelcomeAdapter adapter;

    private List<AirMapWelcomeResult> welcome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        welcome = (List<AirMapWelcomeResult>) getIntent().getSerializableExtra(AirMapConstants.WELCOME_EXTRA);

        setContentView(R.layout.activity_welcome);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //noinspection ConstantConditions
        getSupportActionBar().setTitle(getIntent().getStringExtra(AirMapConstants.CITY_EXTRA));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        recyclerView = (RecyclerView) findViewById(R.id.welcome_results_recycler_view);
        adapter = new WelcomeAdapter(this, welcome);
        recyclerView.setAdapter(adapter);
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
