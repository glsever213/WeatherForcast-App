package com.example.weatherforcastapp.api;

import androidx.annotation.Nullable;

/**
 * Ảnh điều kiện WeatherAPI — ưu tiên URL đầy đủ / relative trong {@code condition.icon}.
 * Nếu tự ghép CDN: segment tên file là <strong>số icon</strong> (113, 389, …), không phải
 * {@code condition.code} (1000, 1276, …). Xem {@code weather_conditions.json} trên docs.
 * CDN: {@code https://cdn.weatherapi.com/weather/{size}x{size}/day|night/{iconId}.png}
 */
public final class WeatherApiIcons {

    public static final int SIZE_LIST = 128;
    public static final int SIZE_HERO = 256;

    private WeatherApiIcons() {
    }

    /**
     * @param iconFromApi đường dẫn API ({@code condition.icon}), hoặc chỉ số icon trong path CDN (vd {@code 113}), không dùng {@code condition.code}.
     * @param day         true = /day/, false = /night/
     */
    public static String url(@Nullable String iconFromApi, boolean day, int sizePx) {
        if (iconFromApi == null || iconFromApi.isEmpty()) {
            return null;
        }
        String s = iconFromApi.trim();
        if (s.startsWith("http://") || s.startsWith("https://")) {
            return s;
        }
        if (s.startsWith("//")) {
            return "https:" + s;
        }
        String dn = day ? "day" : "night";
        return "https://cdn.weatherapi.com/weather/" + sizePx + "x" + sizePx + "/" + dn + "/" + s + ".png";
    }

    public static String urlDayIconCode(@Nullable String code, int sizePx) {
        return url(code, true, sizePx);
    }
}
