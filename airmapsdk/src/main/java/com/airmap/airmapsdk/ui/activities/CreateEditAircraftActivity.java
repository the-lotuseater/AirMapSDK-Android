package com.airmap.airmapsdk.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.airmap.airmapsdk.AirMapException;
import com.airmap.airmapsdk.Analytics;
import com.airmap.airmapsdk.models.aircraft.AirMapAircraft;
import com.airmap.airmapsdk.models.aircraft.AirMapAircraftManufacturer;
import com.airmap.airmapsdk.models.aircraft.AirMapAircraftModel;
import com.airmap.airmapsdk.networking.callbacks.AirMapCallback;
import com.airmap.airmapsdk.networking.services.AirMap;
import com.airmap.airmapsdk.R;
import com.airmap.airmapsdk.util.AirMapConstants;

import java.util.ArrayList;
import java.util.List;

public class CreateEditAircraftActivity extends AppCompatActivity implements View.OnClickListener {

    private Toolbar toolbar;
    private TextInputLayout nicknameTextInputLayout;
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
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        int id;
        if (getIntent() != null && getIntent().getSerializableExtra(AirMapConstants.AIRCRAFT_EXTRA) != null) {
            aircraftToEdit = (AirMapAircraft) getIntent().getSerializableExtra(AirMapConstants.AIRCRAFT_EXTRA);
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
                response.add(0, new AirMapAircraftManufacturer().setId("select_manufacturer").setName(getString(R.string.select_manufacturer)));
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        manufacturerSpinner.setAdapter(new ArrayAdapter<>(CreateEditAircraftActivity.this, android.R.layout.simple_spinner_dropdown_item, response)); //The AirMapAircraftManufacturer's toString takes care of proper representation of data
                    }
                });
            }

            @Override
            public void onError(AirMapException e) {
                toast(getString(R.string.error_getting_mfrs));
            }
        });
        modelSpinner.setVisibility(View.GONE);
        manufacturerSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                AirMapAircraftManufacturer manufacturer = (AirMapAircraftManufacturer) parent.getAdapter().getItem(pos);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        modelSpinner.setVisibility(View.GONE);
                    }
                });
                if (manufacturer.getId().equals("select_manufacturer")) {
                    return;
                }

                if (aircraftToEdit == null) {
                    Analytics.logEvent(Analytics.Page.MANUFACTURERS_CREATE_AIRCRAFT, Analytics.Action.tap, Analytics.Label.SELECT_MANUFACTURER);
                }

                AirMap.getModels(manufacturer.getId(), new AirMapCallback<List<AirMapAircraftModel>>() {
                    @Override
                    public void onSuccess(final List<AirMapAircraftModel> response) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                response.add(0, new AirMapAircraftModel().setModelId("select_model").setName(getString(R.string.select_model)).setManufacturer(new AirMapAircraftManufacturer().setName("")));
                                modelSpinner.setAdapter(new ArrayAdapter<>(CreateEditAircraftActivity.this, android.R.layout.simple_spinner_dropdown_item, response));
                                modelSpinner.setVisibility(View.VISIBLE);
                                modelSpinner.setOnTouchListener(new View.OnTouchListener() {
                                    @Override
                                    public boolean onTouch(View view, MotionEvent motionEvent) {
                                        hideKeyboard();

                                        if (aircraftToEdit == null) {
                                            Analytics.logEvent(Analytics.Page.CREATE_AIRCRAFT, Analytics.Action.tap, Analytics.Label.SELECT_MODEL);
                                        }

                                        return false;
                                    }
                                });
                                modelSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                    @Override
                                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                                        if (aircraftToEdit == null) {
                                            Analytics.logEvent(Analytics.Page.MODEL_CREATE_AIRCRAFT, Analytics.Action.tap, Analytics.Label.SELECT_MODEL);
                                        }
                                    }

                                    @Override
                                    public void onNothingSelected(AdapterView<?> adapterView) {

                                    }
                                });
                            }
                        });
                    }

                    @Override
                    public void onError(AirMapException e) {
                        e.printStackTrace();
                        Log.e("CreateAircraftActivity", e.getMessage());
                        toast(getString(R.string.error_models_for_mfrs));
                    }
                });
            }

            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        manufacturerSpinner.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                hideKeyboard();

                if (aircraftToEdit == null) {
                    Analytics.logEvent(Analytics.Page.CREATE_AIRCRAFT, Analytics.Action.tap, Analytics.Label.SELECT_MANUFACTURER);
                }

                return false;
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
        toolbar = findViewById(R.id.toolbar);
        nicknameTextInputLayout = findViewById(R.id.nickname_text_input_layout);
        nicknameEditText = findViewById(R.id.nickname);
        saveButton = findViewById(R.id.save);
        manufacturerSpinner = findViewById(R.id.manufacturer);
        modelSpinner = findViewById(R.id.models);
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
                    Intent intent = new Intent();
                    intent.putExtra(AirMapConstants.AIRCRAFT_EXTRA, aircraftToEdit);
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
            Analytics.logEvent(Analytics.Page.CREATE_AIRCRAFT, Analytics.Action.tap, Analytics.Label.SAVE);

            boolean incomplete = false;
            if (nickname.isEmpty()) {
                nicknameTextInputLayout.setHintTextAppearance(R.style.AppTheme_TextErrorAppearance);
                incomplete = true;
            } else {
                nicknameTextInputLayout.setHintTextAppearance(R.style.AppTheme_TextHintAppearance);
            }

            AirMapAircraft aircraft = new AirMapAircraft();
            AirMapAircraftModel model = (AirMapAircraftModel) modelSpinner.getSelectedItem();

            if (model == null || model.getModelId().equals("select_model")) {
                incomplete = true;
                toast(getString(R.string.select_model));
            }

            // user needs
            if (incomplete) {
                return;
            }

            aircraft.setModel(model).setNickname(nickname);
            AirMap.createAircraft(aircraft, new AirMapCallback<AirMapAircraft>() {
                @Override
                public void onSuccess(AirMapAircraft response) {
                    Intent intent = new Intent();
                    intent.putExtra(AirMapConstants.AIRCRAFT_EXTRA, response);
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

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(nicknameEditText.getWindowToken(), 0);
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

    @Override
    public void onBackPressed() {
        if (aircraftToEdit == null) {
            Analytics.logEvent(Analytics.Page.CREATE_AIRCRAFT, Analytics.Action.tap, Analytics.Label.CANCEL);
        }

        super.onBackPressed();
    }
}
