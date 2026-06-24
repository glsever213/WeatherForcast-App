package com.example.weatherforcastapp.data;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.weatherforcastapp.api.WeatherApiClient;
import com.example.weatherforcastapp.api.WeatherApiQuery;
import com.example.weatherforcastapp.model.api.ForecastResponse;
import com.example.weatherforcastapp.prefs.WeatherPreferences;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

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

    private final Gson gson = new Gson();
    private Call<ForecastResponse> pendingForecast;

    public void cancel() {
        if (pendingForecast != null) {
            pendingForecast.cancel();
            pendingForecast = null;
        }
    }

    /**
     * Lấy forecast (kèm {@code current}) cho Home. Một request {@code forecast.json} đủ hero + vài ngày.
     *
     * @param bypassThrottle true khi kéo refresh — bỏ qua {@link WeatherPreferences#canCallApi} nhưng vẫn ghi cache khi thành công.
     */
    public void fetchForecastForHome(
            double lat,
            double lon,
            @NonNull WeatherPreferences prefs,
            boolean bypassThrottle,
            @NonNull String lang,
            @NonNull HomeForecastListener listener
    ) {
        cancel();
        String key = prefs.cacheKeyForCoords(lat, lon);

        if (!bypassThrottle && !prefs.canCallApi(key, WeatherPreferences.DEFAULT_MIN_API_INTERVAL_MS)) {
            String cached = prefs.getCachedApiBody(key);
            if (cached != null && !cached.isEmpty()) {
                try {
                    ForecastResponse parsed = gson.fromJson(cached, ForecastResponse.class);
                    if (parsed != null && parsed.getCurrent() != null) {
                        listener.onSuccess(parsed);
                        return;
                    }
                } catch (JsonSyntaxException ignored) {
                    // fall through to network
                }
            }
            listener.onFailure(null);
            return;
        }

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
                    prefs.markApiCalled(key);
                    prefs.putApiResponseCache(key, gson.toJson(body));
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
