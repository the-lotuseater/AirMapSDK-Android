package com.airmap.airmapsdk.ui.views;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.AppCompatButton;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import com.airmap.airmapsdk.R;

public class ToggleButton extends AppCompatButton implements View.OnClickListener {

    private OnClickListener clickListener;

    public ToggleButton(Context context) {
        super(context);

        init();
    }

    public ToggleButton(Context context, AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    public ToggleButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init();
    }


    public void init() {
    }

    @Override
    public void onClick(View v) {
        // toggle this button
        setSelected(true);

        // toggle its siblings to off
        if (getParent() != null && getParent() instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) getParent()).getChildCount(); i++) {
                View child = (((ViewGroup) getParent()).getChildAt(i));
                if (!v.equals(child)) {
                    child.setSelected(false);
                }
            }
        }

        if (clickListener != null) {
            clickListener.onClick(v);
        }
    }

    @Override
    public void setOnClickListener(OnClickListener clickListener) {
        this.clickListener = clickListener;
        super.setOnClickListener(this);
    }

    @Override
    public void setSelected(boolean selected) {
        super.setSelected(selected);

        ViewCompat.setBackgroundTintList(this, ContextCompat.getColorStateList(getContext(), selected ? R.color.colorAccent : R.color.colorPrimaryDark));
    }
}
