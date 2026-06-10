package com.example.weatherforcastapp;

import android.os.Bundle;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.weatherforcastapp.data.PopularCity;
import com.example.weatherforcastapp.databinding.ActivitySearchBinding;
import com.example.weatherforcastapp.location.LocationContract;
import com.example.weatherforcastapp.search.FakeGeocoding;
import com.example.weatherforcastapp.util.ActivityTransitions;
import com.example.weatherforcastapp.util.LocationHelper;

public class SearchActivity extends AppCompatActivity {

    public static final String EXTRA_MODE = "extra_mode";
    public static final int MODE_ONBOARDING = 1;
    public static final int MODE_MANAGEMENT = 2;

    private ActivitySearchBinding binding;
    private int mode = MODE_ONBOARDING;

    public static void startOnboarding(AppCompatActivity from) {
        open(from, MODE_ONBOARDING);
    }

    public static void startForManagement(AppCompatActivity from) {
        open(from, MODE_MANAGEMENT);
    }

    private static void open(AppCompatActivity from, int mode) {
        android.content.Intent intent = new android.content.Intent(from, SearchActivity.class);
        intent.putExtra(EXTRA_MODE, mode);
        from.startActivity(intent);
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
                searchFromInput();
                return true;
            }
            return false;
        });

        binding.chipLocate.setOnClickListener(v -> locateCurrentPosition());
    }

    private void searchFromInput() {
        String query = getQuery();
        if (query.isEmpty()) {
            Toast.makeText(this, R.string.search_empty, Toast.LENGTH_SHORT).show();
            return;
        }
        searchAndOpen(query);
    }

    private String getQuery() {
        CharSequence text = binding.editSearch.getText();
        return text == null ? "" : text.toString().trim();
    }

    private void searchAndOpen(String query) {
        binding.editSearch.clearFocus();
        FakeGeocoding.search(this, query, new FakeGeocoding.Callback() {
            @Override
            public void onSuccess(@Nullable FakeGeocoding.Result result) {
                if (result == null) {
                    Toast.makeText(SearchActivity.this, R.string.search_empty, Toast.LENGTH_SHORT).show();
                    return;
                }
                openPreview(new PopularCity(result.displayName, result.lat, result.lon, false));
            }

            @Override
            public void onError(String message) {
                showSearchError();
            }
        });
    }

    private void locateCurrentPosition() {
        if (!LocationHelper.hasPermission(this)) {
            Toast.makeText(this, R.string.location_permission_message, Toast.LENGTH_SHORT).show();
            return;
        }

        LocationHelper.fetchCurrent(this, new LocationHelper.Callback() {
            @Override
            public void onLocation(double lat, double lon) {
                openPreview(new PopularCity(getString(R.string.locate_chip), lat, lon, true));
            }

            @Override
            public void onError() {
                Toast.makeText(SearchActivity.this, R.string.search_hint, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showSearchError() {
        Toast.makeText(
                this,
                "Không tải được danh sách địa điểm. Kiểm tra mạng hoặc dữ liệu vị trí.",
                Toast.LENGTH_SHORT
        ).show();
    }

    private void openPreview(PopularCity city) {
        int flow = mode == MODE_ONBOARDING
                ? LocationContract.FLOW_ONBOARDING
                : LocationContract.FLOW_MANAGEMENT;
        PreviewActivity.start(this, city.name, city.lat, city.lon, flow);
        ActivityTransitions.slideIn(this);
        finish();
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
    public void onBackPressed() {
        super.onBackPressed();
    }
}
