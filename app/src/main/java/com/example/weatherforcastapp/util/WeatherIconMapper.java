package com.example.weatherforcastapp.util;

import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;

import com.example.weatherforcastapp.R;

public final class WeatherIconMapper {

    private WeatherIconMapper() {
    }

    @DrawableRes
    public static int fromConditionCode(@Nullable Integer conditionCode, boolean isDaytime) {
        if (!isDaytime) {
            return R.drawable.ic_weather_moon;
        }
        if (conditionCode == null) {
            return R.drawable.ic_weather_cloud;
        }

        int code = conditionCode;
        if (code == 1000) {
            return R.drawable.ic_weather_sunny;
        }
        if (code >= 1241 && code <= 1282) {
            return R.drawable.ic_weather_storm;
        }
        if ((code >= 1063 && code <= 1201) || (code >= 1204 && code <= 1240)) {
            return R.drawable.ic_weather_rain;
        }
        if (code >= 1003 && code <= 1009 || code == 1030 || code == 1135 || code == 1147) {
            return R.drawable.ic_weather_cloud;
        }
        return R.drawable.ic_weather_cloud;
    }

    @DrawableRes
    public static int fromWeatherApiIcon(@Nullable String iconValue, boolean daytimeFallback) {
        String value = iconValue == null ? "" : iconValue.trim().toLowerCase();
        boolean isDaytime = !value.contains("/night/") && daytimeFallback;
        if (value.contains("/night/")) {
            isDaytime = false;
        }

        Integer iconId = parseIconId(value);
        if (iconId == null) {
            return fromConditionCode(null, isDaytime);
        }

        switch (iconId) {
            case 113:
                return isDaytime ? R.drawable.ic_weather_sunny : R.drawable.ic_weather_moon;
            case 116:
            case 119:
            case 122:
            case 143:
            case 248:
            case 260:
                return R.drawable.ic_weather_cloud;
            case 176:
            case 263:
            case 266:
            case 281:
            case 284:
            case 293:
            case 296:
            case 299:
            case 302:
            case 305:
            case 308:
            case 311:
            case 314:
            case 317:
            case 320:
            case 353:
            case 356:
            case 359:
                return R.drawable.ic_weather_rain;
            case 200:
            case 386:
            case 389:
            case 392:
            case 395:
                return R.drawable.ic_weather_storm;
            default:
                return R.drawable.ic_weather_cloud;
        }
    }

    @Nullable
    private static Integer parseIconId(@Nullable String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }

        String cleaned = value;
        int slash = cleaned.lastIndexOf('/');
        if (slash >= 0 && slash + 1 < cleaned.length()) {
            cleaned = cleaned.substring(slash + 1);
        }
        int dot = cleaned.indexOf('.');
        if (dot > 0) {
            cleaned = cleaned.substring(0, dot);
        }

        try {
            return Integer.parseInt(cleaned);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }
}
