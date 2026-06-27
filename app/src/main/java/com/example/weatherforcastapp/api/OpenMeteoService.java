package com.example.weatherforcastapp.api;

import com.example.weatherforcastapp.model.api.OpenMeteoResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface OpenMeteoService {
    @GET("v1/search")
    Call<OpenMeteoResponse> searchLocation(@Query("name") String name, @Query("count") int count);
}
