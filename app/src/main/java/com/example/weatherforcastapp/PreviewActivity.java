package com.example.weatherforcastapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.weatherforcastapp.databinding.ActivityPreviewBinding;
import com.example.weatherforcastapp.databinding.WidgetTodayHourlyBlockBinding;
import com.example.weatherforcastapp.location.LocationContract;
import com.example.weatherforcastapp.model.SavedLocation;
import com.example.weatherforcastapp.prefs.WeatherPreferences;
import com.example.weatherforcastapp.ui.TodayHourlySectionHelper;
import com.example.weatherforcastapp.util.ActivityTransitions;

/**
 * Xem trước: khối **Hôm nay** (theo giờ) như màn dự báo 5 ngày; FAB “Thêm vào trang home” giữ nguyên dưới.
 */
public class PreviewActivity extends AppCompatActivity {

    private ActivityPreviewBinding binding;
    private String cityName;
    private double lat;
    private double lon;
    private int flowMode;

    public static void start(Context context, String name, double lat, double lon, int flowMode) {
        Intent i = new Intent(context, PreviewActivity.class);
        LocationContract.putLocation(i, lat, lon, name);
        LocationContract.putFlowMode(i, flowMode);
        context.startActivity(i);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPreviewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        lat = LocationContract.readLat(getIntent(), 0);
        lon = LocationContract.readLon(getIntent(), 0);
        cityName = LocationContract.readDisplayName(getIntent());
        flowMode = LocationContract.readFlowMode(getIntent(), LocationContract.FLOW_MANAGEMENT);

        binding.textPreviewCity.setText(cityName != null && !cityName.isEmpty() ? cityName : "—");

        WidgetTodayHourlyBlockBinding hourly = binding.blockTodayHourlyPreview;
        TodayHourlySectionHelper.bindHeader(hourly, getString(R.string.forecast_today_date_placeholder));
        TodayHourlySectionHelper.bindRecycler(hourly.recyclerTodayHourly);
        TodayHourlySectionHelper.setupBlur(this, hourly.blurTodayHourly);

        binding.buttonBackPreview.setOnClickListener(v -> {
            finish();
            ActivityTransitions.slideOut(this);
        });

        binding.fabAddToHome.setOnClickListener(v -> {
            WeatherPreferences prefs = WeatherPreferences.get(this);
            String name = cityName != null ? cityName : "—";
            SavedLocation loc = new SavedLocation(name, lat, lon, getString(R.string.frame_note));

            if (flowMode == LocationContract.FLOW_ONBOARDING) {
                prefs.setCurrentLocation(lat, lon, name);
                if (!prefs.addOrUpdateLocation(loc)) {
                    Toast.makeText(this, R.string.max_locations_reached, Toast.LENGTH_SHORT).show();
                }
                HomeActivity.startClearTask(this, lat, lon, name);
                finishAffinity();
                return;
            }

            if (!prefs.addOrUpdateLocation(loc)) {
                Toast.makeText(this, R.string.max_locations_reached, Toast.LENGTH_SHORT).show();
                return;
            }
            Toast.makeText(this, R.string.location_added, Toast.LENGTH_SHORT).show();
            finish();
            ActivityTransitions.slideOut(this);
        });
    }
}
