package com.example.weatherforcastapp.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.weatherforcastapp.R;

import java.util.List;

public class AIAdapter extends RecyclerView.Adapter<AIAdapter.AIviewholder> {
    private List<String> suggestions;
    private OnItemClickListener listener;
    public interface OnItemClickListener {
        void onItemClick(String suggestion);
    }

    public AIAdapter(List<String> suggestions, OnItemClickListener listener) {
        this.suggestions = suggestions;
        this.listener = listener;
    }
    @NonNull
    @Override
    public AIviewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_suggession, parent,
                false);

        return new AIviewholder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AIviewholder holder, int position) {
        String suggestion = suggestions.get(position);
        holder.txtSuggestion.setText(suggestion);//gan du lieu vao item
        holder.itemView.setOnClickListener(v -> {//su kien khi nhan vao item
            listener.onItemClick(suggestion);
        });
    }

    @Override
    public int getItemCount() {
        return suggestions.size();
    }

    public class AIviewholder extends RecyclerView.ViewHolder {
        private TextView txtSuggestion;
        public AIviewholder(View itemView) {
            super(itemView);
            txtSuggestion = itemView.findViewById(R.id.txtSuggestion);
        }
    }
}
