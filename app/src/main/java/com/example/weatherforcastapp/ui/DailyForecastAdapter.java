package com.example.weatherforcastapp.ui;

import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.weatherforcastapp.R;
import com.example.weatherforcastapp.api.WeatherApiIcons;
import com.example.weatherforcastapp.databinding.ItemDailyForecastLineBinding;
import com.google.android.material.textview.MaterialTextView;

import java.util.ArrayList;
import java.util.List;

public final class DailyForecastAdapter extends RecyclerView.Adapter<DailyForecastAdapter.VH> {

    public static final class Slot {
        public final String dateLabel;
        public final String condition;
        public final String iconCode;
        public final String highLow;
        public final List<String> metricLines;

        public Slot(String dateLabel, String condition, String iconCode, String highLow, List<String> metricLines) {
            this.dateLabel = dateLabel;
            this.condition = condition;
            this.iconCode = iconCode;
            this.highLow = highLow;
            this.metricLines = metricLines != null ? metricLines : new ArrayList<>();
        }
    }

    private final List<Slot> data = new ArrayList<>();

    public DailyForecastAdapter(List<Slot> slots) {
        if (slots != null) {
            data.addAll(slots);
        }
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemDailyForecastLineBinding b = ItemDailyForecastLineBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new VH(b);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Slot s = data.get(position);
        holder.binding.textDailyDate.setText(s.dateLabel);
        holder.binding.textIconNote.setText(s.condition);
        holder.binding.textDailyTemp.setText(s.highLow);

        bindMetricGrid(holder.binding.layoutMetrics, s.metricLines);

        if (s.iconCode != null && !s.iconCode.isEmpty()) {
            WeatherApiIcons.loadInto(
                    holder.itemView.getContext(),
                    holder.binding.iconDaily,
                    s.iconCode,
                    true,
                    56f,
                    R.drawable.ic_weather_placeholder
            );
        } else {
            holder.binding.iconDaily.setImageResource(R.drawable.ic_weather_placeholder);
        }
    }

    private static void bindMetricGrid(@NonNull LinearLayout container, @NonNull List<String> lines) {
        container.removeAllViews();
        if (lines.isEmpty()) {
            return;
        }

        float density = container.getResources().getDisplayMetrics().density;
        int rowGap = (int) (10 * density);
        int colGap = (int) (8 * density);
        int cellPad = (int) (4 * density);

        for (int i = 0; i < lines.size(); i += 2) {
            LinearLayout row = new LinearLayout(container.getContext());
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setBaselineAligned(false);

            LinearLayout.LayoutParams rowLp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            if (container.getChildCount() > 0) {
                rowLp.topMargin = rowGap;
            }
            row.setLayoutParams(rowLp);

            addMetricCell(row, lines.get(i), cellPad, colGap, true);
            if (i + 1 < lines.size()) {
                addMetricCell(row, lines.get(i + 1), cellPad, colGap, false);
            } else {
                row.addView(new View(container.getContext()), new LinearLayout.LayoutParams(
                        0, 0, 1f));
            }

            container.addView(row);
        }
    }

    private static void addMetricCell(
            @NonNull LinearLayout row,
            @NonNull String line,
            int cellPad,
            int colGap,
            boolean isLeft
    ) {
        int colon = line.indexOf(": ");
        String label = colon >= 0 ? line.substring(0, colon) : line;
        String value = colon >= 0 ? line.substring(colon + 2) : "";

        LinearLayout cell = new LinearLayout(row.getContext());
        cell.setOrientation(LinearLayout.VERTICAL);
        cell.setPadding(cellPad, 0, cellPad, 0);

        LinearLayout.LayoutParams cellLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        if (!isLeft) {
            cellLp.setMarginStart(colGap);
        }
        cell.setLayoutParams(cellLp);

        MaterialTextView tvLabel = new MaterialTextView(row.getContext());
        tvLabel.setText(label);
        tvLabel.setMaxLines(2);
        tvLabel.setEllipsize(android.text.TextUtils.TruncateAt.END);
        tvLabel.setTextAppearance(R.style.TextAppearance_Weather_MetricLabel);

        MaterialTextView tvValue = new MaterialTextView(row.getContext());
        tvValue.setText(value);
        tvValue.setMaxLines(2);
        tvValue.setEllipsize(android.text.TextUtils.TruncateAt.END);
        tvValue.setTextAppearance(R.style.TextAppearance_Weather_MetricValue);
        tvValue.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f);
        LinearLayout.LayoutParams valueLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        valueLp.topMargin = (int) (2 * row.getResources().getDisplayMetrics().density);
        tvValue.setLayoutParams(valueLp);

        cell.addView(tvLabel);
        cell.addView(tvValue);
        row.addView(cell);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void setRows(List<Slot> slots) {
        data.clear();
        if (slots != null) {
            data.addAll(slots);
        }
        notifyDataSetChanged();
    }

    static final class VH extends RecyclerView.ViewHolder {
        final ItemDailyForecastLineBinding binding;

        VH(ItemDailyForecastLineBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
