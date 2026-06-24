package com.example.weatherforcastapp.util;

import android.graphics.Color;

import com.example.weatherforcastapp.R;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Dữ liệu mẫu MPAndroidChart — team thay bằng dữ liệu API. */
public final class ChartSamples {

    private ChartSamples() {
    }

    public static void styleHourlyChart(LineChart chart) {
        chart.getDescription().setEnabled(false);
        chart.setTouchEnabled(true);
        chart.setDragEnabled(true);
        chart.setScaleEnabled(false);
        chart.setPinchZoom(false);
        chart.getLegend().setEnabled(false);
        chart.setExtraOffsets(8f, 16f, 8f, 8f);

        XAxis x = chart.getXAxis();
        x.setPosition(XAxis.XAxisPosition.BOTTOM);
        x.setDrawGridLines(false);
        x.setTextColor(Color.DKGRAY);
        x.setGranularity(1f);
        x.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int h = (int) value;
                if (h == 0) return "Bây giờ";
                return String.format(Locale.getDefault(), "%02d:00", h);
            }
        });

        chart.getAxisLeft().setTextColor(Color.DKGRAY);
        chart.getAxisLeft().setDrawGridLines(true);
        chart.getAxisRight().setEnabled(false);

        List<Entry> entries = new ArrayList<>();
        float[] temps = {26f, 25.5f, 25f, 25.2f, 25f, 24.8f};
        for (int i = 0; i < temps.length; i++) {
            entries.add(new Entry(i, temps[i]));
        }
        LineDataSet set = new LineDataSet(entries, "°C");
        set.setColor(chart.getContext().getColor(R.color.chart_line));
        set.setLineWidth(2f);
        set.setDrawCircles(true);
        set.setCircleColor(Color.WHITE);
        set.setCircleHoleColor(chart.getContext().getColor(R.color.chart_line));
        set.setDrawValues(true);
        set.setValueTextColor(Color.DKGRAY);
        set.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        //chart.setData(new LineData(set));
        chart.invalidate();
    }

    public static void stylePreviewHighLowChart(LineChart chart) {
        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(true);
        chart.getLegend().setTextColor(Color.WHITE);
        chart.setExtraOffsets(8f, 8f, 8f, 8f);

        XAxis x = chart.getXAxis();
        x.setPosition(XAxis.XAxisPosition.BOTTOM);
        x.setTextColor(Color.WHITE);
        x.setDrawGridLines(false);

        chart.getAxisLeft().setTextColor(Color.WHITE);
        chart.getAxisLeft().setDrawGridLines(false);
        chart.getAxisRight().setEnabled(false);

        List<Entry> highs = new ArrayList<>();
        List<Entry> lows = new ArrayList<>();
        float[] h = {25f, 25f, 28f, 26f, 27f};
        float[] l = {22f, 22f, 22f, 22f, 22f};
        for (int i = 0; i < h.length; i++) {
            highs.add(new Entry(i, h[i]));
            lows.add(new Entry(i, l[i]));
        }

        LineDataSet highSet = new LineDataSet(highs, "Cao");
        highSet.setColor(Color.WHITE);
        highSet.setCircleColor(Color.WHITE);
        highSet.setDrawValues(true);
        highSet.setValueTextColor(Color.WHITE);
        highSet.setMode(LineDataSet.Mode.LINEAR);

        LineDataSet lowSet = new LineDataSet(lows, "Thấp");
        lowSet.setColor(0x88FFFFFF);
        lowSet.setCircleColor(Color.WHITE);
        lowSet.setDrawValues(true);
        lowSet.setValueTextColor(Color.WHITE);
        lowSet.setMode(LineDataSet.Mode.LINEAR);

        chart.setData(new LineData(highSet, lowSet));
        chart.invalidate();
    }
}
