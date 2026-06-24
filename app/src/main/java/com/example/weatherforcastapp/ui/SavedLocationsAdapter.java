package com.example.weatherforcastapp.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.weatherforcastapp.R;
import com.example.weatherforcastapp.api.WeatherApiIcons;
import com.example.weatherforcastapp.databinding.ItemSavedLocationBinding;
import com.example.weatherforcastapp.model.SavedLocation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class SavedLocationsAdapter extends RecyclerView.Adapter<SavedLocationsAdapter.VH> {

    public interface Listener {
        void onOpen(@NonNull SavedLocation location);

        void onBeginSelection(@NonNull SavedLocation location);
    }

    private final List<SavedLocation> items = new ArrayList<>();
    private final Listener listener;
    private boolean selectionMode;
    private final Set<String> selectedIds = new HashSet<>();

    public SavedLocationsAdapter(Listener listener) {
        this.listener = listener;
    }

    public void setItems(List<SavedLocation> list) {
        items.clear();
        if (list != null) items.addAll(list);
        notifyDataSetChanged();
    }

    public void setSelectionMode(boolean on) {
        this.selectionMode = on;
        if (!on) selectedIds.clear();
        notifyDataSetChanged();
    }

    public boolean isSelectionMode() {
        return selectionMode;
    }

    public Set<String> getSelectedIds() {
        return new HashSet<>(selectedIds);
    }

    private void toggleSelected(String id) {
        if (selectedIds.contains(id)) selectedIds.remove(id);
        else selectedIds.add(id);
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemSavedLocationBinding b = ItemSavedLocationBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new VH(b);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        SavedLocation loc = items.get(position);
        holder.binding.textCityName.setText(loc.getDisplayName());
        holder.binding.textCondition.setText(loc.getCachedSummaryLine().isEmpty() ? "—" : loc.getCachedSummaryLine());
        holder.binding.textHighLow.setText(loc.getCachedHighLow());
        holder.binding.textHumidity.setText(loc.getCachedHumidity());
        holder.binding.textTempBig.setText(loc.getCachedTemp());

        String iconCode = loc.getCachedIconCode();
        if (iconCode != null && !iconCode.isEmpty()) {
            String url = WeatherApiIcons.url(iconCode, true, WeatherApiIcons.SIZE_LIST);
            Glide.with(holder.itemView.getContext())
                    .load(url)
                    .placeholder(R.drawable.ic_weather_placeholder)
                    .error(R.drawable.ic_weather_placeholder)
                    .into(holder.binding.iconWeather);
        } else {
            holder.binding.iconWeather.setImageResource(R.drawable.ic_weather_placeholder);
        }

        holder.binding.checkSelect.setVisibility(selectionMode ? View.VISIBLE : View.GONE);
        holder.binding.checkSelect.setChecked(selectedIds.contains(loc.getId()));

        View.OnClickListener rowClick = v -> {
            int pos = holder.getBindingAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return;
            SavedLocation l = items.get(pos);
            if (selectionMode) {
                toggleSelected(l.getId());
                notifyItemChanged(pos);
                return;
            }
            listener.onOpen(l);
        };

        holder.itemView.setOnClickListener(rowClick);
        holder.binding.checkSelect.setOnClickListener(v -> {
            int pos = holder.getBindingAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return;
            SavedLocation l = items.get(pos);
            toggleSelected(l.getId());
            notifyItemChanged(pos);
        });

        holder.itemView.setOnLongClickListener(v -> {
            int pos = holder.getBindingAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return true;
            listener.onBeginSelection(items.get(pos));
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    /** Vào chế độ chọn từ long-press một dòng. */
    public void enterSelectionWith(@NonNull SavedLocation location) {
        selectionMode = true;
        selectedIds.add(location.getId());
        notifyDataSetChanged();
    }

    static final class VH extends RecyclerView.ViewHolder {
        final ItemSavedLocationBinding binding;

        VH(ItemSavedLocationBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

    }
}
