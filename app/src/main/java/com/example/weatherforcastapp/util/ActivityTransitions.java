package com.example.weatherforcastapp.util;

import android.app.Activity;

import com.example.weatherforcastapp.R;

public final class ActivityTransitions {

    private ActivityTransitions() {
    }

    public static void slideIn(Activity activity) {
        activity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    public static void slideOut(Activity activity) {
        activity.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}
