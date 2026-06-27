package com.example.weatherforcastapp.model;

import androidx.annotation.Nullable;

import java.io.Serializable;
import java.util.Locale;
import java.util.Objects;

/**
 * Địa điểm đã lưu (tối đa 20) — serialize Gson/JSON trong {@link com.example.weatherforcastapp.prefs.WeatherPreferences}.
 */
public final class SavedLocation implements Serializable {

    private final String id;
    private String displayName;
    private final double latitude;
    private final double longitude;
    private String cachedSummaryLine;
    private String cachedTemp;        // Ví dụ: "26°"
    private String cachedHighLow;     // Ví dụ: "29° / 24°"
    private String cachedIconCode;    // Ví dụ: "113"
    private String cachedHumidity;    // Ví dụ: "80%"
    private Integer cachedConditionCode;
    private boolean cachedIsDaytime = true;
    private boolean fromGps;

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

    /** Cùng điểm (~8 km) — GPS vs geocoding search thường lệch vài phần mili độ. */
    public static boolean isSamePlace(double lat1, double lon1, double lat2, double lon2) {
        if (buildId(lat1, lon1).equals(buildId(lat2, lon2))) {
            return true;
        }
        return distanceKm(lat1, lon1, lat2, lon2) < 8.0;
    }

    private static double distanceKm(double lat1, double lon1, double lat2, double lon2) {
        double r = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return r * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        if (displayName != null && !displayName.isEmpty()) {
            this.displayName = displayName;
        }
    }

    public boolean isFromGps() {
        return fromGps;
    }

    public void setFromGps(boolean fromGps) {
        this.fromGps = fromGps;
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

    @Nullable
    public Integer getCachedConditionCode() {
        return cachedConditionCode;
    }

    public void setCachedConditionCode(@Nullable Integer cachedConditionCode) {
        this.cachedConditionCode = cachedConditionCode;
    }

    public boolean isCachedIsDaytime() {
        return cachedIsDaytime;
    }

    public void setCachedIsDaytime(boolean cachedIsDaytime) {
        this.cachedIsDaytime = cachedIsDaytime;
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
