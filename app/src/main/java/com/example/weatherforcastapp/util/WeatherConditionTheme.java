package com.example.weatherforcastapp.util;

import androidx.annotation.Nullable;

/**
 * Map condition code của WeatherAPI.com sang bộ 3 màu gradient
 * dùng cho {@link com.example.weatherforcastapp.ui.widget.GradientShiftBackgroundView}.
 *
 * WeatherAPI dùng các nhóm code sau (xem docs chính thức):
 *  - 1000              : Sunny / Clear
 *  - 1003–1009         : Partly cloudy → Overcast
 *  - 1030, 1135, 1147  : Mist / Fog
 *  - 1063–1201         : Mưa các mức (drizzle → heavy rain)
 *  - 1204–1237         : Sleet / Ice
 *  - 1240–1282         : Shower / Thunderstorm
 *
 * Hiện tại phân 4 nhóm tương ứng 4 bộ màu đã có trong colors.xml:
 *  SUNNY   → weather_type1 (xanh sáng — nắng)
 *  CLOUDY  → weather_type3 (xám xanh — mây)
 *  RAINY   → weather_type2 (tím đêm — mưa)
 *  NIGHT   → weather_type2 (tối — ban đêm, không phụ thuộc condition)
 *
 * Khi Nguyễn Công Huy bổ sung thêm bộ màu, chỉ cần thêm case ở đây.
 */
public final class WeatherConditionTheme {

    /**
     * Bộ 3 màu gradient: top → mid → bottom.
     * Dùng trực tiếp với {@code GradientShiftBackgroundView.setGradientColors}.
     */
    public static final class Colors {
        public final int top;
        public final int mid;
        public final int bottom;

        Colors(int top, int mid, int bottom) {
            this.top = top;
            this.mid = mid;
            this.bottom = bottom;
        }
    }

    // --- Bộ màu tương ứng colors.xml ---

    /** Nắng / Quang đãng — weather_type1 */
    private static final Colors SUNNY = new Colors(
            0xFF47BFDF,   // weather_type1_start
            0xFF4A91FF,   // weather_type1_end (dùng làm mid)
            0xFF1E4A9A    // xanh đậm phía dưới
    );

    /** Nhiều mây / U ám — weather_type3 */
    private static final Colors CLOUDY = new Colors(
            0xFF838BAA,   // weather_type3
            0xFF636B8A,
            0xFF444E72    // weather_type2
    );

    /** Mưa / Dông — weather_type2 */
    private static final Colors RAINY = new Colors(
            0xFF444E72,   // weather_type2
            0xFF333B5E,
            0xFF1E2440
    );

    /** Ban đêm (overrides condition) */
    private static final Colors NIGHT = new Colors(
            0xFF1A1F3C,
            0xFF0D1128,
            0xFF050810
    );

    private WeatherConditionTheme() {
    }

    /**
     * Trả về bộ màu phù hợp dựa trên condition code và thời điểm trong ngày.
     *
     * @param conditionCode code từ {@code ConditionDto.getCode()}; null → SUNNY mặc định
     * @param isDaytime     true nếu ban ngày (từ {@code CurrentDto.isDaytime()})
     */
    public static Colors resolve(@Nullable Integer conditionCode, boolean isDaytime) {
        if (!isDaytime) {
            return NIGHT;
        }
        if (conditionCode == null) {
            return SUNNY;
        }

        int code = conditionCode;

        // Nắng / Trời quang
        if (code == 1000) {
            return SUNNY;
        }

        // Ít mây đến nhiều mây — vẫn còn tươi sáng
        if (code >= 1003 && code <= 1009) {
            return CLOUDY;
        }

        // Sương mù / Mist / Fog
        if (code == 1030 || code == 1135 || code == 1147) {
            return CLOUDY;
        }

        // Mưa phùn, mưa nhẹ đến nặng (bao gồm freezing drizzle)
        if (code >= 1063 && code <= 1201) {
            return RAINY;
        }

        // Sleet / Ice pellets
        if (code >= 1204 && code <= 1237) {
            return RAINY;
        }

        // Shower / Thunderstorm
        if (code >= 1240 && code <= 1282) {
            return RAINY;
        }

        // Tuyết (Snow)
        if (code >= 1066 && code <= 1117) {
            return CLOUDY;
        }

        return SUNNY; // fallback
    }

    public enum AnimationType {
        SUNNY,
        CLOUDY,
        RAINY,
        STORM,
        NIGHT
    }

    public static AnimationType resolveAnimation(
            @Nullable Integer conditionCode,
            boolean isDaytime
    ) {

        if (!isDaytime) {
            return AnimationType.NIGHT;
        }

        if (conditionCode == null) {
            return AnimationType.SUNNY;
        }

        int code = conditionCode;

        if (code == 1000) {
            return AnimationType.SUNNY;
        }

        if (code >= 1003 && code <= 1030) {
            return AnimationType.CLOUDY;
        }

        if (code >= 1063 && code <= 1240) {
            return AnimationType.RAINY;
        }

        if (code >= 1241 && code <= 1282) {
            return AnimationType.STORM;
        }

        return AnimationType.SUNNY;
    }
}
