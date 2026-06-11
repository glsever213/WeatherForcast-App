package com.example.weatherforcastapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.example.weatherforcastapp.api.WeatherApiIcons;
import com.example.weatherforcastapp.data.WeatherRepository;
import com.example.weatherforcastapp.databinding.ActivityHomeBinding;
import com.example.weatherforcastapp.location.LocationContract;
import com.example.weatherforcastapp.model.api.ApiForecastDayDto;
import com.example.weatherforcastapp.model.api.ConditionDto;
import com.example.weatherforcastapp.model.api.CurrentDto;
import com.example.weatherforcastapp.model.api.DayAggregateDto;
import com.example.weatherforcastapp.model.api.ForecastBucketDto;
import com.example.weatherforcastapp.model.api.ForecastResponse;
import com.example.weatherforcastapp.prefs.WeatherPreferences;
import com.example.weatherforcastapp.ui.ForecastDayAdapter;
import com.example.weatherforcastapp.util.ActivityTransitions;
import com.example.weatherforcastapp.util.ChartSamples;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import android.graphics.Color;

/**
 * Trang chủ: hero, MotionLayout, danh sách ngày; tải thời tiết qua {@link WeatherRepository}
 * (Retrofit + Gson POJO), hủy request khi destroy.
 */
public class HomeActivity extends AppCompatActivity {

    /** Alias để code cũ / tài liệu tham chiếu cùng một key với {@link LocationContract}. */
    public static final String EXTRA_LAT = LocationContract.EXTRA_LAT;
    public static final String EXTRA_LON = LocationContract.EXTRA_LON;
    public static final String EXTRA_NAME = LocationContract.EXTRA_DISPLAY_NAME;

    /** Khoảng cuộn càng dài, hero/ chữ mất càng chậm — tránh “nhảy” layout. */
    private static final float HERO_HIDE_DISTANCE_PX = 520f;

    private ActivityHomeBinding binding;
    private WeatherPreferences prefs;
    private final WeatherRepository weatherRepo = new WeatherRepository();
    private ForecastDayAdapter forecastDayAdapter;

    public static void startClearTask(Context context, double lat, double lon, @Nullable String name) {
        Intent i = new Intent(context, HomeActivity.class);
        LocationContract.putLocation(i, lat, lon, name);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(i);
    }

    public static void startClearTop(@NonNull AppCompatActivity from, double lat, double lon, @Nullable String name) {
        Intent i = new Intent(from, HomeActivity.class);
        LocationContract.putLocation(i, lat, lon, name);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        from.startActivity(i);
        ActivityTransitions.slideIn(from);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        prefs = new WeatherPreferences(this);

        forecastDayAdapter = new ForecastDayAdapter();
        binding.recyclerForecast5d.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerForecast5d.setAdapter(forecastDayAdapter);

        applyLocationFromIntent(getIntent());

        ChartSamples.styleHourlyChart(binding.chartHourlyTemp);

        loadWeatherApiHeroPlaceholderIfNoData();

        binding.scrollHome.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener)
                (v, scrollX, scrollY, oldScrollX, oldScrollY) -> applyHeroVisibility(scrollY));

        binding.buttonAI.setOnClickListener(v -> {
            Intent intent = new Intent(this, AIActivity.class);
            startActivity(intent);});
        binding.buttonAddLocation.setOnClickListener(v -> {
            v.animate().scaleX(0.92f).scaleY(0.92f).setDuration(70).withEndAction(() ->
                    v.animate().scaleX(1f).scaleY(1f).setDuration(100).withEndAction(() -> {
                        startActivity(new Intent(this, LocationManagementActivity.class));
                        ActivityTransitions.slideIn(this);
                    }).start()
            ).start();
        });
        binding.buttonMore.setOnClickListener(v ->
                Toast.makeText(this, R.string.frame_note, Toast.LENGTH_SHORT).show());

        binding.buttonOpenForecastDetail.setOnClickListener(this::openFiveDayForecastScreen);

        binding.swipeRefresh.setOnRefreshListener(this::onSwipeRefresh);

    }

    @Override
    protected void onDestroy() {
        weatherRepo.cancel();
        super.onDestroy();
    }

    private void onSwipeRefresh() {
        double la = prefs.getCurrentLat();
        double lo = prefs.getCurrentLon();
        if (isInvalidCoords(la, lo)) {
            binding.swipeRefresh.setRefreshing(false);
            return;
        }
        weatherRepo.fetchForecastForHome(la, lo, prefs, true, "vi", new WeatherRepository.HomeForecastListener() {
            @Override
            public void onSuccess(@NonNull ForecastResponse body) {
                runOnUiThread(() -> {
                    binding.swipeRefresh.setRefreshing(false);
                    bindForecastToHome(body);
                });
            }

            @Override
            public void onFailure(@Nullable String message) {
                runOnUiThread(() -> {
                    binding.swipeRefresh.setRefreshing(false);
                    if (message != null) {
                        Toast.makeText(HomeActivity.this, R.string.weather_load_failed, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void applyHeroVisibility(int scrollY) {
        float t = Math.min(1f, Math.max(0f, scrollY / HERO_HIDE_DISTANCE_PX));
        float p = t * t * (3f - 2f * t);
        binding.motionHero.setProgress(p);
        binding.motionHero.setVisibility(View.VISIBLE);
    }

    /** Icon mẫu khi chưa có dữ liệu API (hoặc lỗi parse). */
    private void loadWeatherApiHeroPlaceholderIfNoData() {
        String url = WeatherApiIcons.urlDayIconCode("116", WeatherApiIcons.SIZE_HERO);
        Glide.with(this)
                .load(url)
                .placeholder(R.drawable.ic_weather_placeholder)
                .error(R.drawable.ic_weather_placeholder)
                .into(binding.imageWeatherHero);
    }

    private void openFiveDayForecastScreen(View v) {
        v.animate().scaleX(0.96f).scaleY(0.96f).setDuration(85).withEndAction(() ->
                v.animate().scaleX(1f).scaleY(1f).setDuration(115).withEndAction(() ->
                        FiveDayForecastActivity.start(
                                this,
                                prefs.getCurrentLat(),
                                prefs.getCurrentLon(),
                                prefs.getCurrentName()
                        )
                ).start()
        ).start();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        applyLocationFromIntent(intent);
    }

    private void applyLocationFromIntent(Intent intent) {
        if (intent.hasExtra(LocationContract.EXTRA_LAT) && intent.hasExtra(LocationContract.EXTRA_LON)) {
            double la = LocationContract.readLat(intent, 0);
            double lo = LocationContract.readLon(intent, 0);
            prefs.setCurrentLocation(la, lo, LocationContract.readDisplayName(intent));

        }

        double lat = prefs.getCurrentLat();
        double lon = prefs.getCurrentLon();
        String name = prefs.getCurrentName();

//        binding.textLocationName.setText(name != null && !name.isEmpty() ? name : getString(R.string.placeholder_location));
//        binding.textCurrentTemp.setText(R.string.placeholder_temp);
//        binding.textConditionLine.setText(R.string.placeholder_condition);

        binding.scrollHome.post(() -> applyHeroVisibility(binding.scrollHome.getScrollY()));

        loadWeatherApiHeroPlaceholderIfNoData();

        if (isInvalidCoords(lat, lon)) {
            return;
        }
//ham load du lieu tu api
        weatherRepo.fetchForecastForHome(lat, lon, prefs, false, "vi", new WeatherRepository.HomeForecastListener() {
            @Override
            public void onSuccess(@NonNull ForecastResponse body) {
                runOnUiThread(() -> bindForecastToHome(body));
            }

            @Override
            public void onFailure(@Nullable String message) {
                if (message != null) {
                    runOnUiThread(() ->
                            Toast.makeText(HomeActivity.this, R.string.weather_load_failed, Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    private static boolean isInvalidCoords(double lat, double lon) {
        return Math.abs(lat) < 1e-5 && Math.abs(lon) < 1e-5;
    }

    private void bindForecastToHome(@NonNull ForecastResponse r) {
        CurrentDto cur = r.getCurrent();
        if (cur == null) {
            return;
        }
        if (r.getLocation() != null && r.getLocation().getName() != null) {
            String realName = r.getLocation().getName();
            binding.textLocationName.setText(realName);//lay ten cua thanh pho
            prefs.setCurrentLocation(
                    prefs.getCurrentLat(),
                    prefs.getCurrentLon(),
                    realName
            );
        }//Lấy location name từ Weather API

        if (cur.getAirQuality() != null) {//AQI
            int aqi = cur.getAirQuality().getUsEpaIndex();
            binding.textAqi.setText("AQI " + aqi);
        }

        binding.textConditionLine.setText(cur.getCondition().getText());

        if (cur.getTempC() != null) {//hien thi nhiet do hien tai
            binding.textCurrentTemp.setText(String.format(Locale.getDefault(), "%.0f°", cur.getTempC()));
        }
        ConditionDto cond = cur.getCondition();//hien thi trang thai thoi tiet
        if (cond != null && cond.getText() != null) {
            binding.textConditionLine.setText(cond.getText());
        }

        if (cur.getUv() != null) {//hien thi tia UV
            binding.textMetricUv.setText(String.valueOf(cur.getUv()));
        }
        if (cur.getHumidity() != null) {//hien thi Humidity
            binding.textMetricHumidity.setText(cur.getHumidity() + "%");
        }
        if (cur.getFeelslikeC() != null) {//hien thi nhiet do cam nhan
            binding.textMetricFeels.setText(String.format(Locale.getDefault(), "%.0f°", cur.getFeelslikeC()));
        }
        if (cur.getWindKph() != null && cur.getWindKph() != null) {//hien thi huong gio
            binding.textMetricWind.setText(cur.getWindKph() + " km/h");
        }
        if (cur.getPressureMb() != null) {//hien thi ap suat
            binding.textMetricPressure.setText(cur.getPressureMb() + " mb");
        }

        if (cond != null) {//hien thi icon to du bao thoi tiet
            String u = WeatherApiIcons.url(cond.getIcon(), cur.isDaytime(), WeatherApiIcons.SIZE_HERO);
            if (u != null && !u.isEmpty()) {
                Glide.with(this)
                        .load(u)
                        .placeholder(R.drawable.ic_weather_placeholder)
                        .error(R.drawable.ic_weather_placeholder)
                        .into(binding.imageWeatherHero);
            }
        }

        ForecastBucketDto fb = r.getForecast();//hien thi mat troi moc va lan
        if (fb != null && fb.getForecastday() != null && !fb.getForecastday().isEmpty()) {
            ApiForecastDayDto today = fb.getForecastday().get(0);
            if (today.getAstro() != null) {
                String sunrise = today.getAstro().getSunrise();
                String sunset = today.getAstro().getSunset();
                binding.textMetricSun.setText("↑ " + sunrise + "\n" + "↓ " + sunset);
            }
        }

        List<ApiForecastDayDto> days = fb.getForecastday();//du bao thoi tiet 3 ngay
        List<ForecastDayAdapter.Row> rows = new ArrayList<>();
        int n = Math.min(3, days.size());
        for (int i = 0; i < n; i++) {
            ApiForecastDayDto d = days.get(i);
            DayAggregateDto day = d != null ? d.getDay() : null;
            String low = "—";
            String high = "—";
            if (day != null) {
                if (day.getMintempC() != null) {
                    low = String.format(Locale.getDefault(), "%.0f°", day.getMintempC());
                }
                if (day.getMaxtempC() != null) {
                    high = String.format(Locale.getDefault(), "%.0f°", day.getMaxtempC());
                }
            }
            String conditionText = "—";
            if (day != null && day.getCondition() != null) {
                conditionText = day.getCondition().getText();
            }
            String label = formatForecastRowLabel(i, d != null ? d.getDate() : null);
            rows.add(new ForecastDayAdapter.Row(label, low, high, conditionText));
        }
        forecastDayAdapter.setRows(rows);

        ApiForecastDayDto today = r.getForecast().getForecastday().get(0);//bieu do nhiet do trong 24h
        if (today.getHour() != null && !today.getHour().isEmpty()) {
            List<Entry> entries = new ArrayList<>();
            for (int i = 0; i < today.getHour().size(); i+=3) {
                ApiForecastDayDto.Hour h = today.getHour().get(i);
                if (h.getTempC() != null) {
                    entries.add(new Entry(i, h.getTempC().floatValue()));
                }
            }
            updateHourlyChart(entries);
        }
    }

    private void updateHourlyChart(List<Entry> entries) {

        LineDataSet set = new LineDataSet(entries, "°C");

        set.setColor(getColor(R.color.chart_line));
        set.setLineWidth(2f);
        set.setDrawCircles(true);
        set.setCircleColor(Color.WHITE);
        set.setCircleHoleColor(getColor(R.color.chart_line));
        set.setDrawValues(true);
        set.setValueTextColor(Color.DKGRAY);
        set.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        LineData data = new LineData(set);

        binding.chartHourlyTemp.setData(data);
        binding.chartHourlyTemp.invalidate();
    }

    //hien thu trong tuan o du bao 3 ngay
    private String formatForecastRowLabel(int indexInList, @Nullable String yyyyMmDd) {
        if (indexInList == 0) {
            return getString(R.string.forecast_today_section);
        }

        if (yyyyMmDd == null || yyyyMmDd.isEmpty()) {
            return "—";
        }
        try {
            SimpleDateFormat in = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            Date d = in.parse(yyyyMmDd);
            if (d != null) {
                SimpleDateFormat out = new SimpleDateFormat("EEE", new Locale("vi", "VN"));
                String day = out.format(d);
                return Character.toUpperCase(day.charAt(0)) + day.substring(1);
                //return out.format(d);
            }
        } catch (ParseException ignored) {
        }
        return "—";
    }
}
