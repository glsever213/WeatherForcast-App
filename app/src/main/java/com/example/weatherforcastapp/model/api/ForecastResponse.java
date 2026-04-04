package com.example.weatherforcastapp.model.api;

import com.google.gson.annotations.SerializedName;

/**
 * Phản hồi {@code forecast.json}: có {@code current} + {@code forecast.forecastday}
 * — đủ để vừa hero vừa vài hàng dự báo ngày trên Home.
 */
public final class ForecastResponse {

    @SerializedName("location")
    private LocationDto location;

    @SerializedName("current")
    private CurrentDto current;

    @SerializedName("forecast")
    private ForecastBucketDto forecast;

    public LocationDto getLocation() {
        return location;
    }

    public CurrentDto getCurrent() {
        return current;
    }

    public ForecastBucketDto getForecast() {
        return forecast;
    }
}
