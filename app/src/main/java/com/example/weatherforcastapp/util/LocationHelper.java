package com.example.weatherforcastapp.util;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.example.weatherforcastapp.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;

import android.os.Handler;
import android.os.Looper;

public final class LocationHelper {
    public static final long GPS_TIMEOUT_MS = 20_000L;

    public interface Callback {
        void onLocation(double lat, double lon);

        /** reason: thông báo người dùng đọc được, đã phân biệt theo nguyên nhân. */
        void onError(@NonNull String reason);
    }

    private LocationHelper() {
    }

    public static boolean hasPermission(@NonNull Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    /** Android 12+ có thể chỉ cấp quyền vị trí gần đúng (Approximate). */
    public static boolean hasFinePermission(@NonNull Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    /** True nếu công tắc Location của hệ thống đang bật (có ít nhất một provider). */
    public static boolean isLocationEnabled(@NonNull Context context) {
        LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (lm == null) {
            return false;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            return lm.isLocationEnabled();
        }
        return lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    /** True nếu thiết bị có Google Play Services (fused provider cần cái này). */
    public static boolean hasGooglePlayServices(@NonNull Context context) {
        return GoogleApiAvailability.getInstance()
                .isGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS;
    }

    public static void fetchCurrent(@NonNull Context context, @NonNull Callback callback) {
        fetchCurrent(context, GPS_TIMEOUT_MS, callback);
    }

    public static void fetchCurrent(@NonNull Context context, long timeoutMs, @NonNull Callback callback) {
        // (Lỗi 4) Thiếu Google Play Services → fused provider không chạy được.
        if (!hasGooglePlayServices(context)) {
            callback.onError(context.getString(R.string.loc_err_no_play_services));
            return;
        }
        if (!hasPermission(context)) {
            callback.onError(context.getString(R.string.loc_err_permission));
            return;
        }
        // (Lỗi 3) Công tắc Location của hệ thống đang tắt → không thể có fix.
        if (!isLocationEnabled(context)) {
            callback.onError(context.getString(R.string.loc_err_location_off));
            return;
        }

        FusedLocationProviderClient client = LocationServices.getFusedLocationProviderClient(context);
        CancellationTokenSource cts = new CancellationTokenSource();
        Handler mainHandler = new Handler(Looper.getMainLooper());

        // done[0] = true sau khi một trong hai (GPS hoặc timeout) đã xử lý xong
        boolean[] done = {false};

        // Runnable timeout: nếu GPS chậm quá → hủy request GPS, gọi onError
        Runnable timeoutRunnable = () -> {
            if (!done[0]) {
                done[0] = true;
                cts.cancel(); // hủy getCurrentLocation đang chờ
                callback.onError(context.getString(R.string.loc_err_timeout));
            }
        };
        mainHandler.postDelayed(timeoutRunnable, timeoutMs);

        try {
            // Chỉ dùng HIGH_ACCURACY khi có quyền chính xác (Precise).
            // Nếu user chọn Approximate thì dùng BALANCED để tránh lỗi / null.
            int priority = hasFinePermission(context)
                    ? Priority.PRIORITY_HIGH_ACCURACY
                    : Priority.PRIORITY_BALANCED_POWER_ACCURACY;
            client.getCurrentLocation(priority, cts.getToken())
                    .addOnSuccessListener(location -> {
                        if (done[0]) return;
                        if (location != null) {
                            done[0] = true;
                            mainHandler.removeCallbacks(timeoutRunnable);
                            callback.onLocation(location.getLatitude(), location.getLongitude());
                            return;
                        }
                        client.getLastLocation().addOnSuccessListener(last -> {
                            if (done[0]) return;
                            done[0] = true;
                            mainHandler.removeCallbacks(timeoutRunnable);
                            if (last != null) {
                                callback.onLocation(last.getLatitude(), last.getLongitude());
                            } else {
                                // (Lỗi 1 & 2) Cả fix mới lẫn cache đều null
                                // (hay gặp trên emulator chưa set vị trí).
                                callback.onError(context.getString(R.string.loc_err_unavailable));
                            }
                        }).addOnFailureListener(e -> {
                            if (done[0]) return;
                            done[0] = true;
                            mainHandler.removeCallbacks(timeoutRunnable);
                            callback.onError(context.getString(R.string.loc_err_unavailable));
                        });
                    })
                    .addOnFailureListener(e -> {
                        if (done[0]) return;
                        client.getLastLocation()
                                .addOnSuccessListener(last -> {
                                    if (done[0]) return;
                                    done[0] = true;
                                    mainHandler.removeCallbacks(timeoutRunnable);
                                    if (last != null) {
                                        callback.onLocation(last.getLatitude(), last.getLongitude());
                                    } else {
                                        callback.onError(context.getString(R.string.loc_err_unavailable));
                                    }
                                })
                                .addOnFailureListener(e2 -> {
                                    if (done[0]) return;
                                    done[0] = true;
                                    mainHandler.removeCallbacks(timeoutRunnable);
                                    callback.onError(context.getString(R.string.loc_err_unavailable));
                                });
                    });
        } catch (SecurityException e) {
            mainHandler.removeCallbacks(timeoutRunnable);
            callback.onError(context.getString(R.string.loc_err_permission));
        }
    }
}
