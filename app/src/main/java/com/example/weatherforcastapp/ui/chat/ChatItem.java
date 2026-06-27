package com.example.weatherforcastapp.ui.chat;

import androidx.annotation.NonNull;

/** Một dòng tin nhắn hiển thị trong khung chat AI. */
public final class ChatItem {

    public final String text;
    public final boolean fromUser;

    public ChatItem(@NonNull String text, boolean fromUser) {
        this.text = text;
        this.fromUser = fromUser;
    }

    public static ChatItem user(@NonNull String text) {
        return new ChatItem(text, true);
    }

    public static ChatItem bot(@NonNull String text) {
        return new ChatItem(text, false);
    }
}
