package com.airmap.airmapsdk.ui.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.airmap.airmapsdk.R;
import com.airmap.airmapsdk.models.AirMapWeatherUpdate;
import com.airmap.airmapsdk.util.Utils;

import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Created by collin@airmap.com on 6/16/17.
 */

public class WeatherAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = "WeatherAdapter";

    private static final int HEADER_VIEW_TYPE = 1;
    private static final int WEATHER_VIEW_TYPE = 2;

    private List<AirMapWeatherUpdate> weatherUpdates;

    public WeatherAdapter(List<AirMapWeatherUpdate> weatherUpdates) {
        this.weatherUpdates = weatherUpdates;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        switch (viewType) {
            case HEADER_VIEW_TYPE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_weather_header, parent, false);
                return new HeaderViewHolder(view);
            case WEATHER_VIEW_TYPE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_weather_update, parent, false);
                return new WeatherViewHolder(view);
        }

        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        switch (getItemViewType(position)) {
            case HEADER_VIEW_TYPE:
                break;
            case WEATHER_VIEW_TYPE:
                Context context = viewHolder.itemView.getContext();
                boolean useMetric = Utils.useMetric(context);
                AirMapWeatherUpdate weather = weatherUpdates.get(position - 1);

                WeatherViewHolder holder = (WeatherViewHolder) viewHolder;

                //TODO: localization support
                SimpleDateFormat dateFormat = new SimpleDateFormat("h:mm a");
                holder.timeTextView.setText(dateFormat.format(weather.getTime()));

                AirMapWeatherUpdate.Wind wind = weather.getWind();
                int convertedSpeed = useMetric ? Utils.metersPerSecondToKmph(wind.getSpeed()) : Utils.metersPerSecondToMph(wind.getSpeed());
                int convertedGusting = useMetric ? Utils.metersPerSecondToKmph(wind.getGusting()) : Utils.metersPerSecondToMph(wind.getSpeed());
                //TODO: localization support
                String speed;
                if (wind.getGusting() == 0) {
                    speed = String.format("%s %s", convertedSpeed, useMetric ? context.getString(R.string.flight_feature_km_per_hour) : context.getString(R.string.flight_feature_miles_per_hour));
                } else {
                    speed = String.format("%s-%s %s", convertedSpeed, convertedGusting, useMetric ? context.getString(R.string.flight_feature_km_per_hour) : context.getString(R.string.flight_feature_miles_per_hour));
                }
                String windText = String.format("%s°/%s", wind.getHeading(), speed);
                holder.windTextView.setText(windText);

                int visibility = useMetric ? (int) weather.getVisibility() : Utils.kilometersToMiles((int) weather.getVisibility());
                holder.visibilityTextView.setText(context.getString(useMetric ? R.string.kilometers_short : R.string.miles_short, visibility));

                holder.tempTextView.setText(Utils.getTemperatureString(context, weather.getTemperature(), !useMetric));

                holder.dewPointTextView.setText(Utils.getTemperatureString(context, weather.getDewPoint(), !useMetric));

                //TODO: localization support
                String pressure = String.format("%s Hg", weather.getMslp());
                holder.pressureTextView.setText(pressure);
                break;
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return HEADER_VIEW_TYPE;
        }

        return WEATHER_VIEW_TYPE;
    }

    @Override
    public int getItemCount() {
        return weatherUpdates != null ? weatherUpdates.size() + 1 : 0;
    }

    class WeatherViewHolder extends RecyclerView.ViewHolder {
        TextView timeTextView;
        TextView windTextView;
        TextView visibilityTextView;
        TextView tempTextView;
        TextView dewPointTextView;
        TextView pressureTextView;

        WeatherViewHolder(View itemView) {
            super(itemView);

            timeTextView = (TextView) itemView.findViewById(R.id.time_text_view);
            windTextView = (TextView) itemView.findViewById(R.id.wind_text_view);
            visibilityTextView = (TextView) itemView.findViewById(R.id.visibility_text_view);
            tempTextView = (TextView) itemView.findViewById(R.id.temp_text_view);
            dewPointTextView = (TextView) itemView.findViewById(R.id.dew_point_text_view);
            pressureTextView = (TextView) itemView.findViewById(R.id.pressure_text_view);
        }
    }

    class HeaderViewHolder extends RecyclerView.ViewHolder {

        HeaderViewHolder(View itemView) {
            super(itemView);
        }
    }
}
