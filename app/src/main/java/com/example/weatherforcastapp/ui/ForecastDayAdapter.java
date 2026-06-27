package com.example.weatherforcastapp.ui;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.weatherforcastapp.databinding.ItemForecastDayRowBinding;

import java.util.ArrayList;
import java.util.List;

public final class ForecastDayAdapter extends RecyclerView.Adapter<ForecastDayAdapter.VH> {

    public static final class Row {
        public final String dayLabel;
        public final String low;
        public final String high;
        public final String condition;

        public Row(String dayLabel, String low, String high, String condition) {
            this.dayLabel = dayLabel;
            this.low = low;
            this.high = high;
            this.condition = condition;
        }
    }

    private final List<Row> data = new ArrayList<>();

    public ForecastDayAdapter() {
        data.add(new Row("Hôm nay", "24°", "29°", "Có mây"));
        data.add(new Row("Ngày mai", "24°", "30°", "Có mây"));
        data.add(new Row("CN", "24°", "30°", "Có mây"));
    }

    public void setRows(List<Row> rows) {
        data.clear();
        if (rows != null) data.addAll(rows);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemForecastDayRowBinding b = ItemForecastDayRowBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new VH(b);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Row r = data.get(position);
        holder.binding.textDayLabel.setText(r.dayLabel);
        holder.binding.textDaySummary.setText(r.condition + " - " + r.high + " / " + r.low);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static final class VH extends RecyclerView.ViewHolder {
        final ItemForecastDayRowBinding binding;

        VH(ItemForecastDayRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
