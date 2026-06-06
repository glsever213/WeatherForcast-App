package com.example.weatherforcastapp.model.api;

import com.google.gson.annotations.SerializedName;

public final class AstroDto {
    @SerializedName("sunrise")
    private String sunrise;

    @SerializedName("sunset")
    private String sunset;

    public String getSunrise() {
        return sunrise;
    }

    public String getSunset() {
        return sunset;
    }
}
