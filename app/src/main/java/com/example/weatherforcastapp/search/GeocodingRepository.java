package com.example.weatherforcastapp.search;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.weatherforcastapp.api.WeatherApiClient;
import com.example.weatherforcastapp.model.api.LocationDto;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** Tầng gọi WeatherAPI search.json để lấy địa điểm thật. */
public final class GeocodingRepository {

    public interface Listener {
        void onSuccess(@NonNull List<LocationDto> results);

        void onFailure(@Nullable String message);
    }

    private Call<List<LocationDto>> pendingSearch;

    public void cancel() {
        if (pendingSearch != null) {
            pendingSearch.cancel();
            pendingSearch = null;
        }
    }

    public void search(@NonNull String query, @NonNull Listener listener) {
        cancel();
        pendingSearch = WeatherApiClient.api().search(query);
        pendingSearch.enqueue(new Callback<List<LocationDto>>() {
            @Override
            public void onResponse(@NonNull Call<List<LocationDto>> call, @NonNull Response<List<LocationDto>> response) {
                if (call.isCanceled()) {
                    return;
                }
                List<LocationDto> body = response.body();
                if (response.isSuccessful() && body != null) {
                    listener.onSuccess(body);
                } else {
                    listener.onFailure(response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<LocationDto>> call, @NonNull Throwable t) {
                if (call.isCanceled()) {
                    return;
                }
                listener.onFailure(t.getMessage());
            }
        });
    }
}
