package com.example.weatherforcastapp.model;

import java.io.Serializable;
import java.util.Locale;
import java.util.Objects;

/**
 * Địa điểm đã lưu (tối đa 20) — serialize Gson/JSON trong {@link com.example.weatherforcastapp.prefs.WeatherPreferences}.
 */
public final class SavedLocation implements Serializable {

    private final String id;
    private final String displayName;
    private final double latitude;
    private final double longitude;
    private final String cachedSummaryLine;

    public SavedLocation(String displayName, double latitude, double longitude, String cachedSummaryLine) {
        this.id = buildId(latitude, longitude);
        this.displayName = displayName;
        this.latitude = latitude;
        this.longitude = longitude;
        this.cachedSummaryLine = cachedSummaryLine != null ? cachedSummaryLine : "";
    }

    public static String buildId(double lat, double lon) {
        return String.format(Locale.US, "%.5f_%.5f", lat, lon);
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getCachedSummaryLine() {
        return cachedSummaryLine;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SavedLocation that = (SavedLocation) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
