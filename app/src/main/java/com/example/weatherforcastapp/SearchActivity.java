package com.example.weatherforcastapp;

import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.weatherforcastapp.data.PopularCities;
import com.example.weatherforcastapp.data.PopularCity;
import com.example.weatherforcastapp.databinding.ActivitySearchBinding;
import com.example.weatherforcastapp.location.LocationContract;
import com.example.weatherforcastapp.ui.PopularCityAdapter;
import com.example.weatherforcastapp.util.ActivityTransitions;
import com.example.weatherforcastapp.util.LocationHelper;
import com.example.weatherforcastapp.search.GeocodingRepository;
import com.example.weatherforcastapp.model.api.LocationDto;
import com.example.weatherforcastapp.util.LocationNameResolver;

import java.util.List;

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

        binding.buttonCancel.setOnClickListener(v -> onCancel());

        binding.editSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE) {
                runSearchFromInput();
                return true;
            }
            return false;
        });

        setupPopularCities();

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
                        String cityName = LocationNameResolver.resolve(SearchActivity.this, lat, lon, getString(R.string.locate_chip));

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

    private void setupPopularCities() {
        //Thêm danh sách các thành phố gợi ý
        binding.recyclerCityList.setLayoutManager(
                new GridLayoutManager(this, 3)
        );

        PopularCityAdapter adapter =
                new PopularCityAdapter(
                        PopularCities.all(),
                        city -> openPreview(city)
                );

        binding.recyclerCityList.setAdapter(adapter);
    }

    private void runSearchFromInput() {
        CharSequence cs = binding.editSearch.getText();
        String q = cs != null ? cs.toString().trim() : "";
        if (q.isEmpty()) {
            Toast.makeText(this, R.string.search_empty, Toast.LENGTH_SHORT).show();
            return;
        }

        //Chỉnh lại search API + Geocoder
        binding.progressSearch.setVisibility(View.VISIBLE);

        geocodingRepository.search(q, new GeocodingRepository.Listener() {
                    @Override
                    public void onSuccess(@NonNull List<LocationDto> results) {
                        runOnUiThread(() -> {
                            binding.progressSearch.setVisibility(View.GONE);

                            if (results.isEmpty()) {
                                Toast.makeText(SearchActivity.this, R.string.search_empty, Toast.LENGTH_SHORT).show();
                                return;
                            }

                            LocationDto first = results.get(0);

                            String cityName = LocationNameResolver.resolve(SearchActivity.this, first.getLat(), first.getLon(), first.getName());

                            openPreview(new PopularCity(cityName, first.getLat(), first.getLon(), false));
                        });
                    }

                    @Override
                    public void onFailure(@Nullable String message) {
                        runOnUiThread(() -> {
                            binding.progressSearch.setVisibility(View.GONE);
                            Toast.makeText(SearchActivity.this, R.string.search_empty, Toast.LENGTH_SHORT).show();
                        });
                    }
                });
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
