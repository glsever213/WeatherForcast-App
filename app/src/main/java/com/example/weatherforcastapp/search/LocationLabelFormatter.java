package com.example.weatherforcastapp.search;

import androidx.annotation.NonNull;

import com.example.weatherforcastapp.model.api.LocationDto;

/** Helper để đổi Location API thành nhãn hiển thị ngắn, gọn, đồng nhất. */
public final class LocationLabelFormatter {

    private LocationLabelFormatter() {
    }

    @NonNull
    public static String displayName(@NonNull LocationDto dto) {
        String name = safe(dto.getName());
        String region = safe(dto.getRegion());
        String country = safe(dto.getCountry());

        StringBuilder sb = new StringBuilder();
        if (!name.isEmpty()) sb.append(name);
        if (!region.isEmpty()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(region);
        }
        if (!country.isEmpty()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(country);
        }
        return sb.length() > 0 ? sb.toString() : "—";
    }

    @NonNull
    public static String subtitle(@NonNull LocationDto dto) {
        String region = safe(dto.getRegion());
        String country = safe(dto.getCountry());

        if (!region.isEmpty() && !country.isEmpty()) {
            return region + " • " + country;
        }
        if (!country.isEmpty()) {
            return country;
        }
        return region;
    }

    private static String safe(String value) {
        return value == null ? "" : value.trim();
    }
}
