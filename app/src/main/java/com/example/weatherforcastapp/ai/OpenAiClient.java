package com.example.weatherforcastapp.ai;

import com.example.weatherforcastapp.BuildConfig;

import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Retrofit + OkHttp cho Groq qua endpoint tương thích OpenAI:
 * gắn header {@code Authorization: Bearer <GROQ_API_KEY>} (khai báo trong {@code local.properties}).
 *
 * @see <a href="https://console.groq.com/docs/openai">Groq OpenAI compatibility</a>
 */
public final class OpenAiClient {

    private static final String BASE_URL = "https://api.groq.com/openai/v1/";

    private static volatile Retrofit retrofit;

    private OpenAiClient() {
    }

    public static boolean hasApiKey() {
        return BuildConfig.GROQ_API_KEY != null && !BuildConfig.GROQ_API_KEY.trim().isEmpty();
    }

    public static OpenAiService api() {
        return retrofit().create(OpenAiService.class);
    }

    private static Retrofit retrofit() {
        if (retrofit == null) {
            synchronized (OpenAiClient.class) {
                if (retrofit == null) {
                    retrofit = build();
                }
            }
        }
        return retrofit;
    }

    private static Retrofit build() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BASIC);

        Interceptor authInterceptor = chain -> {
            Request original = chain.request();
            Request authed = original.newBuilder()
                    .header("Authorization", "Bearer " + BuildConfig.GROQ_API_KEY)
                    .header("Content-Type", "application/json")
                    .build();
            return chain.proceed(authed);
        };

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .addInterceptor(authInterceptor)
                .addInterceptor(logging)
                .build();

        return new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }
}
