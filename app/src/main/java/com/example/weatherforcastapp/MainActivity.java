package com.example.weatherforcastapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.weatherforcastapp.databinding.ActivityMainBinding;
import com.example.weatherforcastapp.prefs.WeatherPreferences;
import com.example.weatherforcastapp.HomeActivity;
import com.example.weatherforcastapp.SearchActivity;
import com.example.weatherforcastapp.util.ActivityTransitions;
import com.example.weatherforcastapp.util.LocationHelper;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

//claude
import android.location.Address;
import android.location.Geocoder;

import com.example.weatherforcastapp.databinding.ActivityMainBinding;
import com.example.weatherforcastapp.prefs.WeatherPreferences;
import com.example.weatherforcastapp.util.ActivityTransitions;
import com.example.weatherforcastapp.util.LocationHelper;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Màn khởi động: đã có vị trí lưu → Home; chưa có → hỏi quyền vị trí (đồng ý / từ chối).
 */
public class MainActivity extends AppCompatActivity {

    private static final int REQ_LOCATION = 1001;

    private ActivityMainBinding binding;

//    chatgpt
//    private boolean navigationHandled = false;
//    claude
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        WeatherPreferences prefs = WeatherPreferences.get(this);
        if (prefs.hasCurrentLocation()) {
//            orginal
            HomeActivity.startClearTask(this, prefs.getCurrentLat(), prefs.getCurrentLon(), prefs.getCurrentName());
            finish();
            return;
//            chatgpt
//            navigateHome(
//                    prefs.getCurrentLat(),
//                    prefs.getCurrentLon(),
//                    prefs.getCurrentName()
//            );
//            return;
        }

        showLocationOfferDialog();
    }

//    claude
    @Override
    protected void onDestroy() {
        executor.shutdownNow();
        super.onDestroy();
    }

    private void showLocationOfferDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.location_permission_title)
                .setMessage(R.string.location_permission_message)
                .setCancelable(false)
                .setPositiveButton(R.string.agree, (d, w) -> requestLocationPermission())
                .setNegativeButton(R.string.decline, (d, w) -> goSearchOnboarding())
                .show();
    }

    private void requestLocationPermission() {
        if (LocationHelper.hasPermission(this)) {
            fetchLocationAndGoHome();
            return;
        }
        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                REQ_LOCATION
        );
    }

    private void goSearchOnboarding() {
        SearchActivity.startOnboarding(this);
        ActivityTransitions.slideIn(this);
        finish();
    }

//    chatgpt
//    private void navigateHome(double lat, double lon, String name) {
//        if (navigationHandled) {
//            return;
//        }
//
//        navigationHandled = true;
//
//        WeatherPreferences prefs = WeatherPreferences.get(this);
//        prefs.setCurrentLocation(lat, lon, name);
//
//        HomeActivity.startClearTask(this, lat, lon, name);
//        finish();
//    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode != REQ_LOCATION) return;
        boolean granted = false;
        if (grantResults.length > 0) {
            for (int r : grantResults) {
                if (r == PackageManager.PERMISSION_GRANTED) {
                    granted = true;
                    break;
                }
            }
        }
        if (granted) {
            fetchLocationAndGoHome();
        } else {
            goSearchOnboarding();
        }
    }

//    claude
    private void fetchLocationAndGoHome() {
        binding.progressMain.setVisibility(android.view.View.VISIBLE);
        LocationHelper.fetchCurrent(this, new LocationHelper.Callback() {
            @Override
            public void onLocation(double lat, double lon) {
                fetchCityNameThenGoHome(lat, lon);
            }

            @Override
            public void onError() {
                binding.progressMain.setVisibility(android.view.View.GONE);
                Toast.makeText(MainActivity.this, R.string.search_hint, Toast.LENGTH_SHORT).show();
                goSearchOnboarding();
            }
        });
    }

    private void fetchCityNameThenGoHome(double lat, double lon) {
        executor.execute(() -> {
            String cityName = resolveCityName(lat, lon);
            // Chuyển về main thread để thao tác UI và start Activity
            runOnUiThread(() -> {
                if (isFinishing()) return;
                binding.progressMain.setVisibility(android.view.View.GONE);
                saveAndGoHome(lat, lon, cityName);
            });
        });
    }

    private String resolveCityName(double lat, double lon) {
        if (!Geocoder.isPresent()) {
            return getString(R.string.placeholder_location);
        }
        try {
            // Locale("vi", "VN") → Geocoder trả tên tiếng Việt có dấu
            Geocoder geocoder = new Geocoder(this, new Locale("vi", "VN"));
            List<Address> addresses = geocoder.getFromLocation(lat, lon, 1);
            if (addresses == null || addresses.isEmpty()) {
                return getString(R.string.placeholder_location);
            }
            Address address = addresses.get(0);
            if (address.getLocality() != null && !address.getLocality().isEmpty()) {
                return address.getLocality();
            }
            if (address.getSubAdminArea() != null && !address.getSubAdminArea().isEmpty()) {
                return address.getSubAdminArea();
            }
            if (address.getAdminArea() != null && !address.getAdminArea().isEmpty()) {
                return address.getAdminArea();
            }
        } catch (IOException e) {
            // Geocoder thất bại (mạng, service không khả dụng) → dùng placeholder
        }
        return getString(R.string.placeholder_location);
    }

    private void saveAndGoHome(double lat, double lon, String cityName) {
        WeatherPreferences prefs = WeatherPreferences.get(this);
        prefs.setCurrentLocation(lat, lon, cityName);
        HomeActivity.startClearTask(this, lat, lon, cityName);
        finish();
    }
//  chatgpt
//    private void fetchLocationAndGoHome() {
//        binding.progressMain.setVisibility(android.view.View.VISIBLE);
//        LocationHelper.fetchCurrent(this, new LocationHelper.Callback() {
//            @Override
//            public void onLocation(double lat, double lon) {
//                binding.progressMain.setVisibility(android.view.View.GONE);
//                String locationName = lat + ", " + lon;
//                navigateHome(lat, lon, locationName);
//            }
//
//            @Override
//            public void onError() {
//                binding.progressMain.setVisibility(android.view.View.GONE);
//                Toast.makeText(
//                        MainActivity.this,
//                        R.string.location_permission_message,
//                        Toast.LENGTH_SHORT
//                ).show();
//                goSearchOnboarding();
//            }
//        });
//    }
}
