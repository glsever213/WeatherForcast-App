package com.example.weatherforcastapp;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.weatherforcastapp.ai.WeatherChatRepository;
import com.example.weatherforcastapp.ai.WeatherContextBuilder;
import com.example.weatherforcastapp.ai.model.OpenAiMessage;
import com.example.weatherforcastapp.data.WeatherRepository;
import com.example.weatherforcastapp.databinding.ActivityAiBinding;
import com.example.weatherforcastapp.model.api.ForecastResponse;
import com.example.weatherforcastapp.prefs.WeatherPreferences;
import com.example.weatherforcastapp.ui.AIAdapter;
import com.example.weatherforcastapp.ui.chat.ChatAdapter;
import com.example.weatherforcastapp.ui.chat.ChatItem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Trợ lý thời tiết AI: khi mở màn sẽ gọi lại API thời tiết (đúng vị trí Home đang dùng) để nạp
 * dữ liệu làm ngữ cảnh, sau đó cho hỏi đáp. Có 3 câu hỏi gợi ý (bấm là chat trả lời ngay)
 * hoặc người dùng tự nhập.
 */
public class AIActivity extends AppCompatActivity {

    private ActivityAiBinding binding;
    private final WeatherChatRepository repository = new WeatherChatRepository();
    private final WeatherRepository weatherRepo = new WeatherRepository();
    private final ChatAdapter chatAdapter = new ChatAdapter();
    private final List<OpenAiMessage> history = new ArrayList<>();

    private WeatherPreferences prefs;
    private String weatherContext = "";
    private boolean weatherReady = false;

    public static void start(@NonNull Context context) {
        context.startActivity(new Intent(context, AIActivity.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAiBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        prefs = WeatherPreferences.get(this);

        binding.rvChat.setLayoutManager(new LinearLayoutManager(this));
        binding.rvChat.setAdapter(chatAdapter);

        setupSuggestions();
        setupInput();

        binding.buttonBackForecast.setOnClickListener(v -> finish());
        binding.btnSend.setOnClickListener(v -> sendMessage(binding.edtInput.getText().toString()));

        loadWeatherContext();
    }

    /** Gọi lại API thời tiết (như Home) rồi build ngữ cảnh cho AI đọc. */
    private void loadWeatherContext() {
        setWeatherLoading(true);

        double lat = prefs.getCurrentLat();
        double lon = prefs.getCurrentLon();
        if (Math.abs(lat) < 1e-5 && Math.abs(lon) < 1e-5) {
            weatherContext = WeatherContextBuilder.build(null);
            weatherReady = true;
            setWeatherLoading(false);
            return;
        }

        weatherRepo.fetchForecastForHome(lat, lon, prefs, true, "vi",
                new WeatherRepository.HomeForecastListener() {
                    @Override
                    public void onSuccess(@NonNull ForecastResponse body) {
                        runOnUiThread(() -> {
                            if (binding == null) return;
                            weatherContext = WeatherContextBuilder.build(body);
                            weatherReady = true;
                            setWeatherLoading(false);
                        });
                    }

                    @Override
                    public void onFailure(@Nullable String message) {
                        runOnUiThread(() -> {
                            if (binding == null) return;
                            // Vẫn cho chat, nhưng báo là chưa có dữ liệu.
                            weatherContext = WeatherContextBuilder.build(null);
                            weatherReady = true;
                            setWeatherLoading(false);
                            Toast.makeText(AIActivity.this, R.string.weather_load_failed, Toast.LENGTH_SHORT).show();
                        });
                    }
                });
    }

    /** Đang tải dữ liệu thời tiết: hiện spinner giữa màn, ẩn lời chào/gợi ý, khoá nhập. */
    private void setWeatherLoading(boolean loading) {
        binding.loadingLayout.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.txtMessage.setVisibility(loading ? View.GONE : View.VISIBLE);
        binding.rvSuggestions.setVisibility(loading ? View.GONE : View.VISIBLE);
        binding.edtInput.setEnabled(!loading);
        if (loading) {
            setSendEnabled(false);
        }
    }

    private void setupSuggestions() {
        List<String> list = Arrays.asList(
                getString(R.string.ai_suggest_today),
                getString(R.string.ai_suggest_3days),
                getString(R.string.ai_suggest_feels)
        );
        binding.rvSuggestions.setLayoutManager(new LinearLayoutManager(this));
        // Bấm 1 gợi ý là gửi luôn để AI trả lời.
        binding.rvSuggestions.setAdapter(new AIAdapter(list, this::sendMessage));
    }

    private void setupInput() {
        setSendEnabled(false);
        binding.edtInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int count, int after) {
                setSendEnabled(weatherReady && !s.toString().trim().isEmpty());
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });
    }

    private void setSendEnabled(boolean enabled) {
        binding.btnSend.setEnabled(enabled);
        binding.btnSend.setBackgroundTintList(ColorStateList.valueOf(
                Color.parseColor(enabled ? "#1E3C72" : "#808080")));
    }

    private void sendMessage(@Nullable String raw) {
        String text = raw != null ? raw.trim() : "";
        if (text.isEmpty() || !weatherReady) return;

        // Lần đầu gửi: ẩn lời chào + gợi ý, hiện khung hội thoại.
        binding.txtMessage.setVisibility(View.GONE);
        binding.rvSuggestions.setVisibility(View.GONE);
        binding.rvChat.setVisibility(View.VISIBLE);

        binding.edtInput.setText("");
        chatAdapter.add(ChatItem.user(text));
        chatAdapter.add(ChatItem.bot(getString(R.string.ai_thinking)));
        scrollToBottom();

        repository.send(weatherContext, new ArrayList<>(history), text, new WeatherChatRepository.ChatListener() {
            @Override
            public void onReply(@NonNull String reply) {
                runOnUiThread(() -> {
                    if (binding == null) return;
                    history.add(OpenAiMessage.user(text));
                    history.add(OpenAiMessage.assistant(reply));
                    chatAdapter.updateLast(ChatItem.bot(reply));
                    scrollToBottom();
                });
            }

            @Override
            public void onError(@NonNull String message) {
                runOnUiThread(() -> {
                    if (binding == null) return;
                    chatAdapter.updateLast(ChatItem.bot("⚠️ " + message));
                    scrollToBottom();
                });
            }
        });
    }

    private void scrollToBottom() {
        binding.rvChat.post(() -> binding.rvChat.smoothScrollToPosition(chatAdapter.lastIndex()));
    }

    @Override
    protected void onDestroy() {
        repository.cancel();
        weatherRepo.cancel();
        binding = null;
        super.onDestroy();
    }
}
