package com.example.weatherforcastapp.model.api;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/** Một phần tử trong {@code forecast.forecastday}. */
public final class ApiForecastDayDto {

    @SerializedName("date")
    private String date;

    @SerializedName("day")
    private DayAggregateDto day;

    @SerializedName("astro")
    private AstroDto astro;

    public AstroDto getAstro() { return astro; }


    @SerializedName("hour")
    private List<Hour> hour;

    public List<Hour> getHour() { return hour; }
    public String getDate() {
        return date;
    }

    public DayAggregateDto getDay() {
        return day;
    }

    public static class AstroDto {

        @SerializedName("sunrise")
        private String sunrise;

        @SerializedName("sunset")
        private String sunset;

        public String getSunrise() { return sunrise; }
        public String getSunset() { return sunset; }
    }

    public static class Hour {

        @SerializedName("temp_c")
        private Double tempC;

        public Double getTempC() {
            return tempC;
        }
    }
}

