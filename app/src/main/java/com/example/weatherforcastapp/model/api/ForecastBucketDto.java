package com.example.weatherforcastapp.model.api;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/** Đối tượng {@code forecast} gốc trong forecast.json. */
public final class ForecastBucketDto {

    @SerializedName("forecastday")
    private List<ApiForecastDayDto> forecastday;

    public List<ApiForecastDayDto> getForecastday() {
        return forecastday;
    }
}
