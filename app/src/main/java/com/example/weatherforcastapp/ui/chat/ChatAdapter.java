package com.example.weatherforcastapp.ui.chat;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.weatherforcastapp.R;

import java.util.ArrayList;
import java.util.List;

/** Hiển thị bong bóng chat: tin của người dùng canh phải, tin của AI canh trái. */
public final class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.VH> {

    private final List<ChatItem> items = new ArrayList<>();

    public void add(@NonNull ChatItem item) {
        items.add(item);
        notifyItemInserted(items.size() - 1);
    }

    /** Cập nhật nội dung tin cuối (dùng để thay "đang trả lời..." bằng câu trả lời thật). */
    public void updateLast(@NonNull ChatItem item) {
        if (items.isEmpty()) return;
        int pos = items.size() - 1;
        items.set(pos, item);
        notifyItemChanged(pos);
    }

    public int lastIndex() {
        return items.size() - 1;
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chat_message, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        ChatItem item = items.get(position);
        holder.bubble.setText(item.text);
        holder.bubble.setBackgroundResource(
                item.fromUser ? R.drawable.bg_chat_bubble_user : R.drawable.bg_chat_bubble_bot);

        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) holder.bubble.getLayoutParams();
        lp.gravity = item.fromUser ? Gravity.END : Gravity.START;
        holder.bubble.setLayoutParams(lp);
    }

    static final class VH extends RecyclerView.ViewHolder {
        final TextView bubble;

        VH(@NonNull android.view.View itemView) {
            super(itemView);
            bubble = itemView.findViewById(R.id.textBubble);
        }
    }
}
