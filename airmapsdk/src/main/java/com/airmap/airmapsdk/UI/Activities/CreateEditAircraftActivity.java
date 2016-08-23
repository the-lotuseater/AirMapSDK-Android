package com.airmap.airmapsdk.UI.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.airmap.airmapsdk.AirMapException;
import com.airmap.airmapsdk.Models.Aircraft.AirMapAircraft;
import com.airmap.airmapsdk.Models.Aircraft.AirMapAircraftManufacturer;
import com.airmap.airmapsdk.Models.Aircraft.AirMapAircraftModel;
import com.airmap.airmapsdk.Networking.Callbacks.AirMapCallback;
import com.airmap.airmapsdk.Networking.Services.AirMap;
import com.airmap.airmapsdk.R;

import java.util.ArrayList;
import java.util.List;

public class CreateEditAircraftActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String AIRCRAFT = "aircraft";

    private Toolbar toolbar;
    private EditText nicknameEditText;
    private Spinner manufacturerSpinner;
    private Spinner modelSpinner;
    private AirMapAircraft aircraftToEdit;
    private Button saveButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.airmap_activity_create_aircraft);
        initializeViews();
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        int id;
        if (getIntent() != null && getIntent().getSerializableExtra(AIRCRAFT) != null) {
            aircraftToEdit = (AirMapAircraft) getIntent().getSerializableExtra(AIRCRAFT);
            populateViews(aircraftToEdit);
            id = R.string.airmap_edit_aircraft;
        } else {
            populateViews();
            id = R.string.airmap_title_activity_create_aircraft;
        }
        getSupportActionBar().setTitle(id);
    }

    private void populateViews() {
        AirMap.getManufacturers(new AirMapCallback<List<AirMapAircraftManufacturer>>() {
            @Override
            public void onSuccess(final List<AirMapAircraftManufacturer> response) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        manufacturerSpinner.setAdapter(new ArrayAdapter<>(CreateEditAircraftActivity.this, android.R.layout.simple_spinner_dropdown_item, response)); //The AirMapAircraftManufacturer's toString takes care of proper representation of data
                    }
                });
            }

            @Override
            public void onError(AirMapException e) {
                toast("Error getting manufacturers");
            }
        });

        manufacturerSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                AirMapAircraftManufacturer manufacturer = (AirMapAircraftManufacturer) parent.getAdapter().getItem(pos);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        modelSpinner.setVisibility(View.GONE);
                    }
                });
                AirMap.getModels(manufacturer.getId(), new AirMapCallback<List<AirMapAircraftModel>>() {
                    @Override
                    public void onSuccess(final List<AirMapAircraftModel> response) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                modelSpinner.setAdapter(new ArrayAdapter<>(CreateEditAircraftActivity.this, android.R.layout.simple_spinner_dropdown_item, response));
                                modelSpinner.setVisibility(View.VISIBLE);
                            }
                        });
                    }

                    @Override
                    public void onError(AirMapException e) {
                        e.printStackTrace();
                        Log.e("CreateAircraftActivity", e.getMessage());
                        toast("Error retrieving models for selected manufacturer");
                    }
                });
            }

            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void populateViews(AirMapAircraft aircraft) {
        if (aircraft != null && aircraft.getModel() != null && aircraft.getModel().getManufacturer() != null) {

            List<AirMapAircraftManufacturer> singleAircraft = new ArrayList<>();
            singleAircraft.add(aircraft.getModel().getManufacturer());
            manufacturerSpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, singleAircraft)); //The AirMapAircraftManufacturer's toString takes care of proper representation of data
            manufacturerSpinner.setEnabled(false);

            List<AirMapAircraftModel> singleModel = new ArrayList<>();
            singleModel.add(aircraft.getModel());
            modelSpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, singleModel));
            modelSpinner.setEnabled(false);
            modelSpinner.setVisibility(View.VISIBLE);
            nicknameEditText.setText(aircraft.getNickname());
        }
    }

    private void initializeViews() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        nicknameEditText = (EditText) findViewById(R.id.nickname);
        saveButton = (Button) findViewById(R.id.save);
        manufacturerSpinner = (Spinner) findViewById(R.id.manufacturer);
        modelSpinner = (Spinner) findViewById(R.id.models);
        saveButton.setOnClickListener(this);
        modelSpinner.setVisibility(View.GONE); //Don't want to show the models picker a manufacturer is picked
    }

    @Override
    public void onClick(View v) {
        String nickname = nicknameEditText.getText().toString();

        if (aircraftToEdit != null) {
            aircraftToEdit.setNickname(nickname);
            AirMap.updateAircraft(aircraftToEdit, new AirMapCallback<AirMapAircraft>() {
                @Override
                public void onSuccess(AirMapAircraft response) {
                    toast("Successfully edited aircraft");
                    Intent intent = new Intent();
                    intent.putExtra(AIRCRAFT, response);
                    setResult(RESULT_OK, intent);
                    finish();
                }

                @Override
                public void onError(AirMapException e) {
                    toast(e.getMessage());
                    e.printStackTrace();
                }
            });
        } else {
            AirMapAircraft aircraft = new AirMapAircraft();
            AirMapAircraftModel model = (AirMapAircraftModel) modelSpinner.getSelectedItem();
            aircraft.setModel(model).setNickname(nickname);
            AirMap.createAircraft(aircraft, new AirMapCallback<AirMapAircraft>() {
                @Override
                public void onSuccess(AirMapAircraft response) {
                    toast("Successfully created aircraft");
                    Intent intent = new Intent();
                    intent.putExtra(AIRCRAFT, response);
                    setResult(RESULT_OK, intent);
                    finish();
                }

                @Override
                public void onError(AirMapException e) {
                    toast(e.getMessage());
                    e.printStackTrace();
                }
            });
        }
    }

    private void toast(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(CreateEditAircraftActivity.this, message, Toast.LENGTH_SHORT).show();
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
