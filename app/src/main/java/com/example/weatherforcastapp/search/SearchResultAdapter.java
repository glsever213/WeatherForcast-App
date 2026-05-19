package com.example.weatherforcastapp.search;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.weatherforcastapp.databinding.ItemSearchSuggestionBinding;
import com.example.weatherforcastapp.model.api.LocationDto;

import java.util.ArrayList;
import java.util.List;

/** Adapter danh sách kết quả tìm kiếm địa điểm thật. */
public final class SearchResultAdapter extends RecyclerView.Adapter<SearchResultAdapter.VH> {

    public interface Listener {
        void onPick(@NonNull LocationDto location);
    }

    private final List<LocationDto> items = new ArrayList<>();
    private final Listener listener;

    public SearchResultAdapter(@NonNull Listener listener) {
        this.listener = listener;
    }

    public void submitList(@NonNull List<LocationDto> results) {
        items.clear();
        items.addAll(results);
        notifyDataSetChanged();
    }

    public void clear() {
        items.clear();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemSearchSuggestionBinding binding = ItemSearchSuggestionBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new VH(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        LocationDto item = items.get(position);
        holder.binding.textSuggestionTitle.setText(LocationLabelFormatter.displayName(item));
        holder.binding.textSuggestionSub.setText(LocationLabelFormatter.subtitle(item));
        holder.itemView.setOnClickListener(v -> listener.onPick(item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static final class VH extends RecyclerView.ViewHolder {
        final ItemSearchSuggestionBinding binding;

        VH(ItemSearchSuggestionBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
