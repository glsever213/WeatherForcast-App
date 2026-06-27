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
import com.example.weatherforcastapp.model.SavedLocation;
import com.example.weatherforcastapp.util.ActivityTransitions;
import com.example.weatherforcastapp.util.LocationHelper;
import com.example.weatherforcastapp.util.LocationNameResolver;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import android.location.Address;
import android.location.Geocoder;
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
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

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
                fetchCityNameThenGoHome(lat, lon);
            }

            @Override
            public void onError(@NonNull String reason) {
                binding.progressMain.setVisibility(android.view.View.GONE);
                Toast.makeText(MainActivity.this, reason, Toast.LENGTH_LONG).show();
                goSearchOnboarding();
            }
        });
    }

    private void fetchCityNameThenGoHome(double lat, double lon) {
        executor.execute(() -> {
            String cityName = LocationNameResolver.resolve(this, lat, lon, getString(R.string.placeholder_location));
            runOnUiThread(() -> {
                if (isFinishing()) return;
                binding.progressMain.setVisibility(android.view.View.GONE);
                saveAndGoHome(lat, lon, cityName);
            });
        });
    }

//    private String resolveCityName(double lat, double lon) {
//        if (!Geocoder.isPresent()) {
//            return getString(R.string.placeholder_location);
//        }
//        try {
//            Geocoder geocoder = new Geocoder(this, new Locale("vi", "VN"));
//            List<Address> addresses = geocoder.getFromLocation(lat, lon, 1);
//            if (addresses == null || addresses.isEmpty()) {
//                return getString(R.string.placeholder_location);
//            }
//            Address address = addresses.get(0);
//            if (address.getAdminArea() != null && !address.getAdminArea().isEmpty()) {
//                return address.getAdminArea();
//            }
//            if (address.getSubAdminArea() != null && !address.getSubAdminArea().isEmpty()) {
//                return address.getSubAdminArea();
//            }
//            if (address.getLocality() != null && !address.getLocality().isEmpty()) {
//                return address.getLocality();
//            }
//        } catch (IOException e) {}
//        return getString(R.string.placeholder_location);
//    }

    private void saveAndGoHome(double lat, double lon, String cityName) {
        WeatherPreferences prefs = WeatherPreferences.get(this);
        prefs.setCurrentLocation(lat, lon, cityName, true);
        prefs.markGpsLocation(lat, lon);
        HomeActivity.startClearTask(this, lat, lon, cityName);
        finish();
    }
}
