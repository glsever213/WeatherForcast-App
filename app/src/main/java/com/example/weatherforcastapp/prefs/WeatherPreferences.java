package com.example.weatherforcastapp.prefs;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.example.weatherforcastapp.model.SavedLocation;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * SharedPreferences: danh sách địa điểm (CRUD, tối đa 20), vị trí hiện tại, cache JSON API, throttle gọi API.
 */
public final class WeatherPreferences {

    public static final int MAX_SAVED_LOCATIONS = 20;
    public static final long DEFAULT_MIN_API_INTERVAL_MS = 45_000L;

    private static final String PREFS_NAME = "weather_forcast_prefs";
    private static final String KEY_LOCATIONS_JSON = "locations_json";
    private static final String KEY_CURRENT_LAT = "current_lat";
    private static final String KEY_CURRENT_LON = "current_lon";
    private static final String KEY_CURRENT_NAME = "current_name";
    private static final String KEY_PREFIX_API_LAST = "api_last_";
    private static final String KEY_PREFIX_CACHE_BODY = "cache_body_";
    private static final String KEY_PREFIX_CACHE_TIME = "cache_time_";

    private static final Gson GSON = new Gson();
    private static final Type LOCATION_LIST_TYPE = new TypeToken<List<SavedLocation>>() {
    }.getType();

    private final SharedPreferences prefs;

    private WeatherPreferences(Context appContext) {
        this.prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static WeatherPreferences get(Context context) {
        return new WeatherPreferences(context.getApplicationContext());
    }

    public boolean hasCurrentLocation() {
        return prefs.contains(KEY_CURRENT_LAT) && prefs.contains(KEY_CURRENT_LON);
    }

    public void setCurrentLocation(double lat, double lon, String displayName) {
        prefs.edit()
                .putFloat(KEY_CURRENT_LAT, (float) lat)
                .putFloat(KEY_CURRENT_LON, (float) lon)
                .putString(KEY_CURRENT_NAME, displayName != null ? displayName : "")
                .apply();
    }

    public double getCurrentLat() {
        return prefs.getFloat(KEY_CURRENT_LAT, 0f);
    }

    public double getCurrentLon() {
        return prefs.getFloat(KEY_CURRENT_LON, 0f);
    }

    public String getCurrentName() {
        return prefs.getString(KEY_CURRENT_NAME, "");
    }

    public void clearCurrentLocation() {
        prefs.edit().remove(KEY_CURRENT_LAT).remove(KEY_CURRENT_LON).remove(KEY_CURRENT_NAME).apply();
    }

    public List<SavedLocation> getSavedLocations() {
        String json = prefs.getString(KEY_LOCATIONS_JSON, "[]");
        List<SavedLocation> list = GSON.fromJson(json, LOCATION_LIST_TYPE);
        return list != null ? new ArrayList<>(list) : new ArrayList<>();
    }

    private void saveLocations(List<SavedLocation> list) {
        prefs.edit().putString(KEY_LOCATIONS_JSON, GSON.toJson(list)).apply();
    }

    public boolean addOrUpdateLocation(SavedLocation location) {
        List<SavedLocation> list = getSavedLocations();
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getId().equals(location.getId())) {
                list.set(i, location);
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

    public String cacheKeyForCoords(double lat, double lon) {
        return SavedLocation.buildId(lat, lon);
    }

    public void putApiResponseCache(String key, String jsonBody) {
        prefs.edit()
                .putString(KEY_PREFIX_CACHE_BODY + key, jsonBody)
                .putLong(KEY_PREFIX_CACHE_TIME + key, System.currentTimeMillis())
                .apply();
    }

    public String getCachedApiBody(String key) {
        return prefs.getString(KEY_PREFIX_CACHE_BODY + key, null);
    }

    public long getCachedApiTime(String key) {
        return prefs.getLong(KEY_PREFIX_CACHE_TIME + key, 0L);
    }

    public boolean canCallApi(String key, long minIntervalMs) {
        long last = prefs.getLong(KEY_PREFIX_API_LAST + key, 0L);
        return System.currentTimeMillis() - last >= minIntervalMs;
    }

    public void markApiCalled(String key) {
        prefs.edit().putLong(KEY_PREFIX_API_LAST + key, System.currentTimeMillis()).apply();
    }


    /**
     * Helper cho PreviewActivity kiểm tra xem vị trí đã lưu chưa
     */
    public boolean hasSavedLocationWithCoords(double lat, double lon) {
        List<SavedLocation> currentList = getSavedLocations();
        String targetId = SavedLocation.buildId(lat, lon); // Giả định hàm buildId trả về chuỗi nối lat_lon
        for (SavedLocation loc : currentList) {
            if (loc.getId() != null && loc.getId().equals(targetId)) {
                return true;
            }
        }
        return false;
    }

    public boolean addLocation(SavedLocation location) {
        List<SavedLocation> currentList = getSavedLocations();

        // Kiểm tra trùng lặp
        if (hasSavedLocationWithCoords(location.getLatitude(), location.getLongitude())) {
            return false;
        }

        // Kiểm tra giới hạn 20
        if (currentList.size() >= MAX_SAVED_LOCATIONS) {
            return false;
        }

        currentList.add(location);
        saveLocations(currentList);
        return true;
    }
}