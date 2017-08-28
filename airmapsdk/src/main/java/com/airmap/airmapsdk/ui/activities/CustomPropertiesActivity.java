package com.airmap.airmapsdk.ui.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.airmap.airmapsdk.AirMapException;
import com.airmap.airmapsdk.AirMapLog;
import com.airmap.airmapsdk.Analytics;
import com.airmap.airmapsdk.R;
import com.airmap.airmapsdk.models.permits.AirMapAvailablePermit;
import com.airmap.airmapsdk.models.permits.AirMapPilotPermit;
import com.airmap.airmapsdk.models.permits.AirMapPilotPermitCustomProperty;
import com.airmap.airmapsdk.networking.callbacks.AirMapCallback;
import com.airmap.airmapsdk.networking.services.AirMap;
import com.airmap.airmapsdk.util.AirMapConstants;
import com.airmap.airmapsdk.util.Utils;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;

public class CustomPropertiesActivity extends AppCompatActivity {

    private LinearLayout customPropertiesLayout;
    private FrameLayout progressBarContainer;
    private Button selectPermitButton;

    private List<AirMapPilotPermitCustomProperty> customProperties;
    private AirMapAvailablePermit availablePermit;
    private AirMapPilotPermit pilotPermit;
    private List<Pair<AirMapPilotPermitCustomProperty, TextInputEditText>> pairs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.airmap_activity_custom_properties);
        availablePermit = (AirMapAvailablePermit) getIntent().getSerializableExtra(AirMapConstants.AVAILABLE_PERMIT_EXTRA);
        pilotPermit = (AirMapPilotPermit) getIntent().getSerializableExtra(AirMapConstants.PERMIT_WALLET_EXTRA);
        customProperties = availablePermit.getCustomProperties();

        initializeViews();

        if (pilotPermit == null) {
            AirMap.getPermit(availablePermit.getId(), new AirMapCallback<List<AirMapAvailablePermit>>() { //So that we can get other information about the availablePermit, such as its name
                @Override
                public void onSuccess(final List<AirMapAvailablePermit> response) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (response != null && !response.isEmpty()) {
                                availablePermit = response.get(0);
                                customProperties = availablePermit.getCustomProperties();
                                initializeViews();
                                initializeCustomProperties(true);
                            }
                            hideProgressBar();
                        }
                    });
                }

                @Override
                public void onError(AirMapException e) {
                    e.printStackTrace();
                    AirMapLog.e("PermitsAdapter", e.getMessage());
                    hideProgressBar();
                }
            });
        } else {
            customProperties = pilotPermit.getCustomProperties();
            initializeCustomProperties(false);
            hideProgressBar();
        }
    }

    private void initializeViews() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //noinspection ConstantConditions
        getSupportActionBar().setTitle(availablePermit.getName());
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        TextView descriptionTextView = (TextView) findViewById(R.id.description_text);
        TextView validityTextView = (TextView) findViewById(R.id.validity);
        TextView priceTextView = (TextView) findViewById(R.id.price);
        customPropertiesLayout = (LinearLayout) findViewById(R.id.custom_properties_container);
        selectPermitButton = (Button) findViewById(R.id.select_permit_button);

        progressBarContainer = (FrameLayout) findViewById(R.id.progress_bar_container);

        descriptionTextView.setText(availablePermit.getDescription());
        priceTextView.setText(availablePermit.getPrice() == 0 ? getString(R.string.free) : Utils.getPriceText(availablePermit.getPrice()));
        if (availablePermit.isSingleUse()) {
            validityTextView.setText(R.string.single_use);
        } else if (availablePermit.getValidFor() > 0) {
            if (availablePermit.getValidFor() >= 60) {
                validityTextView.setText(getString(R.string.validity_hours, availablePermit.getValidFor() / 60));
            } else {
                validityTextView.setText(getString(R.string.validity_minutes, availablePermit.getValidFor()));
            }
        } else if (availablePermit.getValidUntil() != null) {
            DateFormat format = Utils.getDateTimeFormat();
            validityTextView.setText(format.format(availablePermit.getValidUntil()));
        }

        selectPermitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Analytics.logEvent(Analytics.Page.PERMIT_DETAILS, Analytics.Action.tap, Analytics.Label.SELECT_PERMIT);

                if (allRequiredFieldsFilled()) {
                    availablePermit.setCustomProperties(getUpdatedCustomProperties());
                    Intent data = new Intent();
                    data.putExtra(AirMapConstants.AVAILABLE_PERMIT_EXTRA, availablePermit);
                    setResult(Activity.RESULT_OK, data);
                    finish();
                } else {
                    Toast.makeText(CustomPropertiesActivity.this, R.string.required_field_must_be_complete, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    private ArrayList<AirMapPilotPermitCustomProperty> getUpdatedCustomProperties() {
        ArrayList<AirMapPilotPermitCustomProperty> updatedProperties = new ArrayList<>();
        for (Pair<AirMapPilotPermitCustomProperty, TextInputEditText> pair : pairs) {
            AirMapPilotPermitCustomProperty property = pair.first;
            TextInputEditText editText = pair.second;
            property.setValue(editText.getText().toString());
            updatedProperties.add(property);
        }
        return updatedProperties;
    }

    private void initializeCustomProperties(boolean editable) {
        pairs = new ArrayList<>();
        if (customProperties == null) {
            return;
        }
        for (AirMapPilotPermitCustomProperty property : customProperties) {
            switch (property.getType()) {
                case Text:
                    TextInputLayout textInputLayout = (TextInputLayout) LayoutInflater.from(this).inflate(R.layout.custom_property_edit_text, customPropertiesLayout, false);
                    TextInputEditText editText = (TextInputEditText) textInputLayout.findViewById(R.id.edit_text);
                    textInputLayout.setHint(property.getLabel() + (property.isRequired() && property.getLabel() != null && !property.getLabel().toLowerCase().contains("require") ? "*" : ""));
                    if (property.getValue() != null) { //Will populate with data if it exists
                        editText.setText(property.getValue());
                    }

                    if (property.getLabel() != null && (property.getLabel().toLowerCase().contains("email") || property.getLabel().toLowerCase().contains("e-mail"))) {
                        editText.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
                    }

                    editText.setEnabled(editable);

                    if (!editable) {
                        editText.setTextColor(ContextCompat.getColor(this, android.R.color.white));
                        editText.setHintTextColor(ContextCompat.getColor(this, R.color.colorLightGray));
                        textInputLayout.setSelected(false);
                        textInputLayout.setEnabled(false);
                        editText.setSelected(false);
                        editText.clearFocus();
                        selectPermitButton.requestFocus();
                    }

                    customPropertiesLayout.addView(textInputLayout);

                    pairs.add(new Pair<>(property, editText));
            }
        }
    }

    private boolean allRequiredFieldsFilled() {
        boolean toggle = true;
        for (Pair<AirMapPilotPermitCustomProperty, TextInputEditText> pair : pairs) {
            AirMapPilotPermitCustomProperty property = pair.first;
            TextInputEditText editText = pair.second;
            if (property.isRequired() && editText.getText().length() == 0) {
                toggle = false;
                editText.setError(getString(R.string.required_field));
            }
        }
        return toggle;
    }

    private void hideProgressBar() {
        progressBarContainer.post(new Runnable() {
            @Override
            public void run() {
                progressBarContainer.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onBackPressed() {
        Analytics.logEvent(Analytics.Page.PERMIT_DETAILS, Analytics.Action.tap, Analytics.Label.CANCEL);
        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Analytics.logEvent(Analytics.Page.PERMIT_DETAILS, Analytics.Action.tap, Analytics.Label.CANCEL);
                return false;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
