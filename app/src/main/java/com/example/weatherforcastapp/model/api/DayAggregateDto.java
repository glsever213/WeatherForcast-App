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
    @SerializedName("maxwind_kph")
    private Double maxwindKph;

    @SerializedName("avghumidity")
    private Double avgHumidity;

    @SerializedName("uv")
    private Double uv;
    public Double getAvgtempC() { return avgtempC; }
    public Integer getDailyChanceOfRain() { return dailyChanceOfRain; }
    public Double getMaxtempC() {return maxtempC;}
    public Double getMintempC() {
        return mintempC;
    }

    public ConditionDto getCondition() {
        return condition;
    }
    public Double getMaxwindKph() { return maxwindKph; }
    public Double getAvghumidity() { return avgHumidity; }
    public Double getUv() { return uv; }

}
