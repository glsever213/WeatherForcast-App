package com.example.weatherforcastapp.model.api;

import com.google.gson.annotations.SerializedName;

/** Phần {@code current} trong phản hồi current.json / forecast.json. */
public final class CurrentDto {

    private Double uv;
    private Integer humidity;
    @SerializedName("feelslike_c")
    private Double feelslikeC;
    @SerializedName("wind_kph")
    private Double windKph;
    @SerializedName("pressure_mb")
    private Double pressureMb;
    @SerializedName("air_quality")
    private AirQualityDto airQuality;


    public Double getUv() { return uv; }
    public Integer getHumidity() { return humidity; }
    public Double getFeelslikeC() { return feelslikeC; }
    public Double getWindKph() { return windKph; }
    public Double getPressureMb() { return pressureMb; }
    public AirQualityDto getAirQuality() { return airQuality; }

    @SerializedName("temp_c")
    private Double tempC;

    @SerializedName("condition")
    private ConditionDto condition;

    /** 1 = ban ngày, 0 = ban đêm — dùng chọn icon day/night. */
    @SerializedName("is_day")
    private Integer isDay;

    public Double getTempC() {
        return tempC;
    }

    public ConditionDto getCondition() {
        return condition;
    }

    public Integer getIsDay() {
        return isDay;
    }

    public boolean isDaytime() {
        return isDay != null && isDay == 1;
    }

    public class AirQualityDto {
        @SerializedName("us-epa-index")
        private int usEpaIndex;

        public int getUsEpaIndex() {
            return usEpaIndex;
        }
    }
}
