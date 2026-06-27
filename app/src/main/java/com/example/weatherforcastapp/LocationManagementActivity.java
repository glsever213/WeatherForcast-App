package com.example.weatherforcastapp;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.weatherforcastapp.databinding.ActivityLocationManagementBinding;
import com.example.weatherforcastapp.model.SavedLocation;
import com.example.weatherforcastapp.prefs.WeatherPreferences;
import com.example.weatherforcastapp.ui.SavedLocationsAdapter;
import com.example.weatherforcastapp.util.ActivityTransitions;
import com.example.weatherforcastapp.data.WeatherRepository;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.List;
import java.util.Set;

/**
 * Quản lý thành phố: search, chọn vị trí, long-press xóa, tap mở Home.
 */
public class LocationManagementActivity extends AppCompatActivity implements SavedLocationsAdapter.Listener {

    private ActivityLocationManagementBinding binding;
    private WeatherPreferences prefs;
    private SavedLocationsAdapter adapter;
    private final WeatherRepository weatherRepo = new WeatherRepository();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLocationManagementBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        prefs = WeatherPreferences.get(this);
        adapter = new SavedLocationsAdapter(this);

        binding.recyclerLocations.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerLocations.setAdapter(adapter);

        binding.buttonBack.setOnClickListener(v -> {
            finish();
            ActivityTransitions.slideOut(this);
        });

        binding.cardSearchEntry.setOnClickListener(v -> {
            SearchActivity.startForManagement(this);
            ActivityTransitions.slideIn(this);
        });

        binding.buttonDeleteSelected.setOnClickListener(v -> {
            Set<String> ids = adapter.getSelectedIds();
            if (ids.isEmpty()) {
                return;
            }
            new MaterialAlertDialogBuilder(this)
                    .setTitle(R.string.delete_selected_confirm_title)
                    .setMessage(getString(R.string.delete_selected_confirm_message, ids.size()))
                    .setNegativeButton(R.string.cancel, null)
                    .setPositiveButton(R.string.delete_confirm_action, (d, w) -> deleteSelected(ids))
                    .show();
        });

        binding.buttonDeleteAll.setOnClickListener(v -> {
            if (prefs.getSavedLocations().isEmpty()) {
                return;
            }
            new MaterialAlertDialogBuilder(this)
                    .setTitle(R.string.delete_all_confirm_title)
                    .setMessage(R.string.delete_all_confirm_message)
                    .setNegativeButton(R.string.cancel, null)
                    .setPositiveButton(R.string.delete_confirm_action, (d, w) -> deleteAll())
                    .show();
        });

        binding.buttonDoneSelection.setOnClickListener(v -> exitSelectionMode());

        refreshList();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshList();
    }

    private void deleteSelected(Set<String> ids) {
        for (String id : ids) {
            prefs.removeLocation(id);
        }
        exitSelectionMode();
        refreshList();
    }

    private void deleteAll() {
        prefs.removeAllLocations();
        exitSelectionMode();
        refreshList();
    }

    private void exitSelectionMode() {
        adapter.setSelectionMode(false);
        binding.barSelectionActions.setVisibility(android.view.View.GONE);
    }

    private void refreshList() {
        prefs.syncCurrentLocationToSavedList();
        if (prefs.isCurrentFromGps() && prefs.hasCurrentLocation() && !prefs.hasGpsMarker()) {
            prefs.markGpsLocation(prefs.getCurrentLat(), prefs.getCurrentLon());
        }
        List<SavedLocation> locations = prefs.getSavedLocations();
        adapter.setItems(
                locations,
                prefs.hasGpsMarker(),
                prefs.getGpsMarkerLat(),
                prefs.getGpsMarkerLon()
        );
        for (SavedLocation loc : locations) {
            weatherRepo.updateWeatherForLocation(loc, prefs, updated -> {
                runOnUiThread(() -> adapter.notifyDataSetChanged());
            });
        }
    }

    @Override
    public void onOpen(SavedLocation location) {
        if (adapter.isSelectionMode()) return;
        prefs.setCurrentLocation(
                location.getLatitude(),
                location.getLongitude(),
                location.getDisplayName(),
                location.isFromGps()
        );
        HomeActivity.startClearTop(this, location.getLatitude(), location.getLongitude(), location.getDisplayName());
        finish();
    }

    @Override
    public void onBeginSelection(SavedLocation location) {
        binding.barSelectionActions.setVisibility(android.view.View.VISIBLE);
        adapter.enterSelectionWith(location);
    }
}
