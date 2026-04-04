package com.example.weatherforcastapp.ui;

import java.util.ArrayList;
import java.util.List;

/**
 * Dữ liệu mẫu — chuỗi thứ 3 phải là <strong>số icon</strong> trong URL CDN (giống segment trong
 * {@code condition.icon}, vd {@code .../day/389.png}), <em>không</em> phải {@code condition.code}
 * (vd 1276 → icon 389; dùng 1276 làm tên file CDN sẽ 404).
 */
public final class Forecast5dSamples {

    private Forecast5dSamples() {
    }

    public static List<DailyForecastAdapter.Slot> defaultSlots() {
        List<DailyForecastAdapter.Slot> daily = new ArrayList<>();
        daily.add(new DailyForecastAdapter.Slot("13/9", "21°", "389"));
        daily.add(new DailyForecastAdapter.Slot("14/9", "34°", "113"));
        daily.add(new DailyForecastAdapter.Slot("15/9", "30°", "119"));
        daily.add(new DailyForecastAdapter.Slot("16/9", "28°", "122"));
        daily.add(new DailyForecastAdapter.Slot("17/9", "27°", "116"));
        return daily;
    }
}
