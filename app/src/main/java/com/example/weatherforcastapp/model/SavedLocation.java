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
    private String cachedSummaryLine;
    private String cachedTemp;        // Ví dụ: "26°"
    private String cachedHighLow;     // Ví dụ: "29° / 24°"
    private String cachedIconCode;    // Ví dụ: "113"
    private String cachedHumidity;    // Ví dụ: "80%"

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
        return cachedSummaryLine != null ? cachedSummaryLine : "";
    }

    public void setCachedSummaryLine(String cachedSummaryLine) {
        this.cachedSummaryLine = cachedSummaryLine;
    }

    public String getCachedTemp() {
        return cachedTemp != null ? cachedTemp : "—°";
    }

    public void setCachedTemp(String cachedTemp) {
        this.cachedTemp = cachedTemp;
    }

    public String getCachedHighLow() {
        return cachedHighLow != null ? cachedHighLow : "— / —";
    }

    public void setCachedHighLow(String cachedHighLow) {
        this.cachedHighLow = cachedHighLow;
    }

    public String getCachedIconCode() {
        return cachedIconCode;
    }

    public void setCachedIconCode(String cachedIconCode) {
        this.cachedIconCode = cachedIconCode;
    }

    public String getCachedHumidity() {
        return cachedHumidity != null ? cachedHumidity : "—%";
    }

    public void setCachedHumidity(String cachedHumidity) {
        this.cachedHumidity = cachedHumidity;
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
