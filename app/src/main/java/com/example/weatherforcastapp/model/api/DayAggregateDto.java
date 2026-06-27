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
    @SerializedName("totalprecip_mm")
    private Double totalprecipMm;
    @SerializedName("avghumidity")
    private Integer avghumidity;
    @SerializedName("maxhumidity")
    private Integer maxhumidity;
    @SerializedName("minhumidity")
    private Integer minhumidity;
    @SerializedName("uv")
    private Double uv;
    @SerializedName("daily_will_it_rain")
    private Integer dailyWillItRain;
    @SerializedName("daily_chance_of_snow")
    private Integer dailyChanceOfSnow;
    @SerializedName("daily_will_it_snow")
    private Integer dailyWillItSnow;
    @SerializedName("totalsnow_cm")
    private Double totalsnowCm;
    @SerializedName("avgvis_km")
    private Double avgvisKm;

    public Double getAvgtempC() { return avgtempC; }
    public Integer getDailyChanceOfRain() { return dailyChanceOfRain; }
    public Double getMaxtempC() { return maxtempC; }
    public Double getMaxwindKph() { return maxwindKph; }
    public Double getTotalprecipMm() { return totalprecipMm; }
    public Integer getAvghumidity() { return avghumidity; }
    public Integer getMaxhumidity() { return maxhumidity; }
    public Integer getMinhumidity() { return minhumidity; }
    public Double getUv() { return uv; }
    public Integer getDailyWillItRain() { return dailyWillItRain; }
    public Integer getDailyChanceOfSnow() { return dailyChanceOfSnow; }
    public Integer getDailyWillItSnow() { return dailyWillItSnow; }
    public Double getTotalsnowCm() { return totalsnowCm; }
    public Double getAvgvisKm() { return avgvisKm; }

    public Double getMintempC() {
        return mintempC;
    }

    public ConditionDto getCondition() {
        return condition;
    }
}
