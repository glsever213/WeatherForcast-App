package com.example.weatherforcastapp.data;

import androidx.annotation.NonNull;

/** Chip thành phố phổ biến — tọa độ gần đúng để demo khung. */
public final class PopularCity {

    public final String name;
    public final double lat;
    public final double lon;
    public final boolean primaryStyle;

    public PopularCity(String name, double lat, double lon, boolean primaryStyle) {
        this.name = name;
        this.lat = lat;
        this.lon = lon;
        this.primaryStyle = primaryStyle;
    }

    @NonNull
    @Override
    public String toString() {
        return name;
    }
}
