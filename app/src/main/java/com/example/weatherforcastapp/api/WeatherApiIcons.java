package com.example.weatherforcastapp.api;

import android.content.Context;
import android.widget.ImageView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;

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

    /** Chọn kích thước CDN (64/128/256) vừa đủ nét cho view hiển thị. */
    public static int sizeForViewPx(int viewPx) {
        int need = Math.min(256, Math.max(64, viewPx * 2));
        if (need <= 64) return 64;
        if (need <= 128) return 128;
        return 256;
    }

    /** Tải icon thời tiết nét — decode đúng px hiển thị, không phóng to quá CDN. */
    public static void loadInto(
            @NonNull Context context,
            @NonNull ImageView view,
            @Nullable String iconFromApi,
            boolean day,
            float sizeDp,
            @DrawableRes int placeholder
    ) {
        int px = (int) (sizeDp * context.getResources().getDisplayMetrics().density + 0.5f);
        String iconUrl = url(iconFromApi, day, sizeForViewPx(px));
        if (iconUrl == null) {
            view.setImageResource(placeholder);
            return;
        }
        Glide.with(context)
                .load(iconUrl)
                .override(px, px)
                .fitCenter()
                .placeholder(placeholder)
                .error(placeholder)
                .into(view);
    }
}
