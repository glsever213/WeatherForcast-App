package com.example.weatherforcastapp.model.api;

import com.google.gson.annotations.SerializedName;

public final class HourItemDto {
    @SerializedName("time")
    private String time;

    @SerializedName("temp_c")
    private Double tempC;

    @SerializedName("feelslike_c")
    private Double feelslikeC;

    @SerializedName("condition")
    private ConditionDto condition;

    @SerializedName("is_day")
    private Integer isDay;

    @SerializedName("humidity")
    private Integer humidity;

    @SerializedName("wind_kph")
    private Double windKph;

    @SerializedName("wind_dir")
    private String windDir;

    @SerializedName("chance_of_rain")
    private Integer chanceOfRain;

    @SerializedName("precip_mm")
    private Double precipMm;

    @SerializedName("cloud")
    private Integer cloud;

    @SerializedName("vis_km")
    private Double visKm;

    @SerializedName("uv")
    private Double uv;

    public String getTime() {
        return time;
    }

    public Double getTempC() {
        return tempC;
    }

    public Double getFeelslikeC() {
        return feelslikeC;
    }

    public ConditionDto getCondition() {
        return condition;
    }

    public boolean isDaytime() {
        return isDay != null && isDay == 1;
    }

    public Integer getHumidity() {
        return humidity;
    }

    public Double getWindKph() {
        return windKph;
    }

    public String getWindDir() {
        return windDir;
    }

    public Integer getChanceOfRain() {
        return chanceOfRain;
    }

    public Double getPrecipMm() {
        return precipMm;
    }

    public Integer getCloud() {
        return cloud;
    }

    public Double getVisKm() {
        return visKm;
    }

    public Double getUv() {
        return uv;
    }
}
