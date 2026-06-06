package com.example.weatherforcastapp.model.api;

import com.google.gson.annotations.SerializedName;

public final class HourItemDto {
    @SerializedName("time")
    private String time;

    @SerializedName("temp_c")
    private Double tempC;

    @SerializedName("condition")
    private ConditionDto condition;

    @SerializedName("is_day")
    private Integer isDay;

    public String getTime() {
        return time;
    }

    public Double getTempC() {
        return tempC;
    }

    public ConditionDto getCondition() {
        return condition;
    }

    public boolean isDaytime() {
        return isDay != null && isDay == 1;
    }
}