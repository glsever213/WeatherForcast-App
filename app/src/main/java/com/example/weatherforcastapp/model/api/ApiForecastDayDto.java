package com.example.weatherforcastapp.model.api;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/** Một phần tử trong {@code forecast.forecastday}. */
public final class ApiForecastDayDto {

    @SerializedName("date")
    private String date;

    @SerializedName("day")
    private DayAggregateDto day;

    @SerializedName("hour")
    private List<HourItemDto> hour;

    public String getDate() {
        return date;
    }

    public DayAggregateDto getDay() {
        return day;
    }

    public List<HourItemDto> getHour() {
        return hour;
    }
}
