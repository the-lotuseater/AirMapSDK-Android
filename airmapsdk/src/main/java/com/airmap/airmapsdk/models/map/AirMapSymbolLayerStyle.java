package com.airmap.airmapsdk.models.map;

import com.mapbox.mapboxsdk.style.layers.Layer;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;

import org.json.JSONObject;

/**
 * Created by collin@airmap.com on 4/17/17.
 */

public class AirMapSymbolLayerStyle extends AirMapLayerStyle {

    public final String iconImage;
    public final boolean iconAllowOverlap;
    public final boolean iconKeepUpright;

    protected AirMapSymbolLayerStyle(JSONObject json) {
        super(json);

        JSONObject layoutJson = json.optJSONObject("layout");
        if (layoutJson != null) {
            iconImage = optString(layoutJson, "icon-image");
            iconAllowOverlap = layoutJson.optBoolean("icon-allow-overlap");
            iconKeepUpright = layoutJson.optBoolean("icon-keep-upright");
        } else {
            iconImage = null;
            iconAllowOverlap = false;
            iconKeepUpright = false;
        }
    }

    public boolean isBackgroundStyle() {
        return id.contains("background");
    }

    @Override
    public SymbolLayer toMapboxLayer(Layer layerToClone, String sourceId) {
        SymbolLayer layer = new SymbolLayer(id + "|" + sourceId + "|new", sourceId);
        layer.setSourceLayer(sourceId + "_" + sourceLayer);
        layer.setProperties(PropertyFactory.iconImage(iconImage));
        layer.setProperties(PropertyFactory.iconAllowOverlap(iconAllowOverlap));
        layer.setProperties(PropertyFactory.iconKeepUpright(iconKeepUpright));

        if (filter != null) {
            layer.setFilter(filter);
        }

        return layer;
    }
}
