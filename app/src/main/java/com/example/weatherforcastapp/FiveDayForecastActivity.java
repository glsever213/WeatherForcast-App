package com.example.weatherforcastapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.weatherforcastapp.data.WeatherRepository;
import com.example.weatherforcastapp.databinding.ActivityFiveDayForecastBinding;
import com.example.weatherforcastapp.databinding.WidgetTodayHourlyBlockBinding;
import com.example.weatherforcastapp.location.LocationContract;
import com.example.weatherforcastapp.model.api.ApiForecastDayDto;
import com.example.weatherforcastapp.model.api.AstroDto;
import com.example.weatherforcastapp.model.api.ConditionDto;
import com.example.weatherforcastapp.model.api.CurrentDto;
import com.example.weatherforcastapp.model.api.DayAggregateDto;
import com.example.weatherforcastapp.model.api.ForecastResponse;
import com.example.weatherforcastapp.model.api.HourItemDto;
import com.example.weatherforcastapp.prefs.WeatherPreferences;
import com.example.weatherforcastapp.ui.DailyForecastAdapter;
import com.example.weatherforcastapp.ui.HourlyForecastAdapter;
import com.example.weatherforcastapp.ui.TodayHourlySectionHelper;
import com.example.weatherforcastapp.util.ActivityTransitions;
import com.example.weatherforcastapp.util.WeatherConditionTheme;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Chi tiết dự báo 3 ngày — nền/animation như Home, thẻ glass + nhiều chỉ số từ API.
 */
public class FiveDayForecastActivity extends AppCompatActivity {

    private ActivityFiveDayForecastBinding binding;
    private final WeatherRepository weatherRepo = new WeatherRepository();
    private DailyForecastAdapter adapter;
    private WeatherPreferences prefs;
    private int currentAnimationRes = -1;

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
        prefs = WeatherPreferences.get(this);

        double lat = LocationContract.readLat(getIntent(), 0);
        double lon = LocationContract.readLon(getIntent(), 0);

        WidgetTodayHourlyBlockBinding wb = binding.blockTodayHourly;
        TodayHourlySectionHelper.bindHeader(wb, getString(R.string.forecast_today_date_placeholder));
        int muted = ContextCompat.getColor(this, R.color.text_muted_on_home);
        wb.textTodayDateLine.setTextColor(muted);
        TodayHourlySectionHelper.setupBlur(this, wb.blurTodayHourly);

        binding.buttonBackForecast.setOnClickListener(v -> {
            finish();
            ActivityTransitions.slideOut(this);
        });

        binding.blockForecast5dTable.recyclerForecast5dTable.setLayoutManager(new LinearLayoutManager(this));
        adapter = new DailyForecastAdapter(new ArrayList<>());
        binding.blockForecast5dTable.recyclerForecast5dTable.setAdapter(adapter);

        binding.bgGradientShift.setVisibility(View.GONE);
        binding.weatherAnimation.setVisibility(View.GONE);
        binding.scrollForecastContent.setVisibility(View.GONE);
        showLoading(true);

        loadForecast(lat, lon);
    }

    @Override
    protected void onDestroy() {
        weatherRepo.cancel();
        super.onDestroy();
    }

    private void loadForecast(double lat, double lon) {
        weatherRepo.fetchForecastForHome(lat, lon, "vi",
                new WeatherRepository.HomeForecastListener() {
                    @Override
                    public void onSuccess(@NonNull ForecastResponse body) {
                        runOnUiThread(() -> bindForecast(body));
                    }

                    @Override
                    public void onFailure(@Nullable String message) {
                        runOnUiThread(() -> {
                            binding.scrollForecastContent.setVisibility(View.VISIBLE);
                            showLoading(false);
                            Toast.makeText(
                                    FiveDayForecastActivity.this,
                                    R.string.weather_load_failed,
                                    Toast.LENGTH_SHORT
                            ).show();
                        });
                    }
                });
    }

    private void bindForecast(@NonNull ForecastResponse r) {
        CurrentDto cur = r.getCurrent();
        if (cur != null) {
            ConditionDto cond = cur.getCondition();
            Integer code = cond != null ? cond.getCode() : null;
            applyWeatherTheme(code, cur.isDaytime());
        }

        if (r.getForecast() == null || r.getForecast().getForecastday() == null) {
            revealContent();
            return;
        }

        List<ApiForecastDayDto> days = r.getForecast().getForecastday();
        if (days.isEmpty()) {
            revealContent();
            return;
        }

        ApiForecastDayDto today = days.get(0);
        binding.blockTodayHourly.textTodayDateLine.setText(formatDateLine(today.getDate()));

        List<HourlyForecastAdapter.Slot> hourlyData = new ArrayList<>();
        if (today.getHour() != null) {
            for (HourItemDto h : today.getHour()) {
                if (h == null) {
                    continue;
                }
                String temp = h.getTempC() != null
                        ? String.format(Locale.getDefault(), "%.0f°", h.getTempC())
                        : "—";
                String time = "—";
                if (h.getTime() != null && h.getTime().length() >= 16) {
                    time = h.getTime().substring(11, 16);
                }
                String icon = h.getCondition() != null ? h.getCondition().getIcon() : "";
                hourlyData.add(new HourlyForecastAdapter.Slot(temp, time, icon));
            }
        }
        TodayHourlySectionHelper.bindRecycler(binding.blockTodayHourly.recyclerTodayHourly, hourlyData);

        List<DailyForecastAdapter.Slot> rows = new ArrayList<>();
        for (ApiForecastDayDto d : days) {
            rows.add(buildDaySlot(d));
        }
        adapter.setRows(rows);
        revealContent();
    }

    private void revealContent() {
        binding.bgGradientShift.setVisibility(View.VISIBLE);
        binding.weatherAnimation.setVisibility(View.VISIBLE);
        binding.scrollForecastContent.setVisibility(View.VISIBLE);
        showLoading(false);
    }

    private void showLoading(boolean loading) {
        binding.loadingOverlay.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    private DailyForecastAdapter.Slot buildDaySlot(@NonNull ApiForecastDayDto d) {
        DayAggregateDto day = d.getDay();
        AstroDto astro = d.getAstro();
        List<String> lines = new ArrayList<>();

        String condition = "—";
        String iconCode = "";
        String highLow = "— / —";

        if (day != null) {
            if (day.getMintempC() != null && day.getMaxtempC() != null) {
                highLow = String.format(
                        Locale.getDefault(),
                        "%.0f° / %.0f°",
                        day.getMaxtempC(),
                        day.getMintempC()
                );
            }
            if (day.getCondition() != null) {
                if (day.getCondition().getText() != null) {
                    condition = day.getCondition().getText();
                }
                iconCode = day.getCondition().getIcon() != null ? day.getCondition().getIcon() : "";
            }
            if (day.getAvgtempC() != null) {
                lines.add(label(getString(R.string.forecast_metric_avg),
                        String.format(Locale.getDefault(), "%.0f°", day.getAvgtempC())));
            }
            if (day.getDailyChanceOfRain() != null) {
                String rainValue = day.getDailyChanceOfRain() + "%";
                if (day.getDailyWillItRain() != null) {
                    rainValue += day.getDailyWillItRain() == 1
                            ? " · " + getString(R.string.forecast_will_rain_yes)
                            : " · " + getString(R.string.forecast_will_rain_no);
                }
                lines.add(label(getString(R.string.forecast_metric_rain), rainValue));
            }
            if (day.getMaxwindKph() != null) {
                lines.add(label(getString(R.string.forecast_metric_wind),
                        String.format(Locale.getDefault(), "%.0f km/h", day.getMaxwindKph())));
            }
            if (day.getAvghumidity() != null) {
                String hum = day.getAvghumidity() + "%";
                if (day.getMinhumidity() != null && day.getMaxhumidity() != null) {
                    hum += String.format(Locale.getDefault(), " (%d–%d%%)",
                            day.getMinhumidity(), day.getMaxhumidity());
                }
                lines.add(label(getString(R.string.forecast_metric_humidity), hum));
            }
            if (day.getTotalprecipMm() != null) {
                lines.add(label(getString(R.string.forecast_metric_precip),
                        String.format(Locale.getDefault(), "%.1f mm", day.getTotalprecipMm())));
            }
            if (day.getUv() != null) {
                lines.add(label(getString(R.string.forecast_metric_uv),
                        String.format(Locale.getDefault(), "%.0f", day.getUv())));
            }
            if (day.getAvgvisKm() != null) {
                lines.add(label(getString(R.string.forecast_metric_visibility),
                        String.format(Locale.getDefault(), "%.1f km", day.getAvgvisKm())));
            }
            if (day.getDailyChanceOfSnow() != null && day.getDailyChanceOfSnow() > 0) {
                String snow = day.getDailyChanceOfSnow() + "%";
                if (day.getDailyWillItSnow() != null) {
                    snow += day.getDailyWillItSnow() == 1
                            ? " · " + getString(R.string.forecast_will_snow_yes)
                            : " · " + getString(R.string.forecast_will_snow_no);
                }
                if (day.getTotalsnowCm() != null && day.getTotalsnowCm() > 0) {
                    snow += String.format(Locale.getDefault(), " · %.1f cm", day.getTotalsnowCm());
                }
                lines.add(label(getString(R.string.forecast_metric_snow), snow));
            }
        }

        if (astro != null) {
            if (astro.getSunrise() != null) {
                lines.add(label(getString(R.string.forecast_metric_sunrise), astro.getSunrise()));
            }
            if (astro.getSunset() != null) {
                lines.add(label(getString(R.string.forecast_metric_sunset), astro.getSunset()));
            }
            if (astro.getMoonrise() != null && !astro.getMoonrise().isEmpty()) {
                lines.add(label(getString(R.string.forecast_metric_moonrise), astro.getMoonrise()));
            }
            if (astro.getMoonset() != null && !astro.getMoonset().isEmpty()) {
                lines.add(label(getString(R.string.forecast_metric_moonset), astro.getMoonset()));
            }
            if (astro.getMoonPhase() != null && !astro.getMoonPhase().isEmpty()) {
                String phase = astro.getMoonPhase();
                if (astro.getMoonIllumination() != null && !astro.getMoonIllumination().isEmpty()) {
                    phase += " · " + astro.getMoonIllumination();
                }
                lines.add(label(getString(R.string.forecast_metric_moon_phase), phase));
            }
        }

        appendHourMetrics(d.getHour(), lines);

        return new DailyForecastAdapter.Slot(
                formatDateLine(d.getDate()),
                condition,
                iconCode,
                highLow,
                lines
        );
    }

    private void appendHourMetrics(@Nullable List<HourItemDto> hours, @NonNull List<String> lines) {
        if (hours == null || hours.isEmpty()) {
            return;
        }

        double maxTemp = Double.NEGATIVE_INFINITY;
        double minTemp = Double.POSITIVE_INFINITY;
        String maxTempTime = "—";
        String minTempTime = "—";

        double maxFeels = Double.NEGATIVE_INFINITY;
        double minFeels = Double.POSITIVE_INFINITY;

        double maxWind = 0;
        String maxWindDir = "";
        String maxWindTime = "—";

        int maxRainChance = 0;
        String maxRainTime = "—";

        int cloudSum = 0;
        int cloudCount = 0;

        double maxUv = 0;
        String maxUvTime = "—";

        for (HourItemDto h : hours) {
            if (h == null) {
                continue;
            }
            String time = formatHourTime(h.getTime());

            if (h.getTempC() != null) {
                if (h.getTempC() > maxTemp) {
                    maxTemp = h.getTempC();
                    maxTempTime = time;
                }
                if (h.getTempC() < minTemp) {
                    minTemp = h.getTempC();
                    minTempTime = time;
                }
            }
            if (h.getFeelslikeC() != null) {
                maxFeels = Math.max(maxFeels, h.getFeelslikeC());
                minFeels = Math.min(minFeels, h.getFeelslikeC());
            }
            if (h.getWindKph() != null && h.getWindKph() > maxWind) {
                maxWind = h.getWindKph();
                maxWindDir = h.getWindDir() != null ? h.getWindDir() : "";
                maxWindTime = time;
            }
            if (h.getChanceOfRain() != null && h.getChanceOfRain() > maxRainChance) {
                maxRainChance = h.getChanceOfRain();
                maxRainTime = time;
            }
            if (h.getCloud() != null) {
                cloudSum += h.getCloud();
                cloudCount++;
            }
            if (h.getUv() != null && h.getUv() > maxUv) {
                maxUv = h.getUv();
                maxUvTime = time;
            }
        }

        if (maxTemp != Double.NEGATIVE_INFINITY) {
            lines.add(label(getString(R.string.forecast_metric_peak_temp),
                    String.format(Locale.getDefault(), "%s · %.0f°", maxTempTime, maxTemp)));
        }
        if (minTemp != Double.POSITIVE_INFINITY) {
            lines.add(label(getString(R.string.forecast_metric_low_temp),
                    String.format(Locale.getDefault(), "%s · %.0f°", minTempTime, minTemp)));
        }
        if (maxFeels != Double.NEGATIVE_INFINITY && minFeels != Double.POSITIVE_INFINITY) {
            lines.add(label(getString(R.string.forecast_metric_feels),
                    String.format(Locale.getDefault(), "%.0f° – %.0f°", minFeels, maxFeels)));
        }
        if (maxWind > 0) {
            String windVal = String.format(Locale.getDefault(), "%s · %.0f km/h", maxWindTime, maxWind);
            if (!maxWindDir.isEmpty()) {
                windVal += " " + maxWindDir;
            }
            lines.add(label(getString(R.string.forecast_metric_wind_peak), windVal));
        }
        if (maxRainChance > 0) {
            lines.add(label(getString(R.string.forecast_metric_rain_peak),
                    String.format(Locale.getDefault(), "%s · %d%%", maxRainTime, maxRainChance)));
        }
        if (cloudCount > 0) {
            lines.add(label(getString(R.string.forecast_metric_cloud),
                    String.format(Locale.getDefault(), "%d%%", cloudSum / cloudCount)));
        }
        if (maxUv > 0) {
            lines.add(label(getString(R.string.forecast_metric_uv_peak),
                    String.format(Locale.getDefault(), "%s · %.1f", maxUvTime, maxUv)));
        }
    }

    private static String formatHourTime(@Nullable String isoTime) {
        if (isoTime != null && isoTime.length() >= 16) {
            return isoTime.substring(11, 16);
        }
        return "—";
    }

    private static String label(String name, String value) {
        return name + ": " + value;
    }

    private String formatDateLine(@Nullable String yyyyMmDd) {
        if (yyyyMmDd == null || yyyyMmDd.isEmpty()) {
            return "—";
        }
        try {
            Date date = new SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(yyyyMmDd);
            if (date == null) {
                return yyyyMmDd;
            }
            SimpleDateFormat dayFmt = new SimpleDateFormat("EEE", new Locale("vi", "VN"));
            SimpleDateFormat dateFmt = new SimpleDateFormat("d/M", new Locale("vi", "VN"));
            String day = dayFmt.format(date);
            if (!day.isEmpty()) {
                day = Character.toUpperCase(day.charAt(0)) + day.substring(1);
            }
            return day + ", " + dateFmt.format(date);
        } catch (ParseException ignored) {
            return yyyyMmDd;
        }
    }

    private void applyWeatherTheme(@Nullable Integer conditionCode, boolean isDaytime) {
        WeatherConditionTheme.Colors c = WeatherConditionTheme.resolve(conditionCode, isDaytime);
        binding.bgGradientShift.setGradientColors(c.top, c.mid, c.bottom);

        WeatherConditionTheme.AnimationType type = WeatherConditionTheme.resolveAnimation(conditionCode, isDaytime);
        int animRes;
        switch (type) {
            case NIGHT:
                animRes = R.raw.night;
                break;
            case CLOUDY:
                animRes = R.raw.cloudy;
                break;
            case RAINY:
            case STORM:
                animRes = R.raw.rain;
                break;
            case SUNNY:
            default:
                animRes = R.raw.sunny;
                break;
        }
        if (currentAnimationRes != animRes) {
            currentAnimationRes = animRes;
            binding.weatherAnimation.setAnimation(animRes);
            binding.weatherAnimation.playAnimation();
        }
    }
}
