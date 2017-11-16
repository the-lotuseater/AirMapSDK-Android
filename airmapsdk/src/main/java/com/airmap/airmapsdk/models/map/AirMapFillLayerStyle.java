package com.airmap.airmapsdk.models.map;

import android.text.TextUtils;
import android.util.Log;

import com.mapbox.mapboxsdk.style.functions.Function;
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
    public final Function fillColorFunction;
    public final String fillOutlineColor;

    protected AirMapFillLayerStyle(JSONObject json) {
        super(json);

        JSONObject paintJson = json.optJSONObject("paint");
        if (paintJson != null) {
            fillOpacity = (float) paintJson.optDouble("fill-opacity");

            JSONObject fillColorJson = paintJson.optJSONObject("fill-color");
            if (fillColorJson != null) {
                fillColorFunction = getFillColorFunction(fillColorJson);
                fillColor = "#000000";
            } else {
                fillColor = optString(paintJson, "fill-color");
                fillColorFunction = null;
            }

            fillOutlineColor = optString(paintJson, "fill-outline-color");
        } else {
            fillOpacity = 0;
            fillColor = "#000000";
            fillColorFunction = null;
            fillOutlineColor = "#000000";
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

        if (!TextUtils.isEmpty(fillOutlineColor)) {
            fillLayer.setProperties(PropertyFactory.fillOutlineColor(fillOutlineColor));
        }

        if (fillColorFunction != null) {
            fillLayer.setProperties(PropertyFactory.fillColor(fillColorFunction));
        } else {
            fillLayer.setProperties(PropertyFactory.fillColor(fillColor));
        }

        if (filter != null) {
            fillLayer.setFilter(filter);
        }

        return fillLayer;
    }
}
