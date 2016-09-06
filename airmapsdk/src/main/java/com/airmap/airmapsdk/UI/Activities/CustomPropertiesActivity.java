package com.airmap.airmapsdk.ui.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.airmap.airmapsdk.models.permits.AirMapAvailablePermit;
import com.airmap.airmapsdk.models.permits.AirMapPilotPermitCustomProperty;
import com.airmap.airmapsdk.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CustomPropertiesActivity extends AppCompatActivity {

    public static String PERMIT = "permit";

    private LinearLayout customPropertiesLayout;

    private List<AirMapPilotPermitCustomProperty> customProperties;
    private AirMapAvailablePermit permit;
    private List<Pair<AirMapPilotPermitCustomProperty, TextInputEditText>> pairs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.airmap_activity_custom_properties);
        permit = (AirMapAvailablePermit) getIntent().getSerializableExtra(PERMIT);
        customProperties = permit.getCustomProperties();
        initializeViews();
        initializeCustomProperties();
    }

    private void initializeViews() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //noinspection ConstantConditions
        getSupportActionBar().setTitle(permit.getName());
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        TextView descriptionTextView = (TextView) findViewById(R.id.description_text);
        TextView validityTextView = (TextView) findViewById(R.id.validity);
//        TextView priceTextView = (TextView) findViewById(R.id.price);
        customPropertiesLayout = (LinearLayout) findViewById(R.id.custom_properties_container);
        Button selectPermitButton = (Button) findViewById(R.id.select_permit_button);

        descriptionTextView.setText(permit.getDescription());
//        priceTextView.setText(permit.getPrice());
        if (permit.getValidFor() != -1) {
            validityTextView.setText(String.format(Locale.US, "%d minutes", permit.getValidFor()));
        } else if (permit.getValidUntil() != null) {
            SimpleDateFormat format = new SimpleDateFormat("MM/dd/yy h:mm a", Locale.US);
            validityTextView.setText(format.format(permit.getValidUntil()));
        } else if (permit.isSingleUse()) {
            validityTextView.setText("Single Use");
        }

        selectPermitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (allRequiredFieldsFilled()) {
                    permit.setCustomProperties(getUpdatedCustomProperties());
                    Intent data = new Intent();
                    data.putExtra(PERMIT, permit);
                    setResult(Activity.RESULT_OK, data);
                    finish();
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

    private void initializeCustomProperties() {
        pairs = new ArrayList<>();
        if (customProperties == null) {
            return;
        }
        for (AirMapPilotPermitCustomProperty property : customProperties) {
            switch (property.getType()) {
                case Text:
                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

                    TextInputEditText editText = new TextInputEditText(this);
                    editText.setHint(property.getLabel());
                    editText.setMaxLines(1);
                    editText.setInputType(InputType.TYPE_CLASS_TEXT);
                    editText.setLayoutParams(layoutParams);

                    TextInputLayout textInputLayout = new TextInputLayout(this);
                    textInputLayout.setLayoutParams(layoutParams);
                    textInputLayout.addView(editText);
                    if (property.getValue() != null) { //Will populate with data if it exists
                        editText.setText(property.getValue());
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
                editText.setError("This is a required field");
            }
        }
        return toggle;
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
