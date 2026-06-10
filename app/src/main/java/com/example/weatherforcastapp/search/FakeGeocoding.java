package com.example.weatherforcastapp.search;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

    public interface Callback {
        void onSuccess(@Nullable Result result);

        void onError(@NonNull String message);
    }

    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();
    private static final Handler MAIN = new Handler(Looper.getMainLooper());
    private static final int MAX_RESULTS = 5;

    private FakeGeocoding() {
    }

    public static void search(@NonNull Context context, @NonNull String rawQuery, @NonNull Callback callback) {
        String query = rawQuery.trim();
        if (query.isEmpty()) {
            postSuccess(callback, null);
            return;
        }

        Context appContext = context.getApplicationContext();
        EXECUTOR.execute(() -> {
            try {
                postSuccess(callback, findFirstMatch(appContext, query));
            } catch (IOException e) {
                postError(callback, "Không tìm được địa điểm phù hợp");
            } catch (Exception e) {
                postError(callback, "Không tìm được địa điểm phù hợp");
            }
        });
    }

    private static void postSuccess(@NonNull Callback callback, @Nullable Result result) {
        MAIN.post(() -> callback.onSuccess(result));
    }

    private static void postError(@NonNull Callback callback, @NonNull String message) {
        MAIN.post(() -> callback.onError(message));
    }

    @Nullable
    private static Result findFirstMatch(
            @NonNull Context context,
            @NonNull String query) throws IOException {

        android.util.Log.d("GEOCODER", "Search query = " + query);

        List<Address> results = geocode(
                context,
                query,
                new Locale("vi", "VN")
        );

        android.util.Log.d(
                "GEOCODER",
                "VN results = " + (results == null ? "null" : results.size())
        );

        if (results == null || results.isEmpty()) {
            results = geocode(
                    context,
                    query,
                    Locale.getDefault()
            );

            android.util.Log.d(
                    "GEOCODER",
                    "Default results = " + (results == null ? "null" : results.size())
            );
        }

        if (results == null || results.isEmpty()) {
            android.util.Log.d("GEOCODER", "No result found");
            return null;
        }

        Address best = results.get(0);

        android.util.Log.d(
                "GEOCODER",
                "Found: " +
                        best.getLatitude() +
                        ", " +
                        best.getLongitude()
        );

        return new Result(
                buildDisplayName(best, query),
                best.getLatitude(),
                best.getLongitude()
        );
    }

    @Nullable
    private static List<Address> geocode(@NonNull Context context, @NonNull String query, @NonNull Locale locale)
            throws IOException {
        Geocoder geocoder = new Geocoder(context, locale);
        return geocoder.getFromLocationName(query, MAX_RESULTS);
    }

    @NonNull
    private static String buildDisplayName(@NonNull Address address, @NonNull String fallbackQuery) {
        StringBuilder name = new StringBuilder();
        appendPart(name, firstNonEmpty(address.getLocality(), address.getSubAdminArea(), address.getFeatureName()));
        appendPart(name, firstNonEmpty(address.getAdminArea(), address.getSubLocality()));
        appendPart(name, address.getCountryName());

        if (name.length() > 0) {
            return name.toString();
        }

        String line0 = address.getAddressLine(0);
        if (line0 != null && !line0.trim().isEmpty()) {
            return line0.trim();
        }

        return fallbackQuery;
    }

    private static void appendPart(@NonNull StringBuilder target, @Nullable String part) {
        if (part == null) {
            return;
        }
        String cleaned = part.trim();
        if (cleaned.isEmpty()) {
            return;
        }
        if (target.length() > 0) {
            target.append(", ");
        }
        target.append(cleaned);
    }

    @Nullable
    private static String firstNonEmpty(@Nullable String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (value != null) {
                String cleaned = value.trim();
                if (!cleaned.isEmpty()) {
                    return cleaned;
                }
            }
        }
        return null;
    }
}
