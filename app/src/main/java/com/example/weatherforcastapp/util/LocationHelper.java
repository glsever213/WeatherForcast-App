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

public final class LocationHelper {

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
        if (!hasPermission(context)) {
            callback.onError();
            return;
        }
        FusedLocationProviderClient client = LocationServices.getFusedLocationProviderClient(context);
        CancellationTokenSource cts = new CancellationTokenSource();
        client.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, cts.getToken())
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        callback.onLocation(location.getLatitude(), location.getLongitude());
                        return;
                    }
                    client.getLastLocation().addOnSuccessListener(last -> {
                        if (last != null) {
                            callback.onLocation(last.getLatitude(), last.getLongitude());
                        } else {
                            callback.onError();
                        }
                    }).addOnFailureListener(e -> callback.onError());
                })
                .addOnFailureListener(e -> client.getLastLocation()
                        .addOnSuccessListener(last -> {
                            if (last != null) {
                                callback.onLocation(last.getLatitude(), last.getLongitude());
                            } else {
                                callback.onError();
                            }
                        })
                        .addOnFailureListener(e2 -> callback.onError()));
    }
}
