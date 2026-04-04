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

        public Row(String dayLabel, String low, String high) {
            this.dayLabel = dayLabel;
            this.low = low;
            this.high = high;
        }
    }

    private final List<Row> data = new ArrayList<>();

    public ForecastDayAdapter() {
        data.add(new Row("Hôm nay", "24°", "29°"));
        data.add(new Row("Ngày mai", "24°", "30°"));
        data.add(new Row("CN", "24°", "30°"));
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
        holder.binding.textLow.setText(r.low);
        holder.binding.textHigh.setText(r.high);
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
