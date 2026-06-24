package com.example.weatherforcastapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.weatherforcastapp.data.WeatherRepository;
import com.example.weatherforcastapp.databinding.ActivityFiveDayForecastBinding;
import com.example.weatherforcastapp.databinding.WidgetTodayHourlyBlockBinding;
import com.example.weatherforcastapp.location.LocationContract;
import com.example.weatherforcastapp.prefs.WeatherPreferences;
import com.example.weatherforcastapp.ui.DailyForecastAdapter;
import com.example.weatherforcastapp.ui.Forecast5dSamples;
import com.example.weatherforcastapp.ui.HourlyForecastAdapter;
import com.example.weatherforcastapp.ui.TodayHourlySectionHelper;
import com.example.weatherforcastapp.util.ActivityTransitions;

import java.util.ArrayList;
import androidx.annotation.NonNull;
import com.example.weatherforcastapp.model.api.ApiForecastDayDto;
import com.example.weatherforcastapp.model.api.DayAggregateDto;
import com.example.weatherforcastapp.model.api.ForecastResponse;
import java.util.List;
import java.util.Locale;

/**
 * Dự báo 5 ngày — nền đen, khối Today (giờ) + bảng 3 cột dùng chung với Preview.
 */
public class FiveDayForecastActivity extends AppCompatActivity {

    private ActivityFiveDayForecastBinding binding;
    private final WeatherRepository weatherRepo = new WeatherRepository();
    private DailyForecastAdapter adapter;
    private WeatherPreferences prefs;
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
        prefs = new WeatherPreferences(this);

        double lat = LocationContract.readLat(getIntent(), 0);
        double lon = LocationContract.readLon(getIntent(), 0);

        WidgetTodayHourlyBlockBinding wb = binding.blockTodayHourly;
        TodayHourlySectionHelper.bindHeader(wb, getString(R.string.forecast_today_date_placeholder));
        TodayHourlySectionHelper.setupBlur(this, wb.blurTodayHourly);

        binding.buttonBackForecast.setOnClickListener(v -> {
            finish();
            ActivityTransitions.slideOut(this);
        });

        binding.blockForecast5dTable.recyclerForecast5dTable.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        adapter = new DailyForecastAdapter(new ArrayList<>());

        binding.blockForecast5dTable.recyclerForecast5dTable.setLayoutManager(new LinearLayoutManager(this));

        binding.blockForecast5dTable.recyclerForecast5dTable.setAdapter(adapter);

        loadForecast(lat, lon);
    }

    private void loadForecast(double lat, double lon) {//ham load du lieu tu api
        weatherRepo.fetchForecastForHome(lat, lon,prefs,true,"vi",
                new WeatherRepository.HomeForecastListener() {
                    @Override
                    public void onSuccess(@NonNull ForecastResponse body) {
                        runOnUiThread(() -> bindForecast(body));
                    }
                    @Override
                    public void onFailure(@Nullable String message) {
                        runOnUiThread(() ->
                                Toast.makeText(
                                        FiveDayForecastActivity.this,
                                        "Load failed",
                                        Toast.LENGTH_SHORT
                                ).show()
                        );
                    }
                });
    }
    private void bindForecast(@NonNull ForecastResponse r) {
        List<DailyForecastAdapter.Slot> rows = new ArrayList<>();
        if (r.getForecast() == null ||
                r.getForecast().getForecastday() == null) {
            return;
        }
        List<ApiForecastDayDto> days = r.getForecast().getForecastday();

        ApiForecastDayDto today = days.get(0);
        binding.blockTodayHourly.textTodayDateLine.setText(today.getDate());
        List<HourlyForecastAdapter.Slot> hourlyData = new ArrayList<>();

        if (today.getHour() != null) {//hien thi thoi gian thuc va nhiet do theo thoi gian thuc
            for (int i = 0; i < today.getHour().size(); i++) {
                ApiForecastDayDto.Hour h = today.getHour().get(i);
                String temp = String.format(Locale.getDefault(), "%.0f°", h.getTempC());
                String time = h.getTime().substring(11, 16);
                String icon = "";

                if (h.getCondition() != null) {
                    icon = h.getCondition().getIcon();
                }
                hourlyData.add(new HourlyForecastAdapter.Slot(temp, time, icon)
                );
            }
        }
        TodayHourlySectionHelper.bindRecycler(binding.blockTodayHourly.recyclerTodayHourly, hourlyData);

        //hien thi du lieu thoi tiet trong 3 ngay tiep
        for (int i = 0; i < days.size(); i++) {
            ApiForecastDayDto d = days.get(i);
            DayAggregateDto day = d.getDay();
            String date = d.getDate();

            String min = "—";
            String max = "—";
            String condition = "";
            String iconCode = "";
            String rainChance = "";
            String feelsLike = "";
            String wind = "";
            String humidity = "";
            String uv = "";

            if (day != null) {
                if (day.getMintempC() != null) {
                    min = String.format(Locale.getDefault(), "%.0f°", day.getMintempC());//lay nhiet do thap nhat
                }
                if (day.getMaxtempC() != null) {
                    max = String.format(Locale.getDefault(), "%.0f°", day.getMaxtempC());//lay nhiet do cao nhat
                }
                if (day != null) {
                    if (day.getCondition() != null) {
                        iconCode = day.getCondition().getIcon();//lay icon thoi tiet
                        condition = day.getCondition().getText();//lay du bao thoi tiet
                    }
                }
                if(day.getDailyChanceOfRain() != null){
                    rainChance = "💧 " + day.getDailyChanceOfRain() + "%";//lay phân tram mua
                }
                if(day.getAvgtempC() != null){
                    feelsLike = String.format(Locale.getDefault(),"Cảm giác "+ "%.0f°", day.getAvgtempC());//lay nhiet do cam nhan
                }
                if(day.getAvghumidity() != null){
                    humidity = "💦 " + String.format(Locale.getDefault(),"%.0f%%", day.getAvghumidity());//lay do am khong khi
                }
                if(day.getMaxwindKph() != null){
                    wind = "🌬️ " + String.format(Locale.getDefault(),"%.0f km/h", day.getMaxwindKph());//lay toc do gio
                }
                if(day.getUv() != null){
                    uv = "☀ UV " + String.format(Locale.getDefault(),"%.0f", day.getUv());//lay tia uv
                }
            }
            rows.add(new DailyForecastAdapter.Slot(date, min + " / " + max, condition, iconCode, rainChance, feelsLike, wind, humidity, uv)
            );
        }
        adapter.setRows(rows);
    }
}
