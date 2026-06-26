package com.example.weatherforcastapp.api;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public final class OpenMeteoClient {
    private static final String BASE_URL = "https://geocoding-api.open-meteo.com/";
    private static volatile OpenMeteoService service;
    private OpenMeteoClient() {}
    public static OpenMeteoService api() {
        if (service == null) {
            synchronized (OpenMeteoClient.class) {
                if (service == null) {
                    service = new Retrofit.Builder().baseUrl(BASE_URL)
                            .addConverterFactory(GsonConverterFactory.create())
                            .build().create(OpenMeteoService.class);
                }
            }
        }
        return service;
    }
}
