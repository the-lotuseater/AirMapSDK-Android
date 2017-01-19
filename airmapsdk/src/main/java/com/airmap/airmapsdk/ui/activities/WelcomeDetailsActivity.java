package com.airmap.airmapsdk.ui.activities;


import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.airmap.airmapsdk.R;
import com.airmap.airmapsdk.models.welcome.AirMapWelcomeResult;
import com.airmap.airmapsdk.util.Constants;

public class WelcomeDetailsActivity extends AppCompatActivity {

    private static final String TAG = "WelcomeDetailsActivity";

    private AirMapWelcomeResult welcomeResult;

    private TextView summaryTextView;
    private TextView linkTextView;
    private Button moreButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        welcomeResult = (AirMapWelcomeResult) getIntent().getSerializableExtra(Constants.WELCOME_RESULT_EXTRA);

        setContentView(R.layout.activity_welcome_result);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //noinspection ConstantConditions
        getSupportActionBar().setTitle(welcomeResult.getJurisdictionName());
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        summaryTextView = (TextView) findViewById(R.id.summary_text_view);
        linkTextView = (TextView) findViewById(R.id.link_text_view);
        moreButton = (Button) findViewById(R.id.read_full_button);

        summaryTextView.setText(welcomeResult.getText());

        if (!TextUtils.isEmpty(welcomeResult.getUrl())) {
            linkTextView.setText(welcomeResult.getUrl());
        } else {
            linkTextView.setVisibility(View.GONE);
        }

        moreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO:
            }
        });
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
