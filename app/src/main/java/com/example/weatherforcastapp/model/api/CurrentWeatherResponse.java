package com.example.weatherforcastapp.model.api;

import com.google.gson.annotations.SerializedName;

/** Phản hồi {@code current.json} (chỉ location + current). */
public final class CurrentWeatherResponse {

    @SerializedName("location")
    private LocationDto location;

    @SerializedName("current")
    private CurrentDto current;

    public LocationDto getLocation() {
        return location;
    }

    public CurrentDto getCurrent() {
        return current;
    }
}
