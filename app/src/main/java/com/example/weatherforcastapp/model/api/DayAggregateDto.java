package com.example.weatherforcastapp.model.api;

import com.google.gson.annotations.SerializedName;

/** Khối {@code day} trong mỗi phần tử {@code forecastday}. */
public final class DayAggregateDto {

    @SerializedName("maxtemp_c")
    private Double maxtempC;

    @SerializedName("mintemp_c")
    private Double mintempC;

    @SerializedName("condition")
    private ConditionDto condition;

    public Double getMaxtempC() {
        return maxtempC;
    }

    public Double getMintempC() {
        return mintempC;
    }

    public ConditionDto getCondition() {
        return condition;
    }
}
