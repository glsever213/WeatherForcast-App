package com.example.weatherforcastapp.prefs;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.example.weatherforcastapp.model.SavedLocation;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * SharedPreferences: danh sách địa điểm (CRUD, tối đa 20), vị trí hiện tại.
 */
public final class WeatherPreferences {

    public static final int MAX_SAVED_LOCATIONS = 20;

    private static final String PREFS_NAME = "weather_forcast_prefs";
    private static final String KEY_LOCATIONS_JSON = "locations_json";
    private static final String KEY_CURRENT_LAT = "current_lat";
    private static final String KEY_CURRENT_LON = "current_lon";
    private static final String KEY_CURRENT_NAME = "current_name";
    private static final String KEY_CURRENT_FROM_GPS = "current_from_gps";
    private static final String KEY_GPS_MARKER_LAT = "gps_marker_lat";
    private static final String KEY_GPS_MARKER_LON = "gps_marker_lon";

    private static final Gson GSON = new Gson();
    private static final Type LOCATION_LIST_TYPE = new TypeToken<List<SavedLocation>>() {
    }.getType();

    private final SharedPreferences prefs;

    public WeatherPreferences(Context appContext) {
        this.prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static WeatherPreferences get(Context context) {
        return new WeatherPreferences(context.getApplicationContext());
    }

    public boolean hasCurrentLocation() {
        return prefs.contains(KEY_CURRENT_LAT) && prefs.contains(KEY_CURRENT_LON);
    }

    public void setCurrentLocation(double lat, double lon, String displayName) {
        setCurrentLocation(lat, lon, displayName, false);
    }

    public void setCurrentLocation(double lat, double lon, String displayName, boolean fromGps) {
        prefs.edit()
                .putFloat(KEY_CURRENT_LAT, (float) lat)
                .putFloat(KEY_CURRENT_LON, (float) lon)
                .putString(KEY_CURRENT_NAME, displayName != null ? displayName : "")
                .putBoolean(KEY_CURRENT_FROM_GPS, fromGps)
                .apply();
    }

    public boolean isCurrentFromGps() {
        return prefs.getBoolean(KEY_CURRENT_FROM_GPS, false);
    }

    public boolean hasGpsMarker() {
        return prefs.contains(KEY_GPS_MARKER_LAT) && prefs.contains(KEY_GPS_MARKER_LON);
    }

    public double getGpsMarkerLat() {
        return prefs.getFloat(KEY_GPS_MARKER_LAT, 0f);
    }

    public double getGpsMarkerLon() {
        return prefs.getFloat(KEY_GPS_MARKER_LON, 0f);
    }

    /** Ghi nhớ vị trí lấy từ GPS — dùng hiển thị icon pin trên list. */
    public void markGpsLocation(double lat, double lon) {
        prefs.edit()
                .putFloat(KEY_GPS_MARKER_LAT, (float) lat)
                .putFloat(KEY_GPS_MARKER_LON, (float) lon)
                .apply();
        SavedLocation existing = findByCoords(lat, lon);
        if (existing != null) {
            existing.setFromGps(true);
            addOrUpdateLocation(existing);
            return;
        }
        SavedLocation loc = new SavedLocation(
                hasCurrentLocation() ? getCurrentName() : "",
                lat,
                lon,
                ""
        );
        loc.setFromGps(true);
        addOrUpdateLocation(loc);
    }

    public boolean isGpsMarkerLocation(double lat, double lon) {
        if (!hasGpsMarker()) {
            return false;
        }
        return SavedLocation.isSamePlace(lat, lon, getGpsMarkerLat(), getGpsMarkerLon());
    }

    public double getCurrentLat() {
        try {
            return prefs.getFloat(KEY_CURRENT_LAT, 0f);
        } catch (Exception e) {
            return 0d;
        }
    }

    public double getCurrentLon() {
        try {
            return prefs.getFloat(KEY_CURRENT_LON, 0f);
        } catch (Exception e) {
            return 0d;
        }
    }

    public String getCurrentName() {
        return prefs.getString(KEY_CURRENT_NAME, "");
    }

    public void clearCurrentLocation() {
        prefs.edit()
                .remove(KEY_CURRENT_LAT)
                .remove(KEY_CURRENT_LON)
                .remove(KEY_CURRENT_NAME)
                .remove(KEY_CURRENT_FROM_GPS)
                .apply();
    }

    public List<SavedLocation> getSavedLocations() {
        String json = prefs.getString(KEY_LOCATIONS_JSON, "[]");
        List<SavedLocation> list = GSON.fromJson(json, LOCATION_LIST_TYPE);
        if (list == null) {
            return new ArrayList<>();
        }
        List<SavedLocation> deduped = dedupeByPlace(list);
        if (deduped.size() != list.size()) {
            saveLocations(deduped);
        }
        return deduped;
    }

    @Nullable
    public SavedLocation findByCoords(double lat, double lon) {
        for (SavedLocation loc : getSavedLocationsRaw()) {
            if (SavedLocation.isSamePlace(lat, lon, loc.getLatitude(), loc.getLongitude())) {
                return loc;
            }
        }
        return null;
    }

    /** Đảm bảo vị trí đang xem (Home) cũng có trong danh sách đã lưu. */
    public void syncCurrentLocationToSavedList() {
        if (!hasCurrentLocation()) {
            return;
        }
        SavedLocation loc = new SavedLocation(
                getCurrentName(),
                getCurrentLat(),
                getCurrentLon(),
                ""
        );
        if (isCurrentFromGps()) {
            loc.setFromGps(true);
            markGpsLocation(getCurrentLat(), getCurrentLon());
        } else {
            addOrUpdateLocation(loc);
        }
    }

    private List<SavedLocation> getSavedLocationsRaw() {
        String json = prefs.getString(KEY_LOCATIONS_JSON, "[]");
        List<SavedLocation> list = GSON.fromJson(json, LOCATION_LIST_TYPE);
        return list != null ? new ArrayList<>(list) : new ArrayList<>();
    }

    private List<SavedLocation> dedupeByPlace(List<SavedLocation> list) {
        List<SavedLocation> out = new ArrayList<>();
        for (SavedLocation loc : list) {
            SavedLocation existing = null;
            for (SavedLocation kept : out) {
                if (SavedLocation.isSamePlace(
                        loc.getLatitude(), loc.getLongitude(),
                        kept.getLatitude(), kept.getLongitude())) {
                    existing = kept;
                    break;
                }
            }
            if (existing != null) {
                mergeInto(existing, loc);
            } else {
                out.add(loc);
            }
        }
        return out;
    }

    private void mergeInto(SavedLocation target, SavedLocation incoming) {
        if (incoming.isFromGps()) {
            target.setFromGps(true);
        }
        String name = incoming.getDisplayName();
        if (name != null && !name.isEmpty() && !incoming.isFromGps()) {
            target.setDisplayName(name);
        }
        if (target.getCachedSummaryLine().isEmpty() && !incoming.getCachedSummaryLine().isEmpty()) {
            target.setCachedSummaryLine(incoming.getCachedSummaryLine());
        }
    }

    private void saveLocations(List<SavedLocation> list) {
        prefs.edit().putString(KEY_LOCATIONS_JSON, GSON.toJson(list)).apply();
    }

    public boolean addOrUpdateLocation(SavedLocation location) {
        List<SavedLocation> list = getSavedLocationsRaw();
        SavedLocation byPlace = findInList(list, location.getLatitude(), location.getLongitude());
        if (byPlace != null) {
            mergeInto(byPlace, location);
            saveLocations(list);
            return true;
        }
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getId().equals(location.getId())) {
                mergeInto(list.get(i), location);
                saveLocations(list);
                return true;
            }
        }
        if (list.size() >= MAX_SAVED_LOCATIONS) {
            return false;
        }
        list.add(location);
        saveLocations(list);
        return true;
    }

    @Nullable
    private SavedLocation findInList(List<SavedLocation> list, double lat, double lon) {
        for (SavedLocation loc : list) {
            if (SavedLocation.isSamePlace(lat, lon, loc.getLatitude(), loc.getLongitude())) {
                return loc;
            }
        }
        return null;
    }

    public void removeLocation(String id) {
        if (TextUtils.isEmpty(id)) return;
        List<SavedLocation> list = getSavedLocations();
        Iterator<SavedLocation> it = list.iterator();
        while (it.hasNext()) {
            if (id.equals(it.next().getId())) {
                it.remove();
                break;
            }
        }
        saveLocations(list);
    }

    public void removeLocations(Iterable<String> ids) {
        List<SavedLocation> list = getSavedLocations();
        for (String id : ids) {
            if (TextUtils.isEmpty(id)) continue;
            list.removeIf(loc -> id.equals(loc.getId()));
        }
        saveLocations(list);
    }

    public void removeAllLocations() {
        prefs.edit().putString(KEY_LOCATIONS_JSON, "[]").apply();
    }

    /**
     * Helper cho PreviewActivity kiểm tra xem vị trí đã lưu chưa
     */
    public boolean hasSavedLocationWithCoords(double lat, double lon) {
        return findByCoords(lat, lon) != null;
    }

    public boolean addLocation(SavedLocation location) {
        if (hasSavedLocationWithCoords(location.getLatitude(), location.getLongitude())) {
            return false;
        }
        List<SavedLocation> currentList = getSavedLocationsRaw();
        if (currentList.size() >= MAX_SAVED_LOCATIONS) {
            return false;
        }
        currentList.add(location);
        saveLocations(currentList);
        return true;
    }
}