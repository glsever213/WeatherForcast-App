package com.example.weatherforcastapp.ai.model;

import androidx.annotation.Nullable;

import java.util.List;

/** Phản hồi từ {@code /v1/chat/completions} (chỉ map các trường cần dùng). */
public final class ChatCompletionResponse {

    private List<Choice> choices;

    @Nullable
    public String firstContent() {
        if (choices == null || choices.isEmpty()) {
            return null;
        }
        Choice c = choices.get(0);
        if (c == null || c.message == null) {
            return null;
        }
        return c.message.content;
    }

    public static final class Choice {
        public Message message;
    }

    public static final class Message {
        public String role;
        public String content;
    }
}
