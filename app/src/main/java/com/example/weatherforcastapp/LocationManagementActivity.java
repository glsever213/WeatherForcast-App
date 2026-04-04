package com.example.weatherforcastapp;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.weatherforcastapp.databinding.ActivityLocationManagementBinding;
import com.example.weatherforcastapp.model.SavedLocation;
import com.example.weatherforcastapp.prefs.WeatherPreferences;
import com.example.weatherforcastapp.ui.SavedLocationsAdapter;
import com.example.weatherforcastapp.util.ActivityTransitions;

import java.util.Set;

/**
 * Quản lý thành phố: search, chọn vị trí, long-press xóa, tap mở Home.
 */
public class LocationManagementActivity extends AppCompatActivity implements SavedLocationsAdapter.Listener {

    private ActivityLocationManagementBinding binding;
    private WeatherPreferences prefs;
    private SavedLocationsAdapter adapter;

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
            for (String id : ids) prefs.removeLocation(id);
            adapter.setSelectionMode(false);
            binding.barSelectionActions.setVisibility(android.view.View.GONE);
            refreshList();
        });

        binding.buttonDeleteAll.setOnClickListener(v -> {
            prefs.removeAllLocations();
            adapter.setSelectionMode(false);
            binding.barSelectionActions.setVisibility(android.view.View.GONE);
            refreshList();
        });

        binding.buttonDoneSelection.setOnClickListener(v -> {
            adapter.setSelectionMode(false);
            binding.barSelectionActions.setVisibility(android.view.View.GONE);
        });

        refreshList();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshList();
    }

    private void refreshList() {
        adapter.setItems(prefs.getSavedLocations());
    }

    @Override
    public void onOpen(SavedLocation location) {
        if (adapter.isSelectionMode()) return;
        prefs.setCurrentLocation(location.getLatitude(), location.getLongitude(), location.getDisplayName());
        HomeActivity.startClearTop(this, location.getLatitude(), location.getLongitude(), location.getDisplayName());
        finish();
    }

    @Override
    public void onBeginSelection(SavedLocation location) {
        binding.barSelectionActions.setVisibility(android.view.View.VISIBLE);
        adapter.enterSelectionWith(location);
    }
}
