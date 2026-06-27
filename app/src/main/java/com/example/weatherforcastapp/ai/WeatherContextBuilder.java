package com.example.weatherforcastapp.ai;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.weatherforcastapp.model.api.ApiForecastDayDto;
import com.example.weatherforcastapp.model.api.ConditionDto;
import com.example.weatherforcastapp.model.api.CurrentDto;
import com.example.weatherforcastapp.model.api.DayAggregateDto;
import com.example.weatherforcastapp.model.api.ForecastResponse;
import com.example.weatherforcastapp.model.api.HourItemDto;
import com.example.weatherforcastapp.model.api.LocationDto;

import java.util.List;
import java.util.Locale;

/**
 * Biến dữ liệu thời tiết của trang Home ({@link ForecastResponse}) thành một đoạn text gọn,
 * dùng làm "ngữ cảnh" (context) nạp vào prompt hệ thống của OpenAI.
 *
 * <p>Mục tiêu: chỉ gửi đúng dữ liệu cần thiết (không gửi cả JSON thô) để tiết kiệm token
 * và để model trả lời bám sát dữ liệu thật thay vì bịa.</p>
 */
public final class WeatherContextBuilder {

    private WeatherContextBuilder() {
    }

    @NonNull
    public static String build(@Nullable ForecastResponse r) {
        if (r == null || r.getCurrent() == null) {
            return "Hiện chưa có dữ liệu thời tiết được tải.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("DỮ LIỆU THỜI TIẾT HIỆN TẠI (nguồn: WeatherAPI, đã tải trên màn hình chính)\n");

        LocationDto loc = r.getLocation();
        if (loc != null) {
            sb.append("- Địa điểm: ").append(safe(loc.getName()));
            if (notEmpty(loc.getRegion())) sb.append(", ").append(loc.getRegion());
            if (notEmpty(loc.getCountry())) sb.append(", ").append(loc.getCountry());
            sb.append(String.format(Locale.US, " (lat %.4f, lon %.4f)", loc.getLat(), loc.getLon()));
            sb.append('\n');
        }

        CurrentDto cur = r.getCurrent();
        appendIfTemp(sb, "- Nhiệt độ hiện tại", cur.getTempC());
        appendIfTemp(sb, "- Cảm giác như", cur.getFeelslikeC());
        if (cur.getCondition() != null && notEmpty(cur.getCondition().getText())) {
            sb.append("- Tình trạng: ").append(cur.getCondition().getText()).append('\n');
        }
        if (cur.getHumidity() != null) sb.append("- Độ ẩm: ").append(cur.getHumidity()).append("%\n");
        if (cur.getWindKph() != null) sb.append("- Gió: ").append(fmt(cur.getWindKph())).append(" km/h\n");
        if (cur.getPressureMb() != null) sb.append("- Áp suất: ").append(fmt(cur.getPressureMb())).append(" mb\n");
        if (cur.getUv() != null) sb.append("- Chỉ số UV: ").append(fmt(cur.getUv())).append('\n');
        if (cur.getAirQuality() != null) {
            sb.append("- Chất lượng không khí (US EPA index 1-6): ")
                    .append(cur.getAirQuality().getUsEpaIndex()).append('\n');
        }
        sb.append("- Thời điểm: ").append(cur.isDaytime() ? "ban ngày" : "ban đêm").append('\n');

        if (r.getForecast() != null && r.getForecast().getForecastday() != null) {
            List<ApiForecastDayDto> days = r.getForecast().getForecastday();
            if (!days.isEmpty()) {
                sb.append("\nDỰ BÁO THEO NGÀY:\n");
                for (ApiForecastDayDto d : days) {
                    if (d == null) continue;
                    sb.append("- ").append(safe(d.getDate())).append(": ");
                    DayAggregateDto day = d.getDay();
                    if (day != null) {
                        sb.append(String.format(Locale.US, "%s°C ~ %s°C",
                                fmt(day.getMintempC()), fmt(day.getMaxtempC())));
                        ConditionDto c = day.getCondition();
                        if (c != null && notEmpty(c.getText())) sb.append(", ").append(c.getText());
                        if (day.getDailyChanceOfRain() != null) {
                            sb.append(", khả năng mưa ").append(day.getDailyChanceOfRain()).append('%');
                        }
                    }
                    if (d.getAstro() != null) {
                        sb.append(" (bình minh ").append(safe(d.getAstro().getSunrise()))
                                .append(", hoàng hôn ").append(safe(d.getAstro().getSunset())).append(')');
                    }
                    sb.append('\n');
                }

                ApiForecastDayDto today = days.get(0);
                if (today != null && today.getHour() != null && !today.getHour().isEmpty()) {
                    sb.append("\nNHIỆT ĐỘ THEO GIỜ (hôm nay, mỗi 3 giờ):\n");
                    List<HourItemDto> hours = today.getHour();
                    for (int i = 0; i < hours.size(); i += 3) {
                        HourItemDto h = hours.get(i);
                        if (h == null) continue;
                        sb.append("  ").append(shortHour(h.getTime())).append(": ")
                                .append(fmt(h.getTempC())).append("°C");
                        if (h.getCondition() != null && notEmpty(h.getCondition().getText())) {
                            sb.append(" - ").append(h.getCondition().getText());
                        }
                        sb.append('\n');
                    }
                }
            }
        }

        return sb.toString();
    }

    private static void appendIfTemp(StringBuilder sb, String label, @Nullable Double v) {
        if (v != null) sb.append(label).append(": ").append(fmt(v)).append("°C\n");
    }

    private static String shortHour(@Nullable String time) {
        if (time == null) return "?";
        int sp = time.indexOf(' ');
        return sp >= 0 && sp + 1 < time.length() ? time.substring(sp + 1) : time;
    }

    private static String fmt(@Nullable Double v) {
        return v == null ? "?" : String.format(Locale.US, "%.0f", v);
    }

    private static boolean notEmpty(@Nullable String s) {
        return s != null && !s.isEmpty();
    }

    private static String safe(@Nullable String s) {
        return s == null ? "?" : s;
    }
}
