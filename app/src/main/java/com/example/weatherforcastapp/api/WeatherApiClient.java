package com.example.weatherforcastapp.api;

import com.example.weatherforcastapp.BuildConfig;

import java.util.concurrent.TimeUnit;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Retrofit + OkHttp: gắn {@code key} từ {@link BuildConfig#WEATHERAPI_KEY} (khai báo trong {@code local.properties}).
 *
 * @see <a href="https://www.weatherapi.com/docs/">WeatherAPI.com Docs</a>
 */
public final class WeatherApiClient {

    private static final String BASE_URL = "https://api.weatherapi.com/v1/";

    private static volatile Retrofit retrofit;

    private WeatherApiClient() {
    }

    public static Retrofit retrofit() {
        if (retrofit == null) {
            synchronized (WeatherApiClient.class) {
                if (retrofit == null) {
                    retrofit = buildRetrofit();
                }
            }
        }
        return retrofit;
    }

    public static WeatherApiService api() {
        return retrofit().create(WeatherApiService.class);
    }

    private static Retrofit buildRetrofit() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BASIC);

        Interceptor keyInterceptor = chain -> {
            Request request = chain.request();
            HttpUrl url = request.url().newBuilder()
                    .addQueryParameter("key", BuildConfig.WEATHERAPI_KEY)
                    .build();
            return chain.proceed(request.newBuilder().url(url).build());
        };

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(20, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .addInterceptor(keyInterceptor)
                .addInterceptor(logging)
                .build();

        return new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }
}
