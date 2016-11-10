package com.airmap.airmapsdk.ui.views;

import android.content.Context;
import android.os.Build;
import android.support.annotation.DrawableRes;
import android.support.annotation.IntegerRes;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.airmap.airmapsdk.R;

/**
 * Created by collinvance on 11/7/16.
 */

public class PermitRadioButton extends FrameLayout {

    private ImageView iconImageView;
    private TextView titleTextView;
    private TextView descriptionTextView;
    private RadioButton radioButton;

    public PermitRadioButton(Context context) {
        super(context);

        init();
    }

    public PermitRadioButton(Context context, AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    public PermitRadioButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public PermitRadioButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        init();
    }

    private void init() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.permit_radio_button, this, true);
        iconImageView = (ImageView) view.findViewById(R.id.icon_image_view);
        titleTextView = (TextView) view.findViewById(R.id.title_text_view);
        descriptionTextView = (TextView) view.findViewById(R.id.description_text_view);
        radioButton = (RadioButton) view.findViewById(R.id.radio_button);
    }

    public void setImageResource(@DrawableRes int resId) {
        iconImageView.setImageResource(resId);
    }

    public void setTitle(String title) {
        titleTextView.setText(title);
    }

    public void setDescription(String description) {
        descriptionTextView.setText(description);
    }

    public void setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener listener) {
        radioButton.setOnCheckedChangeListener(listener);
    }

    public void setChecked(boolean checked) {
        radioButton.setChecked(checked);
    }

    public boolean isChecked() {
        return radioButton.isChecked();
    }

    public void setOnCheckedChangeWidgetListener(CompoundButton.OnCheckedChangeListener listener) {
        radioButton.setOnCheckedChangeListener(listener);
    }
}
