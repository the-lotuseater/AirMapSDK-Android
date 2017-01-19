package com.airmap.airmapsdk.ui.activities;


import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.airmap.airmapsdk.R;
import com.airmap.airmapsdk.models.welcome.AirMapWelcome;
import com.airmap.airmapsdk.ui.adapters.WelcomeAdapter;
import com.airmap.airmapsdk.util.Constants;

public class WelcomeActivity extends AppCompatActivity {

    private static final String TAG = "WelcomeActivity";

    private RecyclerView recyclerView;
    private WelcomeAdapter adapter;

    private AirMapWelcome welcome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        welcome = (AirMapWelcome) getIntent().getSerializableExtra(Constants.WELCOME_EXTRA);

        setContentView(R.layout.activity_welcome);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //noinspection ConstantConditions
        getSupportActionBar().setTitle(getIntent().getStringExtra(Constants.CITY_EXTRA));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        recyclerView = (RecyclerView) findViewById(R.id.welcome_results_recycler_view);
        adapter = new WelcomeAdapter(this, welcome.getResults());
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
