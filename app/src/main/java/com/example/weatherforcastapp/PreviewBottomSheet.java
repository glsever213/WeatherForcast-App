package com.example.weatherforcastapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import com.bumptech.glide.Glide;
import com.example.weatherforcastapp.api.WeatherApiIcons;
import com.example.weatherforcastapp.data.WeatherRepository;
import com.example.weatherforcastapp.databinding.FragmentPreviewBottomSheetBinding;
import com.example.weatherforcastapp.databinding.WidgetTodayHourlyBlockBinding;
import com.example.weatherforcastapp.location.LocationContract;
import com.example.weatherforcastapp.model.SavedLocation;
import com.example.weatherforcastapp.model.api.ApiForecastDayDto;
import com.example.weatherforcastapp.model.api.ConditionDto;
import com.example.weatherforcastapp.model.api.CurrentDto;
import com.example.weatherforcastapp.model.api.ForecastResponse;
import com.example.weatherforcastapp.model.api.HourItemDto;
import com.example.weatherforcastapp.prefs.WeatherPreferences;
import com.example.weatherforcastapp.ui.TodayHourlySectionHelper;
import com.example.weatherforcastapp.util.ActivityTransitions;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PreviewBottomSheet extends BottomSheetDialogFragment {
    private static final String TAG = "PreviewBottomSheet";

    private static final String ARG_NAME     = "name";
    private static final String ARG_LAT      = "lat";
    private static final String ARG_LON      = "lon";
    private static final String ARG_FLOW     = "flow";
    private static final String ARG_FROM_GPS = "from_gps";

    private FragmentPreviewBottomSheetBinding binding;
    private final WeatherRepository weatherRepo = new WeatherRepository();

    public static void show(@NonNull FragmentManager fm, @NonNull String name, double lat, double lon, int flowMode) {
        show(fm, name, lat, lon, flowMode, false);
    }

    public static void show(@NonNull FragmentManager fm, @NonNull String name, double lat, double lon,
                            int flowMode, boolean fromGps) {
        if (fm.findFragmentByTag(TAG) != null) return;

        Bundle args = new Bundle();
        args.putString(ARG_NAME, name);
        args.putDouble(ARG_LAT, lat);
        args.putDouble(ARG_LON, lon);
        args.putInt(ARG_FLOW, flowMode);
        args.putBoolean(ARG_FROM_GPS, fromGps);

        PreviewBottomSheet sheet = new PreviewBottomSheet();
        sheet.setArguments(args);
        sheet.show(fm, TAG);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentPreviewBottomSheetBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle args = requireArguments();
        String cityName = args.getString(ARG_NAME, "—");
        double lat = args.getDouble(ARG_LAT);
        double lon = args.getDouble(ARG_LON);
        int flowMode = args.getInt(ARG_FLOW, LocationContract.FLOW_MANAGEMENT);
        boolean fromGps = args.getBoolean(ARG_FROM_GPS, false);

        binding.textPreviewCity.setText(cityName);
        binding.layoutPreviewHero.setVisibility(View.INVISIBLE);

        WidgetTodayHourlyBlockBinding hourly = binding.blockTodayHourlyPreview;
        TodayHourlySectionHelper.setupBlur((AppCompatActivity) requireActivity(), hourly.blurTodayHourly);

        applyFabSavedState(lat, lon);

        binding.fabViewDetail.setOnClickListener(v ->
                onFabClicked(cityName, lat, lon, flowMode, fromGps));

        loadWeatherData(lat, lon);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        weatherRepo.cancel();
        binding = null;
    }

    private void applyFabSavedState(double lat, double lon) {
        if (WeatherPreferences.get(requireContext()).hasSavedLocationWithCoords(lat, lon)) {
            markFabAsSaved();
        }
    }

    private void markFabAsSaved() {
        binding.fabViewDetail.setEnabled(false);
        binding.fabViewDetail.setText(R.string.location_already_saved);
        binding.fabViewDetail.setAlpha(0.5f);
    }

    private void loadWeatherData(double lat, double lon) {
        showLoading(true);
        weatherRepo.fetchForecastForHome(lat, lon, "vi",
                new WeatherRepository.HomeForecastListener() {
                    @Override
                    public void onSuccess(@NonNull ForecastResponse body) {
                        if (binding == null) return;
                        requireActivity().runOnUiThread(() -> {
                            if (binding == null) return;
                            showLoading(false);
                            bindForecastToPreview(body);
                        });
                    }

                    @Override
                    public void onFailure(@Nullable String message) {
                        if (binding == null) return;
                        requireActivity().runOnUiThread(() -> {
                            if (binding == null) return;
                            showLoading(false);
                            if (message != null) {
                                Toast.makeText(requireContext(),
                                        R.string.weather_load_failed, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
    }

    private void bindForecastToPreview(@NonNull ForecastResponse r) {
        CurrentDto cur = r.getCurrent();
        if (cur != null) {
            if (cur.getTempC() != null) {
                binding.textPreviewTemp.setText(
                        String.format(Locale.getDefault(), "%.0f°", cur.getTempC()));
            }
            ConditionDto cond = cur.getCondition();
            if (cond != null) {
                if (cond.getText() != null) {
                    binding.textPreviewCondition.setText(cond.getText());
                }
                String iconUrl = WeatherApiIcons.url(
                        cond.getIcon(), cur.isDaytime(), WeatherApiIcons.SIZE_HERO);
                if (iconUrl != null && !iconUrl.isEmpty()) {
                    Glide.with(this)
                            .load(iconUrl)
                            .placeholder(R.drawable.ic_weather_placeholder)
                            .error(R.drawable.ic_weather_placeholder)
                            .into(binding.imagePreviewWeatherIcon);
                }
            }
        }
        binding.layoutPreviewHero.setVisibility(View.VISIBLE);

        String dateLabel = new SimpleDateFormat("d/M", new Locale("vi", "VN")).format(new Date());
        TodayHourlySectionHelper.bindHeader(binding.blockTodayHourlyPreview, dateLabel);

        List<HourItemDto> hours = null;
        if (r.getForecast() != null && r.getForecast().getForecastday() != null
                && !r.getForecast().getForecastday().isEmpty()) {
            ApiForecastDayDto today = r.getForecast().getForecastday().get(0);
            if (today != null) hours = today.getHour();
        }
        int currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        TodayHourlySectionHelper.bindRecyclerFromApi(
                binding.blockTodayHourlyPreview.recyclerTodayHourly, hours, currentHour);
    }

    private void showLoading(boolean loading) {
        binding.progressPreview.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    private void onFabClicked(String cityName, double lat, double lon, int flowMode, boolean fromGps) {
        WeatherPreferences prefs = WeatherPreferences.get(requireContext());
        SavedLocation loc = new SavedLocation(cityName, lat, lon, getString(R.string.frame_note));
        if (fromGps) {
            loc.setFromGps(true);
        }

        if (flowMode == LocationContract.FLOW_ONBOARDING) {
            prefs.setCurrentLocation(lat, lon, cityName, fromGps);
            prefs.addOrUpdateLocation(loc);
            if (fromGps) {
                prefs.markGpsLocation(lat, lon);
            }
            dismiss();
            HomeActivity.startClearTask(requireContext(), lat, lon, cityName);
            requireActivity().finishAffinity();
            return;
        }

        boolean alreadySaved = prefs.hasSavedLocationWithCoords(lat, lon);
        boolean added = prefs.addOrUpdateLocation(loc);
        if (!added) {
            Toast.makeText(requireContext(), R.string.max_locations_reached, Toast.LENGTH_SHORT).show();
            return;
        }
        if (fromGps) {
            prefs.markGpsLocation(lat, lon);
        }

        Toast.makeText(
                requireContext(),
                alreadySaved ? R.string.location_already_saved : R.string.location_added,
                Toast.LENGTH_SHORT
        ).show();
        dismiss();
        // Thêm xong → quay lại danh sách vị trí đã thêm (đóng màn Search đang host preview).
        androidx.fragment.app.FragmentActivity host = requireActivity();
        host.finish();
        ActivityTransitions.slideOut(host);
    }
}
