package com.airmap.airmapsdk.ui.views;

import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;

/**
 * Created by Vansh Gandhi on 10/18/16.
 * Copyright © 2016 AirMap, Inc. All rights reserved.
 *
 * This class is an ImageView that also acts as a switch
 */

public class ImageViewSwitch extends ImageView {

    private boolean isChecked = false;
    private CompoundButton.OnCheckedChangeListener onCheckedChangeListener;

    public ImageViewSwitch(Context context) {
        super(context);
    }

    public ImageViewSwitch(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ImageViewSwitch(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public ImageViewSwitch(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    //Static initializer. Called after each constructor
    {
        //Set the onClickListener to toggle
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                setChecked(!isChecked);
            }
        });
    }

    public void setChecked(boolean checked) {
        setSelected(checked);
        isChecked = checked;
        if (onCheckedChangeListener != null) {
            onCheckedChangeListener.onCheckedChanged(null, isChecked);
        }
    }

    public void setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener onCheckedChangeListener) {
        this.onCheckedChangeListener = onCheckedChangeListener;
    }
}