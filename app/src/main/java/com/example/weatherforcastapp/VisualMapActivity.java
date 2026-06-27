package com.example.weatherforcastapp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.weatherforcastapp.databinding.ActivityVisualMapBinding;
import com.example.weatherforcastapp.prefs.WeatherPreferences;
import com.example.weatherforcastapp.util.ActivityTransitions;

import java.util.Locale;

/** Bản đồ dự báo — Windy embed, cùng lat/lon prefs như Home. */
public class VisualMapActivity extends AppCompatActivity {

    private ActivityVisualMapBinding binding;

    public static void start(@NonNull Activity from) {
        Intent i = new Intent(from, VisualMapActivity.class);
        from.startActivity(i);
        ActivityTransitions.slideIn(from);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityVisualMapBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        WeatherPreferences prefs = WeatherPreferences.get(this);
        double lat = prefs.getCurrentLat();
        double lon = prefs.getCurrentLon();
        String name = prefs.getCurrentName();

        if (name != null && !name.isEmpty()) {
            binding.textMapTitle.setText(getString(R.string.visual_map_title_with_name, name));
        }

        binding.buttonBackMap.setOnClickListener(v -> {
            finish();
            ActivityTransitions.slideOut(this);
        });

        setupMap(lat, lon);
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setupMap(double lat, double lon) {
        WebView web = binding.webMap;
        WebSettings settings = web.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            settings.setMixedContentMode(WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE);
        }

        web.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                binding.progressMap.setVisibility(View.GONE);
                view.animate().alpha(1f).setDuration(350).start();
                view.evaluateJavascript(
                        "(function(){var s=document.createElement('style');"
                                + "s.textContent='#bottom,#plugin-detail,#detail{display:none!important}';"
                                + "document.head.appendChild(s);})()",
                        null
                );
            }
        });

        binding.webMap.post(() -> loadWindyEmbed(lat, lon));
    }

    private void loadWindyEmbed(double lat, double lon) {
        int w = Math.max(binding.webMap.getWidth(), 320);
        int h = Math.max(binding.webMap.getHeight(), 400);
        String windyUrl = String.format(
                Locale.US,
                "https://embed.windy.com/embed2.html?lat=%.4f&lon=%.4f&zoom=8&level=surface&overlay=temp"
                        + "&product=ecmwf&menu=&message=&marker=true&calendar=now&pressure=&type=map"
                        + "&location=coordinates&metricWind=km%%2Fh&metricTemp=%%C2%%B0C&radarRange=-1"
                        + "&width=%d&height=%d",
                lat, lon, w, h
        );
        binding.webMap.loadUrl(windyUrl);
    }

    @Override
    protected void onDestroy() {
        if (binding != null) {
            binding.webMap.destroy();
        }
        binding = null;
        super.onDestroy();
    }
}
