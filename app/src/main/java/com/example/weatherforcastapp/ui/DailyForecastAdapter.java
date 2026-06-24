package com.example.weatherforcastapp.ui;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.weatherforcastapp.R;
import com.example.weatherforcastapp.api.WeatherApiIcons;
import com.example.weatherforcastapp.databinding.ItemDailyForecastLineBinding;

import java.util.ArrayList;
import java.util.List;

public final class DailyForecastAdapter extends RecyclerView.Adapter<DailyForecastAdapter.VH> {

    public static final class Slot {
        public final String dateLabel;
        public final String tempLabel;
        public final String condition;
        public final String iconCode;
        public final String rainChance;
        public final String feelsLike;
        public final String wind;
        public final String humidity;
        public final String uv;

        public Slot(String dateLabel, String tempLabel, String condition, String iconCode, String rainChance, String feelsLike, String wind, String humidity, String uv) {
            this.dateLabel = dateLabel;
            this.tempLabel = tempLabel;
            this.condition = condition;
            this.iconCode = iconCode;
            this.rainChance = rainChance;
            this.feelsLike = feelsLike;
            this.wind = wind;
            this.humidity = humidity;
            this.uv = uv;
        }
    }

    private final List<Slot> data = new ArrayList<>();

    public DailyForecastAdapter(List<Slot> slots) {
        if (slots != null) data.addAll(slots);
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemDailyForecastLineBinding b = ItemDailyForecastLineBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new VH(b);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Slot s = data.get(position);
        holder.binding.textDailyDate.setText(s.dateLabel);
        holder.binding.textDailyTemp.setText(s.tempLabel);
        String url = WeatherApiIcons.url(s.iconCode, true, WeatherApiIcons.SIZE_LIST);
        Glide.with(holder.binding.iconDaily.getContext())
                .load(url)
                .placeholder(R.drawable.ic_weather_placeholder)
                .into(holder.binding.iconDaily);

        holder.binding.textIconNote.setText(s.condition);
        holder.binding.textRainChance.setText(s.rainChance);
        holder.binding.textFeelsLike.setText(s.feelsLike);
        holder.binding.textWind.setText(s.wind);
        holder.binding.textHumidity.setText(s.humidity);
        holder.binding.textUv.setText(s.uv);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static final class VH extends RecyclerView.ViewHolder {
        final ItemDailyForecastLineBinding binding;

        VH(ItemDailyForecastLineBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
    public void setRows(List<Slot> slots) {
        data.clear();
        if (slots != null) data.addAll(slots);
        notifyDataSetChanged();
    }
}
