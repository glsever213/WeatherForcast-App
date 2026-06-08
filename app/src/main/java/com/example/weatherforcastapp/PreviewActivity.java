package com.example.weatherforcastapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.weatherforcastapp.api.WeatherApiIcons;
import com.example.weatherforcastapp.data.WeatherRepository;
import com.example.weatherforcastapp.databinding.ActivityPreviewBinding;
import com.example.weatherforcastapp.databinding.WidgetTodayHourlyBlockBinding;
import com.example.weatherforcastapp.location.LocationContract;
import com.example.weatherforcastapp.model.SavedLocation;
import com.example.weatherforcastapp.model.api.ApiForecastDayDto;
import com.example.weatherforcastapp.model.api.ConditionDto;
import com.example.weatherforcastapp.model.api.CurrentDto;
import com.example.weatherforcastapp.model.api.ForecastResponse;
import com.example.weatherforcastapp.prefs.WeatherPreferences;
import com.example.weatherforcastapp.ui.TodayHourlySectionHelper;
import com.example.weatherforcastapp.util.ActivityTransitions;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Xem trước: khối **Hôm nay** (theo giờ) như màn dự báo 5 ngày; FAB “Thêm vào trang home” giữ nguyên dưới.
 */
public class PreviewActivity extends AppCompatActivity {

    private ActivityPreviewBinding binding;
    private String cityName;
    private double lat;
    private double lon;
    private int flowMode;

    private final WeatherRepository weatherRepo = new WeatherRepository();
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
        binding.layoutPreviewHero.setVisibility(View.INVISIBLE);

        WidgetTodayHourlyBlockBinding hourly = binding.blockTodayHourlyPreview;
        TodayHourlySectionHelper.setupBlur(this, hourly.blurTodayHourly);

        applyFabSavedState();

        binding.buttonBackPreview.setOnClickListener(v -> {
            finish();
            ActivityTransitions.slideOut(this);
        });
        binding.fabAddToHome.setOnClickListener(v -> onFabClicked());

        loadWeatherData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        weatherRepo.cancel();
    }

    private void applyFabSavedState() {
        WeatherPreferences prefs = WeatherPreferences.get(this);
        String targetId = SavedLocation.buildId(lat, lon);
        boolean alreadySaved = false;

        for (SavedLocation saved : prefs.getSavedLocations()) {
            if (saved.getId().equals(targetId)) {
                alreadySaved = true;
                break;
            }
        }

        if (alreadySaved) {
            binding.fabAddToHome.setEnabled(false);
            binding.fabAddToHome.setText(R.string.location_already_saved);
            binding.fabAddToHome.setAlpha(0.5f);
        }
    }

    private void loadWeatherData() {
        showLoading(true);

        WeatherPreferences prefs = WeatherPreferences.get(this);
        weatherRepo.fetchForecastForHome(lat, lon, prefs, true, "vi",
                new WeatherRepository.HomeForecastListener() {
                    @Override
                    public void onSuccess(@NonNull ForecastResponse body) {
                        runOnUiThread(() -> {
                            showLoading(false);
                            bindForecastToPreview(body);
                        });
                    }

                    @Override
                    public void onFailure(@Nullable String message) {
                        runOnUiThread(() -> {
                            showLoading(false);
                            // Không toast nếu message null (cache miss / throttle) — silent fail
                            if (message != null) {
                                Toast.makeText(
                                        PreviewActivity.this,
                                        R.string.weather_load_failed,
                                        Toast.LENGTH_SHORT
                                ).show();
                            }
                        });
                    }
                });
    }

    private void bindForecastToPreview(@NonNull ForecastResponse r) {
        CurrentDto cur = r.getCurrent();
        if (cur != null) {
            if (cur.getTempC() != null) {
                binding.textPreviewTemp.setText(
                        String.format(Locale.getDefault(), "%.0f°", cur.getTempC()));
            }

            ConditionDto cond = cur.getCondition();
            if (cond != null && cond.getText() != null) {
                binding.textPreviewCondition.setText(cond.getText());
            }

            if (cond != null) {
                String iconUrl = WeatherApiIcons.url(
                        cond.getIcon(), cur.isDaytime(), WeatherApiIcons.SIZE_HERO);
                if (iconUrl != null && !iconUrl.isEmpty()) {
                    Glide.with(this)
                            .load(iconUrl)
                            .placeholder(R.drawable.ic_weather_placeholder)
                            .error(R.drawable.ic_weather_placeholder)
                            .into(binding.imagePreviewWeatherIcon);
                }
            }
        }

        binding.layoutPreviewHero.setVisibility(View.VISIBLE);

        String dateLabel = buildTodayDateLabel();
        TodayHourlySectionHelper.bindHeader(binding.blockTodayHourlyPreview, dateLabel);

        List<ApiForecastDayDto> forecastDays = null;
        if (r.getForecast() != null) {
            forecastDays = r.getForecast().getForecastday();
        }

        List<com.example.weatherforcastapp.model.api.HourItemDto> hours = null;
        if (forecastDays != null && !forecastDays.isEmpty()) {
            ApiForecastDayDto today = forecastDays.get(0);
            if (today != null) {
                hours = today.getHour();
            }
        }

        int currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);

        TodayHourlySectionHelper.bindRecyclerFromApi(
                binding.blockTodayHourlyPreview.recyclerTodayHourly,
                hours,
                currentHour
        );
    }

    private String buildTodayDateLabel() {
        SimpleDateFormat sdf = new SimpleDateFormat("d/M", new Locale("vi", "VN"));
        return sdf.format(new Date());
    }

    private void showLoading(boolean loading) {
        binding.progressPreview.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    private void onFabClicked() {
        WeatherPreferences prefs = WeatherPreferences.get(this);
        String name = cityName != null ? cityName : "—";
        SavedLocation loc = new SavedLocation(name, lat, lon, getString(R.string.frame_note));

        if (flowMode == LocationContract.FLOW_ONBOARDING) {
            prefs.setCurrentLocation(lat, lon, name);
            prefs.addOrUpdateLocation(loc);
            if (prefs.getSavedLocations().size() >= WeatherPreferences.MAX_SAVED_LOCATIONS
                    && !prefs.getSavedLocations().stream().anyMatch(s -> s.getId().equals(loc.getId()))) {
                Toast.makeText(this, R.string.max_locations_reached, Toast.LENGTH_SHORT).show();
            }
            HomeActivity.startClearTask(this, lat, lon, name);
            finishAffinity();
            return;
        }

        boolean added = prefs.addOrUpdateLocation(loc);
        if (!added) {
            Toast.makeText(this, R.string.max_locations_reached, Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, R.string.location_added, Toast.LENGTH_SHORT).show();

        binding.fabAddToHome.setEnabled(false);
        binding.fabAddToHome.setText(R.string.location_already_saved);
        binding.fabAddToHome.setAlpha(0.5f);

        finish();
        ActivityTransitions.slideOut(this);
    }
}