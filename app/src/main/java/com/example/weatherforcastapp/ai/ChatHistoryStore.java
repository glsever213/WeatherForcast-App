package com.example.weatherforcastapp.ai;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.weatherforcastapp.ai.model.OpenAiMessage;
import com.example.weatherforcastapp.ui.chat.ChatItem;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.List;

public final class ChatHistoryStore {

    private static final String PREFS = "chat_history";
    private static final String KEY_TIME = "saved_at";
    private static final String KEY_CHAT = "chat_items";
    private static final String KEY_HISTORY = "ai_history";
    private static final long EXPIRY_MS = 20 * 60 * 1000L;

    private ChatHistoryStore() {}

    public static void save(Context ctx, List<ChatItem> chatItems, List<OpenAiMessage> history) {
        if (chatItems.isEmpty()) return;
        Gson gson = new Gson();
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
                .putLong(KEY_TIME, System.currentTimeMillis())
                .putString(KEY_CHAT, gson.toJson(chatItems))
                .putString(KEY_HISTORY, gson.toJson(history))
                .apply();
    }

    public static boolean hasValid(Context ctx) {
        SharedPreferences prefs = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        long savedAt = prefs.getLong(KEY_TIME, 0);
        return savedAt > 0 && (System.currentTimeMillis() - savedAt) < EXPIRY_MS;
    }

    public static List<ChatItem> loadChatItems(Context ctx) {
        String json = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_CHAT, null);
        if (json == null) return null;
        return new Gson().fromJson(json, new TypeToken<List<ChatItem>>() {}.getType());
    }

    public static List<OpenAiMessage> loadHistory(Context ctx) {
        String json = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_HISTORY, null);
        if (json == null) return null;
        return new Gson().fromJson(json, new TypeToken<List<OpenAiMessage>>() {}.getType());
    }

    public static void clear(Context ctx) {
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().clear().apply();
    }
}
