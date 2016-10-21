package com.airmap.freehand;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import com.mapbox.mapboxsdk.annotations.BaseMarkerOptions;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;

/**
 * Created by Vansh Gandhi on 10/11/16.
 * Copyright © 2016 AirMap, Inc. All rights reserved.
 */

public class DraggableMarkerOptions extends BaseMarkerOptions<DraggableMarker, DraggableMarkerOptions> {

    public DraggableMarkerOptions() {
    }

    private DraggableMarkerOptions(Parcel in) {
        position((LatLng) in.readParcelable(LatLng.class.getClassLoader()));
        snippet(in.readString());
        String iconId = in.readString();
        Bitmap iconBitmap = in.readParcelable(Bitmap.class.getClassLoader());
        Icon icon = IconFactory.recreate(iconId, iconBitmap);
        icon(icon);
        title(in.readString());
    }

    @Override
    public DraggableMarkerOptions getThis() {
        return this;
    }

    @Override
    public DraggableMarker getMarker() {
        return new DraggableMarker(this);
    }

    public static final Parcelable.Creator<DraggableMarkerOptions> CREATOR
            = new Parcelable.Creator<DraggableMarkerOptions>() {
        public DraggableMarkerOptions createFromParcel(Parcel in) {
            return new DraggableMarkerOptions(in);
        }

        public DraggableMarkerOptions[] newArray(int size) {
            return new DraggableMarkerOptions[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeParcelable(position, flags);
        out.writeString(snippet);
        out.writeString(icon.getId());
        out.writeParcelable(icon.getBitmap(), flags);
        out.writeString(title);
    }
}
