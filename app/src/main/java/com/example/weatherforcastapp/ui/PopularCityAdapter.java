package com.example.weatherforcastapp.ui;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.weatherforcastapp.data.PopularCity;
import com.example.weatherforcastapp.databinding.ItemPopularCityBinding;

import java.util.List;

public class PopularCityAdapter
        extends RecyclerView.Adapter<PopularCityAdapter.VH> {

    public interface Listener {
        void onClick(PopularCity city);
    }

    private final List<PopularCity> cities;
    private final Listener listener;

    public PopularCityAdapter(List<PopularCity> cities, Listener listener) {
        this.cities = cities;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemPopularCityBinding binding =
                ItemPopularCityBinding.inflate(
                        LayoutInflater.from(parent.getContext()),
                        parent,
                        false
                );
        return new VH(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        PopularCity city = cities.get(position);
        holder.binding.btnCity.setText(city.name);
        holder.binding.btnCity.setOnClickListener(v -> listener.onClick(city));
    }

    @Override
    public int getItemCount() {
        return cities.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        ItemPopularCityBinding binding;

        VH(ItemPopularCityBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}