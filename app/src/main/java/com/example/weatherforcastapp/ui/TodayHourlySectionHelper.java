package com.example.weatherforcastapp.ui;

import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.weatherforcastapp.R;
import com.example.weatherforcastapp.api.WeatherApiIcons;
import com.example.weatherforcastapp.databinding.WidgetTodayHourlyBlockBinding;
import com.example.weatherforcastapp.model.api.ConditionDto;
import com.example.weatherforcastapp.model.api.HourItemDto;

import eightbitlab.com.blurview.BlurTarget;
import eightbitlab.com.blurview.BlurView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Khối "Hôm nay" (RecyclerView ngang) dùng chung cho {@link com.example.weatherforcastapp.FiveDayForecastActivity}
 * và {@link com.example.weatherforcastapp.PreviewActivity}.
 */
public final class TodayHourlySectionHelper {

    private TodayHourlySectionHelper() {
    }

    public static void bindRecycler(@NonNull RecyclerView recyclerView) {
        List<HourlyForecastAdapter.Slot> hourly = new ArrayList<>();
        hourly.add(new HourlyForecastAdapter.Slot("29°", "15:00", "116"));
        hourly.add(new HourlyForecastAdapter.Slot("26°", "16:00", "119"));
        hourly.add(new HourlyForecastAdapter.Slot("24°", "17:00", "122"));
        hourly.add(new HourlyForecastAdapter.Slot("23°", "18:00", "118"));
        hourly.add(new HourlyForecastAdapter.Slot("22°", "19:00", "118"));
        recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext(), LinearLayoutManager.HORIZONTAL, false));
        recyclerView.setAdapter(new HourlyForecastAdapter(hourly, 2));
    }

    /**
     * Glassmorphism: BlurView 3.x cần {@link BlurTarget} trong layout (xem {@code activity_five_day_forecast},
     * {@code preview_tab_page_hourly}). Không có target → fallback trong suốt giả.
     */
    public static void setupBlur(@NonNull AppCompatActivity activity, @NonNull BlurView blurView) {
        BlurTarget target = activity.findViewById(R.id.blurTargetRoot);
        try {
            Drawable windowBackground = activity.getWindow().getDecorView().getBackground();
            if (target != null) {
                blurView.setupWith(target)
                        .setFrameClearDrawable(windowBackground)
                        .setBlurRadius(20f);
                blurView.setOverlayColor(0x22FFFFFF);
            } else {
                blurView.setOverlayColor(0);
                blurView.setBackgroundColor(0x12FFFFFF);
            }
        } catch (Throwable ignored) {
            blurView.setOverlayColor(0);
            blurView.setBackgroundColor(0x12FFFFFF);
        }
    }

    public static void bindHeader(@NonNull WidgetTodayHourlyBlockBinding binding, @NonNull String dateLine) {
        binding.textTodaySectionTitle.setText(com.example.weatherforcastapp.R.string.forecast_today_section);
        binding.textTodayDateLine.setText(dateLine);
    }

    public static void bindRecyclerFromApi(@NonNull RecyclerView recyclerView, @Nullable List<HourItemDto> hours, int currentHour) {
        List<HourlyForecastAdapter.Slot> slots = new ArrayList<>();
        int selectedIndex = 0;

        if (hours != null && !hours.isEmpty()) {
            int bestDiff = Integer.MAX_VALUE;

            for (int i = 0; i < hours.size(); i++) {
                HourItemDto h = hours.get(i);
                if (h == null) continue;

                String timeLabel = "—";
                int hourOfSlot = 0;
                String rawTime = h.getTime();
                if (rawTime != null) {
                    int spaceIdx = rawTime.indexOf(' ');
                    if (spaceIdx >= 0 && spaceIdx + 1 < rawTime.length()) {
                        timeLabel = rawTime.substring(spaceIdx + 1); // "HH:mm"
                    }
                    try {
                        int colonIdx = timeLabel.indexOf(':');
                        if (colonIdx > 0) {
                            hourOfSlot = Integer.parseInt(timeLabel.substring(0, colonIdx));
                        }
                    } catch (NumberFormatException ignored) {
                    }
                }

                String tempLabel = h.getTempC() != null
                        ? String.format(Locale.getDefault(), "%.0f°", h.getTempC())
                        : "—";

                String iconCode = null;
                ConditionDto cond = h.getCondition();
                if (cond != null) {
                    iconCode = cond.getIcon();
                }

                slots.add(new HourlyForecastAdapter.Slot(tempLabel, timeLabel, iconCode != null ? iconCode : "116"));

                if (currentHour >= 0) {
                    int diff = Math.abs(hourOfSlot - currentHour);
                    if (diff < bestDiff) {
                        bestDiff = diff;
                        selectedIndex = slots.size() - 1;
                    }
                }
            }
        }
        recyclerView.setLayoutManager(new LinearLayoutManager(
                recyclerView.getContext(), LinearLayoutManager.HORIZONTAL, false));
        recyclerView.setAdapter(new HourlyForecastAdapter(slots, selectedIndex));

        final int scrollTo = selectedIndex;
        recyclerView.post(() -> recyclerView.scrollToPosition(scrollTo));
    }
}

