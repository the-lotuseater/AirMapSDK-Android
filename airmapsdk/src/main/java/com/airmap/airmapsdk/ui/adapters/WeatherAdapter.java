package com.airmap.airmapsdk.ui.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.airmap.airmapsdk.R;
import com.airmap.airmapsdk.models.AirMapWeatherUpdate;

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
                AirMapWeatherUpdate weather = weatherUpdates.get(position - 1);

                WeatherViewHolder holder = (WeatherViewHolder) viewHolder;

                SimpleDateFormat dateFormat = new SimpleDateFormat("h:mm a");
                holder.timeTextView.setText(dateFormat.format(weather.getTime()));
                holder.conditionTextView.setText(weather.getCondition());

                AirMapWeatherUpdate.Wind wind = weather.getWind();
                //FIXME:
                String speed = wind.getGusting() == 0 ? String.format("%s Kts", wind.getSpeed()) : String.format("%s-%s Kts", wind.getSpeed(), wind.getGusting());
                String windText = String.format("%s°/%s", wind.getHeading(), speed);
                holder.windTextView.setText(windText);

                //FIXME:
                String visibility = String.format("%s mi", weather.getVisibility());
                holder.visibilityTextView.setText(visibility);

                //FIXME:
                String temp = String.format("%s° F", weather.getTemperature());
                holder.tempTextView.setText(temp);

                String precip = String.format("%s%%", weather.getPrecipitation());
                holder.precipTextView.setText(precip);

                String humidity = String.format("%s%%", weather.getHumidity());
                holder.humidityTextView.setText(humidity);
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
        TextView conditionTextView;
        TextView windTextView;
        TextView visibilityTextView;
        TextView tempTextView;
        TextView precipTextView;
        TextView humidityTextView;

        WeatherViewHolder(View itemView) {
            super(itemView);

            timeTextView = (TextView) itemView.findViewById(R.id.time_text_view);
            conditionTextView = (TextView) itemView.findViewById(R.id.condition_text_view);
            windTextView = (TextView) itemView.findViewById(R.id.wind_text_view);
            visibilityTextView = (TextView) itemView.findViewById(R.id.visibility_text_view);
            tempTextView = (TextView) itemView.findViewById(R.id.temp_text_view);
            precipTextView = (TextView) itemView.findViewById(R.id.precip_text_view);
            humidityTextView = (TextView) itemView.findViewById(R.id.humidity_text_view);
        }
    }

    class HeaderViewHolder extends RecyclerView.ViewHolder {

        HeaderViewHolder(View itemView) {
            super(itemView);
        }
    }
}
