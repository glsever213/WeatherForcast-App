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

/**
 * Tìm kiếm tối giản: thanh nhập + vài nút giả; Enter hoặc chọn nút → Preview (luôn truyền lat/lon).
 */
public class SearchActivity extends AppCompatActivity {

    public static final String EXTRA_MODE = "extra_mode";
    public static final int MODE_ONBOARDING = 1;
    public static final int MODE_MANAGEMENT = 2;

    private ActivitySearchBinding binding;
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

        binding.buttonCancel.setOnClickListener(v -> onCancel());

        binding.editSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE) {
                runSearchFromInput();
                return true;
            }
            return false;
        });

        binding.chipFakeHanoi.setOnClickListener(v ->
                openPreview(new PopularCity("Hà Nội", 21.0285, 105.8542, false)));
        binding.chipFakeHcmc.setOnClickListener(v ->
                openPreview(new PopularCity("TP.HCM", 10.8231, 106.6297, false)));
        binding.chipLocate.setOnClickListener(v -> {
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
        });
    }

    private void runSearchFromInput() {
        CharSequence cs = binding.editSearch.getText();
        String q = cs != null ? cs.toString().trim() : "";
        if (q.isEmpty()) {
            Toast.makeText(this, R.string.search_empty, Toast.LENGTH_SHORT).show();
            return;
        }
        FakeGeocoding.Result r = FakeGeocoding.search(q);
        if (r == null) {
            Toast.makeText(this, R.string.search_empty, Toast.LENGTH_SHORT).show();
            return;
        }
        openPreview(new PopularCity(r.displayName, r.lat, r.lon, false));
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
        onCancel();
    }
}
