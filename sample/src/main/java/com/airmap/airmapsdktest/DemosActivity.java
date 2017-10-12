package com.airmap.airmapsdktest;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.View;

/**
 * Created by collin@airmap.com on 9/8/17.
 */

public class DemosActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "DemosActivity";

    private Toolbar toolbar;
    private CardView mapCardView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demos);

        setupViews();
    }

    private void setupViews() {
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(getTitle());
        setSupportActionBar(toolbar);

        mapCardView = findViewById(R.id.map_card_view);
        mapCardView.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.map_card_view) {
            startActivity(new Intent(this, MapDemoActivity.class));
        }
    }
}
