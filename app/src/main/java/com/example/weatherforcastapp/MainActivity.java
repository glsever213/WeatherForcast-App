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

/**
 * Màn khởi động: đã có vị trí lưu → Home; chưa có → hỏi quyền vị trí (đồng ý / từ chối).
 */
public class MainActivity extends AppCompatActivity {

    private static final int REQ_LOCATION = 1001;

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        WeatherPreferences prefs = WeatherPreferences.get(this);
        if (prefs.hasCurrentLocation()) {
            HomeActivity.startClearTask(this, prefs.getCurrentLat(), prefs.getCurrentLon(), prefs.getCurrentName());
            finish();
            return;
        }

        showLocationOfferDialog();
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

    private void fetchLocationAndGoHome() {
        binding.progressMain.setVisibility(android.view.View.VISIBLE);
        LocationHelper.fetchCurrent(this, new LocationHelper.Callback() {
            @Override
            public void onLocation(double lat, double lon) {
                binding.progressMain.setVisibility(android.view.View.GONE);
                WeatherPreferences prefs = WeatherPreferences.get(MainActivity.this);
                prefs.setCurrentLocation(lat, lon, getString(R.string.placeholder_location));
                HomeActivity.startClearTask(MainActivity.this, lat, lon, prefs.getCurrentName());
                finish();
            }

            @Override
            public void onError() {
                binding.progressMain.setVisibility(android.view.View.GONE);
                Toast.makeText(MainActivity.this, R.string.search_hint, Toast.LENGTH_SHORT).show();
                goSearchOnboarding();
            }
        });
    }
}
