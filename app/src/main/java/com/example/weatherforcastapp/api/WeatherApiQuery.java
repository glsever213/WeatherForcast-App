package com.example.weatherforcastapp.api;

/**
 * Tham số {@code q} của WeatherAPI — xem
 * <a href="https://www.weatherapi.com/docs/">tài liệu</a> (Latitude and Longitude decimal: {@code q=48.8567,2.3508}).
 */
public final class WeatherApiQuery {

    private WeatherApiQuery() {
    }

    /** Trả về chuỗi {@code lat,lon} không khoảng trắng thừa. */
    public static String latLon(double lat, double lon) {
        return lat + "," + lon;
    }
}
