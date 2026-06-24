package com.example.weatherforcastapp.model.api;

import com.google.gson.annotations.SerializedName;

/** Phần {@code current} trong phản hồi current.json / forecast.json. */
public final class CurrentDto {

    @SerializedName("temp_c")
    private Double tempC;

    @SerializedName("condition")
    private ConditionDto condition;

    /** 1 = ban ngày, 0 = ban đêm — dùng chọn icon day/night. */
    @SerializedName("is_day")
    private Integer isDay;

    @SerializedName("humidity")
    private Integer humidity;

    public Double getTempC() {
        return tempC;
    }

    public ConditionDto getCondition() {
        return condition;
    }

    public Integer getIsDay() {
        return isDay;
    }

    public Integer getHumidity() {
        return humidity;
    }

    public boolean isDaytime() {
        return isDay != null && isDay == 1;
    }
}
