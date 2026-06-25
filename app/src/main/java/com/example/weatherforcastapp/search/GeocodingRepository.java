package com.example.weatherforcastapp.search;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.weatherforcastapp.api.OpenMeteoClient;
import com.example.weatherforcastapp.model.api.OpenMeteoResponse;
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

    private Call<OpenMeteoResponse> pendingSearch;

    public void cancel() {
        if (pendingSearch != null) {
            pendingSearch.cancel();
            pendingSearch = null;
        }
    }

    public void search(@NonNull String query, @NonNull Listener listener) {
        cancel();
        pendingSearch = OpenMeteoClient.api().searchLocation(query, 5);
        pendingSearch.enqueue(new Callback<OpenMeteoResponse>() {
            @Override
            public void onResponse(@NonNull Call<OpenMeteoResponse> call, @NonNull Response<OpenMeteoResponse> response) {
                if (call.isCanceled()) {
                    return;
                }
                OpenMeteoResponse body = response.body();
                if (response.isSuccessful() && body != null && body.getResults() != null) {
                    java.util.List<LocationDto> mapped = new java.util.ArrayList<>();
                    for (OpenMeteoResponse.Result r : body.getResults()) {
                        mapped.add(new LocationDto(r.getName(), r.getCountry(), r.getLatitude(), r.getLongitude()));
                    }
                    listener.onSuccess(mapped);
                } else {
                    listener.onFailure(response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<OpenMeteoResponse> call, @NonNull Throwable t) {
                if (call.isCanceled()) {
                    return;
                }
                listener.onFailure(t.getMessage());
            }
        });
    }
}
