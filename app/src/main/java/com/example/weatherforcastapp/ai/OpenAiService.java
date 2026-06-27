package com.example.weatherforcastapp.ai;

import com.example.weatherforcastapp.ai.model.ChatCompletionRequest;
import com.example.weatherforcastapp.ai.model.ChatCompletionResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * Retrofit cho OpenAI Chat Completions API.
 *
 * @see <a href="https://platform.openai.com/docs/api-reference/chat">OpenAI Chat API</a>
 */
public interface OpenAiService {

    @POST("chat/completions")
    Call<ChatCompletionResponse> chat(@Body ChatCompletionRequest request);
}
