package com.example.weatherforcastapp.model.api;

import com.google.gson.annotations.SerializedName;

/**
 * Khối {@code condition} trong JSON WeatherAPI.com (current / day / hour).
 */
public final class ConditionDto {

    @SerializedName("text")
    private String text;

    @SerializedName("icon")
    private String icon;

    @SerializedName("code")
    private Integer code;

    public String getText() {
        return text;
    }

    public String getIcon() {
        return icon;
    }

    public Integer getCode() {
        return code;
    }
}
