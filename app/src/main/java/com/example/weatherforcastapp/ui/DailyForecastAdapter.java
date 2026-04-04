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
        public final String iconCode;

        public Slot(String dateLabel, String tempLabel, String iconCode) {
            this.dateLabel = dateLabel;
            this.tempLabel = tempLabel;
            this.iconCode = iconCode;
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
        holder.binding.textIconNote.setText(
                holder.binding.getRoot().getContext().getString(R.string.slot_openweather_icon_line)
                        + " — "
                        + s.iconCode);
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
}
