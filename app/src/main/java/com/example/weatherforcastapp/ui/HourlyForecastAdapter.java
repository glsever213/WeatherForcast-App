package com.example.weatherforcastapp.ui;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.weatherforcastapp.R;
import com.example.weatherforcastapp.api.WeatherApiIcons;
import com.example.weatherforcastapp.databinding.ItemHourlyForecastPillDarkBinding;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

public final class HourlyForecastAdapter extends RecyclerView.Adapter<HourlyForecastAdapter.VH> {

    public static final class Slot {
        public final String tempLabel;
        public final String timeLabel;
        public final String iconCode;

        public Slot(String tempLabel, String timeLabel, String iconCode) {
            this.tempLabel = tempLabel;
            this.timeLabel = timeLabel;
            this.iconCode = iconCode;
        }
    }

    private final List<Slot> data = new ArrayList<>();
    private final int selectedIndex;

    public HourlyForecastAdapter(List<Slot> slots, int selectedIndex) {
        if (slots != null) data.addAll(slots);
        this.selectedIndex = selectedIndex;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemHourlyForecastPillDarkBinding b = ItemHourlyForecastPillDarkBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new VH(b);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Slot s = data.get(position);
        holder.binding.textHourlyTemp.setText(s.tempLabel);
        holder.binding.textHourlyTime.setText(s.timeLabel);
        int hour = 12;
        try {
            int colon = s.timeLabel.indexOf(':');
            if (colon > 0) hour = Integer.parseInt(s.timeLabel.substring(0, colon));
        } catch (Exception ignored) {
        }
        String url = WeatherApiIcons.url(s.iconCode, hour >= 6 && hour < 18, WeatherApiIcons.SIZE_LIST);
        Glide.with(holder.binding.iconHourly.getContext())
                .load(url)
                .placeholder(R.drawable.ic_weather_placeholder)
                .into(holder.binding.iconHourly);

        MaterialCardView card = holder.binding.getRoot();
        float d = card.getResources().getDisplayMetrics().density;
        boolean sel = position == selectedIndex;
        card.setStrokeWidth(sel ? Math.round(2f * d) : 0);
        card.setStrokeColor(ContextCompat.getColor(card.getContext(), R.color.glass_stroke_on_dark));
        int bg = sel ? 0x33FFFFFF : 0x14FFFFFF;
        card.setCardBackgroundColor(bg);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static final class VH extends RecyclerView.ViewHolder {
        final ItemHourlyForecastPillDarkBinding binding;

        VH(ItemHourlyForecastPillDarkBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
