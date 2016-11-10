package com.airmap.airmapsdk.ui.activities;


import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;

import com.airmap.airmapsdk.R;
import com.airmap.airmapsdk.models.permits.AirMapAvailablePermit;
import com.airmap.airmapsdk.models.permits.AirMapPilotPermit;
import com.airmap.airmapsdk.models.status.AirMapStatusPermits;
import com.airmap.airmapsdk.ui.adapters.SelectPermitsAdapter;
import com.airmap.airmapsdk.util.Constants;

import java.util.ArrayList;
import java.util.List;

public class PermitSelectionActivity extends AppCompatActivity {

    private static final String TAG = "PermitSelectionActivity";

    private RecyclerView recyclerView;
    private SelectPermitsAdapter adapter;

    private AirMapStatusPermits permits;
    private List<AirMapPilotPermit> permitsFromWallet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        permits = (AirMapStatusPermits) getIntent().getSerializableExtra(Constants.STATUS_PERMIT_EXTRA);
        permitsFromWallet = (ArrayList<AirMapPilotPermit>) getIntent().getSerializableExtra(Constants.PERMIT_WALLET_EXTRA);

        setContentView(R.layout.airmap_activity_select_permits);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //noinspection ConstantConditions
        getSupportActionBar().setTitle(permits.getAuthorityName());
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        recyclerView = (RecyclerView) findViewById(R.id.permit_list);
        adapter = new SelectPermitsAdapter(this, permits, permitsFromWallet);
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent dataFromCustomProperties) {
        if (requestCode == Constants.CUSTOM_PROPERTIES_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Intent data = new Intent();
                data.putExtra(Constants.AVAILABLE_PERMIT_EXTRA, dataFromCustomProperties.getSerializableExtra(Constants.AVAILABLE_PERMIT_EXTRA));
                setResult(RESULT_OK, data);
                finish();
            }
        }
        super.onActivityResult(requestCode, resultCode, dataFromCustomProperties);
    }
}
