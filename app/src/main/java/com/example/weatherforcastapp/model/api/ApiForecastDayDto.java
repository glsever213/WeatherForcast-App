package com.example.weatherforcastapp.model.api;

import com.google.gson.annotations.SerializedName;

/** Một phần tử trong {@code forecast.forecastday}. */
public final class ApiForecastDayDto {

    @SerializedName("date")
    private String date;

    @SerializedName("day")
    private DayAggregateDto day;

    public String getDate() {
        return date;
    }

    public DayAggregateDto getDay() {
        return day;
    }
}
