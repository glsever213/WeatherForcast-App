package com.example.weatherforcastapp.model.api;

import com.google.gson.annotations.SerializedName;

/** Phần {@code location} trong phản hồi WeatherAPI. */
public final class LocationDto {

    @SerializedName("name")
    private String name;

    @SerializedName("region")
    private String region;

    @SerializedName("country")
    private String country;

    @SerializedName("lat")
    private double lat;

    @SerializedName("lon")
    private double lon;

    public String getName() {
        return name;
    }

    public String getRegion() {
        return region;
    }

    public String getCountry() {
        return country;
    }

    public double getLat() {
        return lat;
    }

    public double getLon() {
        return lon;
    }
}
