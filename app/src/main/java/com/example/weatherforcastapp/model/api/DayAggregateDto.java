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
    @SerializedName("daily_chance_of_rain")
    private Integer dailyChanceOfRain;
    @SerializedName("avgtemp_c")
    private Double avgtempC;

    public Double getAvgtempC() { return avgtempC; }
    public Integer getDailyChanceOfRain() { return dailyChanceOfRain; }
    public Double getMaxtempC() {return maxtempC;}

    public Double getMintempC() {
        return mintempC;
    }

    public ConditionDto getCondition() {
        return condition;
    }
}
