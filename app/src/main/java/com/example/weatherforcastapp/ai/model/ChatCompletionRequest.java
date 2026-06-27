package com.example.weatherforcastapp.ai.model;

import androidx.annotation.NonNull;

import java.util.List;

/** Body cho POST {@code /v1/chat/completions}. */
public final class ChatCompletionRequest {

    private final String model;
    private final List<OpenAiMessage> messages;
    private final double temperature;

    public ChatCompletionRequest(@NonNull String model, @NonNull List<OpenAiMessage> messages, double temperature) {
        this.model = model;
        this.messages = messages;
        this.temperature = temperature;
    }

    public String getModel() {
        return model;
    }

    public List<OpenAiMessage> getMessages() {
        return messages;
    }

    public double getTemperature() {
        return temperature;
    }
}
