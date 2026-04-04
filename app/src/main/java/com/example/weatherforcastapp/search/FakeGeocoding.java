package com.example.weatherforcastapp.search;

import androidx.annotation.Nullable;

import java.util.Locale;

/**
 * Dữ liệu giả — team thay bằng Geo API (vd search của WeatherAPI hoặc Google) khi bấm Enter.
 */
public final class FakeGeocoding {

    public static final class Result {
        public final String displayName;
        public final double lat;
        public final double lon;

        public Result(String displayName, double lat, double lon) {
            this.displayName = displayName;
            this.lat = lat;
            this.lon = lon;
        }
    }

    private FakeGeocoding() {
    }

    @Nullable
    public static Result search(String rawQuery) {
        if (rawQuery == null) return null;
        String q = rawQuery.trim();
        if (q.isEmpty()) return null;
        String key = q.toLowerCase(Locale.ROOT);
        if (key.contains("hà nội") || key.contains("ha noi") || key.equals("hn")) {
            return new Result("Hà Nội", 21.0285, 105.8542);
        }
        if (key.contains("hồ chí minh") || key.contains("ho chi minh") || key.contains("sg") || key.contains("sài gòn")) {
            return new Result("TP.HCM", 10.8231, 106.6297);
        }
        if (key.contains("đà nẵng") || key.contains("da nang")) {
            return new Result("Đà Nẵng", 16.0544, 108.2022);
        }
        if (key.contains("cẩm phả") || key.contains("cam pha")) {
            return new Result("Cẩm Phả", 21.0103, 107.2425);
        }
        return new Result(q, 16.0471, 108.2068);
    }
}
