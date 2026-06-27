package com.example.weatherforcastapp.model.api;

import com.google.gson.annotations.SerializedName;

public final class AstroDto {
    @SerializedName("sunrise")
    private String sunrise;

    @SerializedName("sunset")
    private String sunset;

    @SerializedName("moonrise")
    private String moonrise;

    @SerializedName("moonset")
    private String moonset;

    @SerializedName("moon_phase")
    private String moonPhase;

    @SerializedName("moon_illumination")
    private String moonIllumination;

    public String getSunrise() {
        return sunrise;
    }

    public String getSunset() {
        return sunset;
    }

    public String getMoonrise() {
        return moonrise;
    }

    public String getMoonset() {
        return moonset;
    }

    public String getMoonPhase() {
        return moonPhase;
    }

    public String getMoonIllumination() {
        return moonIllumination;
    }
}
