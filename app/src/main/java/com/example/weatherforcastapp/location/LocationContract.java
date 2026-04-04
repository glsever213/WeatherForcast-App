package com.example.weatherforcastapp.location;

import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Chuẩn thống nhất toàn project cho lat/lon + tên hiển thị + luồng (onboarding / quản lý).
 * Mọi Activity truyền/nhận vị trí nên dùng các hằng và helper dưới đây.
 */
public final class LocationContract {

    public static final String EXTRA_LAT = "weather.loc.lat";
    public static final String EXTRA_LON = "weather.loc.lon";
    public static final String EXTRA_DISPLAY_NAME = "weather.loc.display_name";
    /** {@link #FLOW_ONBOARDING} hoặc {@link #FLOW_MANAGEMENT} — dùng ở Preview để biết nút FAB xử lý thế nào */
    public static final String EXTRA_FLOW_MODE = "weather.loc.flow_mode";

    public static final int FLOW_ONBOARDING = 1;
    public static final int FLOW_MANAGEMENT = 2;

    private LocationContract() {
    }

    public static void putLocation(@NonNull Intent intent, double lat, double lon, @Nullable String displayName) {
        intent.putExtra(EXTRA_LAT, lat);
        intent.putExtra(EXTRA_LON, lon);
        intent.putExtra(EXTRA_DISPLAY_NAME, displayName != null ? displayName : "");
    }

    public static void putFlowMode(@NonNull Intent intent, int flowMode) {
        intent.putExtra(EXTRA_FLOW_MODE, flowMode);
    }

    public static double readLat(@NonNull Intent intent, double defaultValue) {
        return intent.getDoubleExtra(EXTRA_LAT, defaultValue);
    }

    public static double readLon(@NonNull Intent intent, double defaultValue) {
        return intent.getDoubleExtra(EXTRA_LON, defaultValue);
    }

    @Nullable
    public static String readDisplayName(@NonNull Intent intent) {
        return intent.getStringExtra(EXTRA_DISPLAY_NAME);
    }

    public static int readFlowMode(@NonNull Intent intent, int defaultMode) {
        return intent.getIntExtra(EXTRA_FLOW_MODE, defaultMode);
    }
}
