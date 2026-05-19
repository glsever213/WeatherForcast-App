package com.example.weatherforcastapp;

import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.weatherforcastapp.databinding.ActivitySearchBinding;
import com.example.weatherforcastapp.location.LocationContract;
import com.example.weatherforcastapp.model.api.LocationDto;
import com.example.weatherforcastapp.search.GeocodingRepository;
import com.example.weatherforcastapp.search.LocationLabelFormatter;
import com.example.weatherforcastapp.search.SearchResultAdapter;
import com.example.weatherforcastapp.util.ActivityTransitions;
import com.example.weatherforcastapp.util.LocationHelper;

import java.util.List;

/**
 * Tìm kiếm địa điểm thật bằng WeatherAPI Search/Autocomplete API.
 * Enter hoặc chọn kết quả → PreviewActivity với lat/lon + tên hiển thị.
 */
public class SearchActivity extends AppCompatActivity {

    public static final String EXTRA_MODE = "extra_mode";
    public static final int MODE_ONBOARDING = 1;
    public static final int MODE_MANAGEMENT = 2;

    private ActivitySearchBinding binding;
    private final GeocodingRepository repository = new GeocodingRepository();
    private SearchResultAdapter adapter;
    private int mode = MODE_ONBOARDING;

    public static void startOnboarding(AppCompatActivity from) {
        android.content.Intent i = new android.content.Intent(from, SearchActivity.class);
        i.putExtra(EXTRA_MODE, MODE_ONBOARDING);
        from.startActivity(i);
    }

    public static void startForManagement(AppCompatActivity from) {
        android.content.Intent i = new android.content.Intent(from, SearchActivity.class);
        i.putExtra(EXTRA_MODE, MODE_MANAGEMENT);
        from.startActivity(i);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySearchBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mode = getIntent().getIntExtra(EXTRA_MODE, MODE_ONBOARDING);

        adapter = new SearchResultAdapter(this::openPreview);
        binding.recyclerSearchResults.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerSearchResults.setAdapter(adapter);
        binding.recyclerSearchResults.setHasFixedSize(true);

        showIdleState();

        binding.buttonCancel.setOnClickListener(v -> onCancel());
        binding.buttonSearch.setOnClickListener(v -> runSearchFromInput());

        binding.editSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE) {
                runSearchFromInput();
                return true;
            }
            return false;
        });

        binding.chipLocate.setOnClickListener(v -> {
            if (!LocationHelper.hasPermission(this)) {
                Toast.makeText(this, R.string.location_permission_message, Toast.LENGTH_SHORT).show();
                return;
            }
            LocationHelper.fetchCurrent(this, new LocationHelper.Callback() {
                @Override
                public void onLocation(double lat, double lon) {
                    openPreview(lat, lon, getString(R.string.locate_chip));
                }

                @Override
                public void onError() {
                    Toast.makeText(SearchActivity.this, R.string.search_error, Toast.LENGTH_SHORT).show();
                }
            });
        });

        binding.editSearch.requestFocus();
    }

    private void runSearchFromInput() {
        CharSequence cs = binding.editSearch.getText();
        String q = cs != null ? cs.toString().trim() : "";
        if (q.isEmpty()) {
            Toast.makeText(this, R.string.search_empty, Toast.LENGTH_SHORT).show();
            showIdleState();
            return;
        }

        hideKeyboard();
        showLoadingState(q);
        repository.search(q, new GeocodingRepository.Listener() {
            @Override
            public void onSuccess(@NonNull List<LocationDto> results) {
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                if (results.isEmpty()) {
                    showEmptyResults(q);
                    return;
                }
                adapter.submitList(results);
                binding.recyclerSearchResults.setVisibility(View.VISIBLE);
                binding.textSearchState.setVisibility(View.GONE);
                binding.progressSearch.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(@Nullable String message) {
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                adapter.clear();
                binding.recyclerSearchResults.setVisibility(View.GONE);
                binding.progressSearch.setVisibility(View.GONE);
                binding.textSearchState.setVisibility(View.VISIBLE);
                binding.textSearchState.setText(
                        message == null || message.trim().isEmpty()
                                ? getString(R.string.search_error)
                                : message
                );
                Toast.makeText(SearchActivity.this, R.string.search_error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showIdleState() {
        adapter.clear();
        binding.progressSearch.setVisibility(View.GONE);
        binding.recyclerSearchResults.setVisibility(View.GONE);
        binding.textSearchState.setVisibility(View.VISIBLE);
        binding.textSearchState.setText(R.string.search_idle);
    }

    private void showLoadingState(String query) {
        adapter.clear();
        binding.recyclerSearchResults.setVisibility(View.GONE);
        binding.textSearchState.setVisibility(View.VISIBLE);
        binding.textSearchState.setText(getString(R.string.search_loading, query));
        binding.progressSearch.setVisibility(View.VISIBLE);
    }

    private void showEmptyResults(String query) {
        adapter.clear();
        binding.recyclerSearchResults.setVisibility(View.GONE);
        binding.progressSearch.setVisibility(View.GONE);
        binding.textSearchState.setVisibility(View.VISIBLE);
        binding.textSearchState.setText(getString(R.string.search_no_results, query));
    }

    private void openPreview(@NonNull LocationDto location) {
        openPreview(location.getLat(), location.getLon(), LocationLabelFormatter.displayName(location));
    }

    private void openPreview(double lat, double lon, @NonNull String displayName) {
        int flow = mode == MODE_ONBOARDING
                ? LocationContract.FLOW_ONBOARDING
                : LocationContract.FLOW_MANAGEMENT;
        PreviewActivity.start(this, displayName, lat, lon, flow);
        ActivityTransitions.slideIn(this);
        finish();
    }

    private void onCancel() {
        repository.cancel();
        if (mode == MODE_ONBOARDING) {
            finishAffinity();
        } else {
            finish();
            ActivityTransitions.slideOut(this);
        }
    }

    @Override
    protected void onDestroy() {
        repository.cancel();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        onCancel();
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(binding.editSearch.getWindowToken(), 0);
        }
    }
}
