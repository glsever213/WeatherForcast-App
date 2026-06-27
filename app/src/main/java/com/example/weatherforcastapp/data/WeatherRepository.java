package com.example.weatherforcastapp.data;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.weatherforcastapp.api.WeatherApiClient;
import com.example.weatherforcastapp.api.WeatherApiQuery;
import com.example.weatherforcastapp.model.SavedLocation;
import com.example.weatherforcastapp.model.api.ApiForecastDayDto;
import com.example.weatherforcastapp.model.api.ConditionDto;
import com.example.weatherforcastapp.model.api.CurrentDto;
import com.example.weatherforcastapp.model.api.DayAggregateDto;
import com.example.weatherforcastapp.model.api.ForecastResponse;
import com.example.weatherforcastapp.prefs.WeatherPreferences;

import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Một điểm gọi API thời tiết cho UI: dùng {@link WeatherApiClient} + Retrofit Gson,
 * tránh lặp cấu hình ở từng Activity. Hủy {@link Call} khi Activity destroy để không cập nhật UI sau khi đóng.
 */
public final class WeatherRepository {

    public interface HomeForecastListener {
        void onSuccess(@NonNull ForecastResponse body);

        void onFailure(@Nullable String message);
    }

    public interface LocationUpdateListener {
        void onUpdated(@NonNull SavedLocation location);
    }

    private Call<ForecastResponse> pendingForecast;

    public void cancel() {
        if (pendingForecast != null) {
            pendingForecast.cancel();
            pendingForecast = null;
        }
    }

    public void updateWeatherForLocation(
            @NonNull SavedLocation loc,
            @NonNull WeatherPreferences prefs,
            @Nullable LocationUpdateListener listener
    ) {
        String q = WeatherApiQuery.latLon(loc.getLatitude(), loc.getLongitude());
        WeatherApiClient.api().getForecast(q, 1, "no", "vi").enqueue(new Callback<ForecastResponse>() {
            @Override
            public void onResponse(@NonNull Call<ForecastResponse> call, @NonNull Response<ForecastResponse> response) {
                ForecastResponse body = response.body();
                if (response.isSuccessful() && body != null && body.getCurrent() != null) {
                    CurrentDto cur = body.getCurrent();
                    loc.setCachedTemp(String.format(Locale.getDefault(), "%.0f°", cur.getTempC()));
                    loc.setCachedHumidity(String.format(Locale.getDefault(), "%d%%", cur.getHumidity()));

                    ConditionDto cond = cur.getCondition();
                    if (cond != null) {
                        loc.setCachedSummaryLine(cond.getText());
                        loc.setCachedIconCode(cond.getIcon());
                        loc.setCachedConditionCode(cond.getCode());
                    }
                    loc.setCachedIsDaytime(cur.isDaytime());

                    if (body.getForecast() != null && body.getForecast().getForecastday() != null && !body.getForecast().getForecastday().isEmpty()) {
                        ApiForecastDayDto firstDay = body.getForecast().getForecastday().get(0);
                        DayAggregateDto day = firstDay.getDay();
                        if (day != null) {
                            loc.setCachedHighLow(String.format(Locale.getDefault(), "%.0f° / %.0f°",
                                    day.getMaxtempC(), day.getMintempC()));
                        }
                    }

                    prefs.addOrUpdateLocation(loc);
                    if (listener != null) {
                        listener.onUpdated(loc);
                    }
                } else if (listener != null) {
                    listener.onUpdated(loc);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ForecastResponse> call, @NonNull Throwable t) {
                if (listener != null) {
                    listener.onUpdated(loc);
                }
            }
        });
    }

    /** Lấy forecast (kèm {@code current}) cho Home / chi tiết / AI. */
    public void fetchForecastForHome(
            double lat,
            double lon,
            @NonNull String lang,
            @NonNull HomeForecastListener listener
    ) {
        cancel();
        String q = WeatherApiQuery.latLon(lat, lon);
        pendingForecast = WeatherApiClient.api().getForecast(q, 3, "yes", lang);
        pendingForecast.enqueue(new Callback<ForecastResponse>() {
            @Override
            public void onResponse(@NonNull Call<ForecastResponse> call, @NonNull Response<ForecastResponse> response) {
                if (call.isCanceled()) {
                    return;
                }
                ForecastResponse body = response.body();
                if (response.isSuccessful() && body != null && body.getCurrent() != null) {
                    listener.onSuccess(body);
                } else {
                    listener.onFailure(response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ForecastResponse> call, @NonNull Throwable t) {
                if (call.isCanceled()) {
                    return;
                }
                listener.onFailure(t.getMessage());
            }
        });
    }
}
