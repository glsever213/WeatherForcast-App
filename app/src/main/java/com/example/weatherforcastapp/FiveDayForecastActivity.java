package com.example.weatherforcastapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.weatherforcastapp.databinding.ActivityFiveDayForecastBinding;
import com.example.weatherforcastapp.databinding.WidgetTodayHourlyBlockBinding;
import com.example.weatherforcastapp.location.LocationContract;
import com.example.weatherforcastapp.ui.DailyForecastAdapter;
import com.example.weatherforcastapp.ui.Forecast5dSamples;
import com.example.weatherforcastapp.ui.TodayHourlySectionHelper;
import com.example.weatherforcastapp.util.ActivityTransitions;

/**
 * Dự báo 5 ngày — nền đen, khối Today (giờ) + bảng 3 cột dùng chung với Preview.
 */
public class FiveDayForecastActivity extends AppCompatActivity {

    private ActivityFiveDayForecastBinding binding;

    public static void start(Activity from, double lat, double lon, @Nullable String name) {
        Intent i = new Intent(from, FiveDayForecastActivity.class);
        LocationContract.putLocation(i, lat, lon, name);
        from.startActivity(i);
        ActivityTransitions.slideIn(from);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFiveDayForecastBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        @SuppressWarnings("unused")
        double lat = LocationContract.readLat(getIntent(), 0);
        @SuppressWarnings("unused")
        double lon = LocationContract.readLon(getIntent(), 0);

        WidgetTodayHourlyBlockBinding wb = binding.blockTodayHourly;
        TodayHourlySectionHelper.bindHeader(wb, getString(R.string.forecast_today_date_placeholder));
        TodayHourlySectionHelper.bindRecycler(wb.recyclerTodayHourly);
        TodayHourlySectionHelper.setupBlur(this, wb.blurTodayHourly);

        binding.buttonBackForecast.setOnClickListener(v -> {
            finish();
            ActivityTransitions.slideOut(this);
        });
        binding.buttonSettingsForecast.setOnClickListener(v ->
                Toast.makeText(this, R.string.settings, Toast.LENGTH_SHORT).show());

        binding.blockForecast5dTable.recyclerForecast5dTable.setLayoutManager(new LinearLayoutManager(this));
        binding.blockForecast5dTable.recyclerForecast5dTable.setAdapter(
                new DailyForecastAdapter(Forecast5dSamples.defaultSlots()));
    }
}
