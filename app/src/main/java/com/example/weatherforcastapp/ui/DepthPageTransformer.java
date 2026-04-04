package com.example.weatherforcastapp.ui;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.viewpager2.widget.ViewPager2;

/**
 * Transformer mượt cho ViewPager2 (tab Preview).
 */
public final class DepthPageTransformer implements ViewPager2.PageTransformer {

    private static final float MIN_SCALE = 0.92f;

    @Override
    public void transformPage(@NonNull View page, float position) {
        if (position < -1f || position > 1f) {
            page.setAlpha(0f);
            return;
        }
        page.setAlpha(1f - Math.abs(position) * 0.15f);
        float scale = Math.max(MIN_SCALE, 1f - Math.abs(position) * 0.08f);
        page.setScaleX(scale);
        page.setScaleY(scale);
        page.setTranslationX(-position * page.getWidth() * 0.12f);
    }
}
