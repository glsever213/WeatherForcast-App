package com.example.weatherforcastapp.api;

import com.example.weatherforcastapp.model.api.CurrentWeatherResponse;
import com.example.weatherforcastapp.model.api.ForecastResponse;
import com.example.weatherforcastapp.model.api.LocationDto;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Retrofit cho <a href="https://www.weatherapi.com/docs/">WeatherAPI.com</a> v1.
 * Tham số {@code key} được OkHttp interceptor gắn tự động.
 * Gson converter map JSON → POJO ({@link CurrentWeatherResponse}, {@link ForecastResponse}).
 * <p>
 * {@code q}: theo tài liệu — tọa độ dạng {@code lat,lon} (vd {@code 48.8567,2.3508}), tên thành phố, v.v.
 */
public interface WeatherApiService {

    @GET("current.json")
    Call<CurrentWeatherResponse> getCurrent(
            @Query("q") String query,
            @Query("lang") String lang
    );

    /**
     * @param query cùng định dạng {@code q} như {@link #getCurrent}
     * @param days  số ngày dự báo (1–14 theo gói tài khoản)
     */
    @GET("forecast.json")
    Call<ForecastResponse> getForecast(
            @Query("q") String query,
            @Query("days") int days,
            @Query("lang") String lang
    );

    /**
     * Search / autocomplete API của WeatherAPI trả về mảng location.
     */
    @GET("search.json")
    Call<List<LocationDto>> search(
            @Query("q") String query
    );
}
