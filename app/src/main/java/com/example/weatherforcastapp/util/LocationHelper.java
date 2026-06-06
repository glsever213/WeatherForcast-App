package com.example.weatherforcastapp.util;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;

//claude
import android.os.Handler;
import android.os.Looper;

public final class LocationHelper {
    public static final long GPS_TIMEOUT_MS = 10_000L;

    public interface Callback {
        void onLocation(double lat, double lon);

        void onError();
    }

    private LocationHelper() {
    }

    public static boolean hasPermission(@NonNull Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    public static void fetchCurrent(@NonNull Context context, @NonNull Callback callback) {
//        original
//        if (!hasPermission(context)) {
//            callback.onError();
//            return;
//        }
//        FusedLocationProviderClient client = LocationServices.getFusedLocationProviderClient(context);
//        CancellationTokenSource cts = new CancellationTokenSource();
//        client.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, cts.getToken())
////        chatgpt
////        client.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.getToken())
//                .addOnSuccessListener(location -> {
//                    if (location != null) {
//                        callback.onLocation(location.getLatitude(), location.getLongitude());
//                        return;
//                    }
//                    client.getLastLocation().addOnSuccessListener(last -> {
//                        if (last != null) {
//                            callback.onLocation(last.getLatitude(), last.getLongitude());
//                        } else {
//                            callback.onError();
//                        }
//                    }).addOnFailureListener(e -> callback.onError());
//                })
//                .addOnFailureListener(e -> client.getLastLocation()
//                        .addOnSuccessListener(last -> {
//                            if (last != null) {
//                                callback.onLocation(last.getLatitude(), last.getLongitude());
//                            } else {
//                                callback.onError();
//                            }
//                        })
//                        .addOnFailureListener(e2 -> callback.onError()));
        fetchCurrent(context, GPS_TIMEOUT_MS, callback);
    }

    public static void fetchCurrent(@NonNull Context context, long timeoutMs, @NonNull Callback callback) {
        if (!hasPermission(context)) {
            callback.onError();
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
                callback.onError();
            }
        };
        mainHandler.postDelayed(timeoutRunnable, timeoutMs);

        client.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, cts.getToken())
                .addOnSuccessListener(location -> {
                    if (done[0]) return; // timeout đã xử lý trước
                    if (location != null) {
                        done[0] = true;
                        mainHandler.removeCallbacks(timeoutRunnable);
                        callback.onLocation(location.getLatitude(), location.getLongitude());
                        return;
                    }
                    // getCurrentLocation trả null → thử getLastLocation
                    client.getLastLocation().addOnSuccessListener(last -> {
                        if (done[0]) return;
                        done[0] = true;
                        mainHandler.removeCallbacks(timeoutRunnable);
                        if (last != null) {
                            callback.onLocation(last.getLatitude(), last.getLongitude());
                        } else {
                            callback.onError();
                        }
                    }).addOnFailureListener(e -> {
                        if (done[0]) return;
                        done[0] = true;
                        mainHandler.removeCallbacks(timeoutRunnable);
                        callback.onError();
                    });
                })
                .addOnFailureListener(e -> {
                    if (done[0]) return;
                    // getCurrentLocation thất bại → thử getLastLocation
                    client.getLastLocation()
                            .addOnSuccessListener(last -> {
                                if (done[0]) return;
                                done[0] = true;
                                mainHandler.removeCallbacks(timeoutRunnable);
                                if (last != null) {
                                    callback.onLocation(last.getLatitude(), last.getLongitude());
                                } else {
                                    callback.onError();
                                }
                            })
                            .addOnFailureListener(e2 -> {
                                if (done[0]) return;
                                done[0] = true;
                                mainHandler.removeCallbacks(timeoutRunnable);
                                callback.onError();
                            });
                });
    }
}
