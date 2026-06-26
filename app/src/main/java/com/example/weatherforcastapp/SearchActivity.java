package com.example.weatherforcastapp;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.weatherforcastapp.data.PopularCities;
import com.example.weatherforcastapp.data.PopularCity;
import com.example.weatherforcastapp.databinding.ActivitySearchBinding;
import com.example.weatherforcastapp.location.LocationContract;
import com.example.weatherforcastapp.model.api.LocationDto;
import com.example.weatherforcastapp.search.GeocodingRepository;
import com.example.weatherforcastapp.search.LocationLabelFormatter;
import com.example.weatherforcastapp.search.SearchResultAdapter;
import com.example.weatherforcastapp.ui.PopularCityAdapter;
import com.example.weatherforcastapp.util.ActivityTransitions;
import com.example.weatherforcastapp.util.LocationHelper;
import com.example.weatherforcastapp.util.LocationNameResolver;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Tìm kiếm tối giản: thanh nhập + vài nút giả; Enter hoặc chọn nút → Preview (luôn truyền lat/lon).
 */
public class SearchActivity extends AppCompatActivity {

    public static final String EXTRA_MODE = "extra_mode";
    public static final int MODE_ONBOARDING = 1;
    public static final int MODE_MANAGEMENT = 2;

    private ActivitySearchBinding binding;
    private int mode = MODE_ONBOARDING;

    private final GeocodingRepository geocodingRepository = new GeocodingRepository();
    private PopularCityAdapter popularCityAdapter;
    private SearchResultAdapter searchResultAdapter;
    private boolean showingSearchResults = false;

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

        setupPopularCities();
        setupSearchInput();

        binding.buttonCancel.setOnClickListener(v -> onCancel());

        binding.chipLocate.setOnClickListener(v -> {
            if (!LocationHelper.hasPermission(this)) {
                Toast.makeText(this, R.string.location_permission_message, Toast.LENGTH_SHORT).show();
                return;
            }
            LocationHelper.fetchCurrent(this, new LocationHelper.Callback() {
                @Override
                public void onLocation(double lat, double lon) {
                    //Bổ sung: Lấy tên của vị trị hiện tại
                    new Thread(() -> {
                        String cityName = LocationNameResolver.resolve(
                                SearchActivity.this,
                                lat,
                                lon,
                                getString(R.string.locate_chip)
                        );

                        runOnUiThread(() -> openPreview(new PopularCity(cityName, lat, lon, true)));
                    }).start();
                }

                @Override
                public void onError() {
                    Toast.makeText(SearchActivity.this, R.string.search_hint, Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void setupSearchInput() {
        binding.editSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE) {
                runSearchFromInput();
                return true;
            }
            return false;
        });

        binding.editSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String current = s == null ? "" : s.toString().trim();
                if (current.isEmpty() && showingSearchResults) {
                    showPopularCities();
                    binding.textSearchState.setVisibility(android.view.View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void setupPopularCities() {
        binding.recyclerCityList.setLayoutManager(new GridLayoutManager(this, 3));

        popularCityAdapter = new PopularCityAdapter(
                PopularCities.all(),
                this::openPreview
        );

        searchResultAdapter = new SearchResultAdapter(location ->
                openPreview(toPopularCity(location))
        );

        binding.recyclerCityList.setAdapter(popularCityAdapter);
        binding.recyclerSearchResults.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerSearchResults.setAdapter(searchResultAdapter);

        showingSearchResults = false;
    }

    private void showPopularCities() {
        if (!showingSearchResults) {
            return;
        }
        binding.recyclerCityList.setVisibility(android.view.View.VISIBLE);
        binding.recyclerCityList.setLayoutManager(new GridLayoutManager(this, 3));
        binding.recyclerCityList.setAdapter(popularCityAdapter);
        binding.chipLocate.setVisibility(android.view.View.VISIBLE);
        binding.recyclerSearchResults.setVisibility(android.view.View.GONE);
        showingSearchResults = false;
    }

    private void showSearchResults(@NonNull List<LocationDto> results, @NonNull String query) {
        binding.recyclerCityList.setVisibility(android.view.View.GONE);
        binding.chipLocate.setVisibility(android.view.View.GONE);
        binding.recyclerSearchResults.setVisibility(android.view.View.VISIBLE);
        binding.recyclerSearchResults.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerSearchResults.setAdapter(searchResultAdapter);
        searchResultAdapter.submitList(results);
        showingSearchResults = true;

        binding.textSearchState.setText(
                "Tìm thấy " + results.size() + " địa điểm phù hợp với \"" + query + "\""
        );
        binding.textSearchState.setVisibility(android.view.View.VISIBLE);
    }

    private void showNoResults(@NonNull String query) {
        searchResultAdapter.clear();
        binding.recyclerCityList.setVisibility(android.view.View.GONE);
        binding.chipLocate.setVisibility(android.view.View.GONE);
        binding.recyclerSearchResults.setVisibility(android.view.View.VISIBLE);
        binding.recyclerSearchResults.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerSearchResults.setAdapter(searchResultAdapter);
        showingSearchResults = true;

        binding.textSearchState.setText(
                "Không tìm thấy địa điểm phù hợp với \"" + query + "\""
        );
        binding.textSearchState.setVisibility(android.view.View.VISIBLE);
    }

    private void runSearchFromInput() {
        CharSequence cs = binding.editSearch.getText();
        String q = cs != null ? cs.toString().trim() : "";
        if (q.isEmpty()) {
            Toast.makeText(this, R.string.search_empty, Toast.LENGTH_SHORT).show();
            showPopularCities();
            return;
        }

        binding.progressSearch.setVisibility(android.view.View.VISIBLE);
        binding.textSearchState.setVisibility(android.view.View.GONE);

        geocodingRepository.search(q, new GeocodingRepository.Listener() {
            @Override
            public void onSuccess(@NonNull List<LocationDto> results) {
                runOnUiThread(() -> {
                    binding.progressSearch.setVisibility(android.view.View.GONE);

                    List<LocationDto> filtered = new ArrayList<>();
                    String normalizedQuery = normalize(q);

                    for (LocationDto item : results) {
                        if (matchesQuery(item, normalizedQuery)) {
                            filtered.add(item);
                        }
                    }

                    if (filtered.isEmpty()) {
                        showNoResults(q);
                    } else {
                        showSearchResults(filtered, q);
                    }
                });
            }

            @Override
            public void onFailure(@Nullable String message) {
                runOnUiThread(() -> {
                    binding.progressSearch.setVisibility(android.view.View.GONE);
                    showNoResults(q);
                    Toast.makeText(SearchActivity.this, R.string.search_empty, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private boolean matchesQuery(@NonNull LocationDto item, @NonNull String normalizedQuery) {
        if (normalizedQuery.isEmpty()) {
            return true;
        }

        return normalize(LocationLabelFormatter.displayName(item)).contains(normalizedQuery)
                || normalize(item.getName()).contains(normalizedQuery)
                || normalize(item.getRegion()).contains(normalizedQuery)
                || normalize(item.getCountry()).contains(normalizedQuery);
    }

    @NonNull
    private PopularCity toPopularCity(@NonNull LocationDto location) {
        String label = LocationLabelFormatter.displayName(location);
        if (label == null || label.trim().isEmpty() || "—".equals(label)) {
            label = safe(location.getName());
        }
        return new PopularCity(label, location.getLat(), location.getLon(), false);
    }

    @NonNull
    private String safe(@Nullable String value) {
        return value == null ? "" : value.trim();
    }

    @NonNull
    private String normalize(@Nullable String text) {
        if (text == null) {
            return "";
        }
        String normalized = Normalizer.normalize(text, Normalizer.Form.NFD);
        normalized = normalized.replaceAll("\\p{M}+", "");
        return normalized.toLowerCase(Locale.ROOT).trim();
    }

    private void openPreview(PopularCity city) {
        int flow = mode == MODE_ONBOARDING
                ? LocationContract.FLOW_ONBOARDING
                : LocationContract.FLOW_MANAGEMENT;
        PreviewBottomSheet.show(getSupportFragmentManager(), city.name, city.lat, city.lon, flow);
    }

    private void onCancel() {
        if (mode == MODE_ONBOARDING) {
            finishAffinity();
        } else {
            finish();
            ActivityTransitions.slideOut(this);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        geocodingRepository.cancel();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        onCancel();
    }
}
