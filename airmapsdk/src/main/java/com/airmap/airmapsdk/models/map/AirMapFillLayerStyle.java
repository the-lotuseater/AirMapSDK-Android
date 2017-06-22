package com.airmap.airmapsdk.models.map;

import android.util.Log;

import com.mapbox.mapboxsdk.style.layers.FillLayer;
import com.mapbox.mapboxsdk.style.layers.Layer;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;

import org.json.JSONObject;

/**
 * Created by collin@airmap.com on 4/17/17.
 */

public class AirMapFillLayerStyle extends AirMapLayerStyle {

    public final float fillOpacity;
    public final String fillColor;

    protected AirMapFillLayerStyle(JSONObject json) {
        super(json);

        JSONObject paintJson = json.optJSONObject("paint");
        if (paintJson != null) {
            fillOpacity = (float) paintJson.optDouble("fill-opacity");
            fillColor = optString(paintJson, "fill-color");
        } else {
            fillOpacity = 0;
            fillColor = "#000000";
        }
    }

    public boolean isBackgroundStyle() {
        return id.contains("background");
    }

    @Override
    public FillLayer toMapboxLayer(Layer layerToClone, String sourceId) {
        FillLayer fillLayer = new FillLayer(id + "|" + sourceId + "|new", sourceId);
        fillLayer.setSourceLayer(sourceId + "_" + sourceLayer);
        fillLayer.setProperties(PropertyFactory.fillOpacity(fillOpacity));
        fillLayer.setProperties(PropertyFactory.fillColor(fillColor));

        if (filter != null) {
            fillLayer.setFilter(filter);
        }

        return fillLayer;
    }
}
