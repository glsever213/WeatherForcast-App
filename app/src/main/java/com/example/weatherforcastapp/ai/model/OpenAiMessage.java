package com.example.weatherforcastapp.ai.model;

import androidx.annotation.NonNull;

/** Một tin nhắn trong hội thoại gửi/nhận với OpenAI Chat Completions. */
public final class OpenAiMessage {

    public static final String ROLE_SYSTEM = "system";
    public static final String ROLE_USER = "user";
    public static final String ROLE_ASSISTANT = "assistant";

    private final String role;
    private final String content;

    public OpenAiMessage(@NonNull String role, @NonNull String content) {
        this.role = role;
        this.content = content;
    }

    public static OpenAiMessage system(@NonNull String content) {
        return new OpenAiMessage(ROLE_SYSTEM, content);
    }

    public static OpenAiMessage user(@NonNull String content) {
        return new OpenAiMessage(ROLE_USER, content);
    }

    public static OpenAiMessage assistant(@NonNull String content) {
        return new OpenAiMessage(ROLE_ASSISTANT, content);
    }

    public String getRole() {
        return role;
    }

    public String getContent() {
        return content;
    }
}
