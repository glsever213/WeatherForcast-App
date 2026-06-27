package com.example.weatherforcastapp.ai;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.weatherforcastapp.ai.model.ChatCompletionRequest;
import com.example.weatherforcastapp.ai.model.ChatCompletionResponse;
import com.example.weatherforcastapp.ai.model.OpenAiMessage;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Điều phối hội thoại với OpenAI: ghép prompt hệ thống (gồm ngữ cảnh thời tiết từ Home)
 * + lịch sử hội thoại + câu hỏi mới, rồi gọi {@link OpenAiService}.
 */
public final class WeatherChatRepository {

    /** Groq Llama 3.3 70B: miễn phí, rất nhanh, đủ tốt cho hỏi đáp thời tiết. Có thể đổi sang model khác. */
    private static final String MODEL = "llama-3.3-70b-versatile";
    private static final double TEMPERATURE = 0.4;

    private static final String SYSTEM_PROMPT_TEMPLATE =
            "Bạn là trợ lý thời tiết thân thiện trong một ứng dụng Android. "
                    + "Phạm vi trả lời: các chủ đề xoay quanh DỰ BÁO THỜI TIẾT và VỊ TRÍ HIỆN TẠI của người dùng, gồm: "
                    + "thời tiết và dự báo; vị trí, địa điểm và khu vực xung quanh vị trí hiện tại; địa điểm du lịch/tham quan gần đó; "
                    + "và các gợi ý, việc nên làm dựa trên thời tiết cùng vị trí hiện tại (mặc gì, mang ô, chống nắng, nên đi đâu, "
                    + "nên làm gì, đi lại, hoạt động ngoài trời...). "
                    + "Mọi gợi ý mang tính địa điểm (ví dụ nên đi đâu, chỗ nào nên ghé) hãy DỰA VÀO vị trí hiện tại đã nạp trong dữ liệu bên dưới "
                    + "và điều kiện thời tiết của nó để phỏng đoán, rồi đề xuất cho phù hợp. "
                    + "Nếu người dùng hỏi chủ đề NẰM NGOÀI phạm vi trên, hãy lịch sự từ chối ngắn gọn và mời họ hỏi về thời tiết hoặc địa điểm quanh vị trí hiện tại. "
                    + "Đừng bịa số liệu cụ thể (nhiệt độ, độ ẩm, giờ...) cho địa điểm của người dùng nếu dữ liệu không có; "
                    + "khi đó hãy nói rõ là chưa có số liệu chính xác và đưa thông tin chung mang tính tham khảo. "
                    + "Trả lời ngắn gọn, dễ hiểu, bằng tiếng Việt.\n\n"
                    + "%s";

    public interface ChatListener {
        void onReply(@NonNull String reply);

        void onError(@NonNull String message);
    }

    private final OpenAiService service = OpenAiClient.api();
    private Call<ChatCompletionResponse> pending;

    public void cancel() {
        if (pending != null) {
            pending.cancel();
            pending = null;
        }
    }

    /**
     * @param weatherContext kết quả của {@link WeatherContextBuilder#build(ForecastResponse, String)}
     * @param history        lịch sử hội thoại (user/assistant) KHÔNG gồm system
     * @param userMessage    câu hỏi mới của người dùng
     */
    public void send(@NonNull String weatherContext,
                     @NonNull List<OpenAiMessage> history,
                     @NonNull String userMessage,
                     @NonNull ChatListener listener) {
        if (!OpenAiClient.hasApiKey()) {
            listener.onError("Chưa cấu hình GROQ_API_KEY trong local.properties.");
            return;
        }

        List<OpenAiMessage> messages = new ArrayList<>();
        messages.add(OpenAiMessage.system(String.format(SYSTEM_PROMPT_TEMPLATE, weatherContext)));
        messages.addAll(history);
        messages.add(OpenAiMessage.user(userMessage));

        cancel();
        pending = service.chat(new ChatCompletionRequest(MODEL, messages, TEMPERATURE));
        pending.enqueue(new Callback<ChatCompletionResponse>() {
            @Override
            public void onResponse(@NonNull Call<ChatCompletionResponse> call,
                                   @NonNull Response<ChatCompletionResponse> response) {
                if (call.isCanceled()) return;
                ChatCompletionResponse body = response.body();
                if (response.isSuccessful() && body != null) {
                    String reply = body.firstContent();
                    if (reply != null && !reply.trim().isEmpty()) {
                        listener.onReply(reply.trim());
                        return;
                    }
                    listener.onError("Groq không trả về nội dung.");
                    return;
                }
                listener.onError(describeError(response));
            }

            @Override
            public void onFailure(@NonNull Call<ChatCompletionResponse> call, @NonNull Throwable t) {
                if (call.isCanceled()) return;
                listener.onError("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    private static String describeError(@Nullable Response<ChatCompletionResponse> response) {
        if (response == null) return "Lỗi không xác định.";
        int code = response.code();

        String detail = readErrorMessage(response);

        String prefix;
        switch (code) {
            case 400: prefix = "Yêu cầu không hợp lệ / API key sai (400)"; break;
            case 401:
            case 403: prefix = "API key Groq không hợp lệ / chưa bật quyền (" + code + ")"; break;
            case 404: prefix = "Sai tên model hoặc endpoint (404)"; break;
            case 429: prefix = "Vượt hạn mức / quá nhiều yêu cầu (429)"; break;
            default: prefix = "Groq lỗi " + code; break;
        }
        return detail != null && !detail.isEmpty() ? prefix + ": " + detail : prefix + ".";
    }

    /** Lấy field {@code error.message} từ body lỗi của Gemini để biết lý do chính xác. */
    @Nullable
    private static String readErrorMessage(@NonNull Response<ChatCompletionResponse> response) {
        if (response.errorBody() == null) return null;
        try {
            String raw = response.errorBody().string();
            if (raw == null || raw.isEmpty()) return null;
            int idx = raw.indexOf("\"message\"");
            if (idx < 0) return raw.length() > 200 ? raw.substring(0, 200) : raw;
            int colon = raw.indexOf(':', idx);
            int start = raw.indexOf('"', colon + 1);
            int end = raw.indexOf('"', start + 1);
            // bỏ qua dấu " bị escape trong message
            while (end > 0 && raw.charAt(end - 1) == '\\') {
                end = raw.indexOf('"', end + 1);
            }
            if (start > 0 && end > start) {
                return raw.substring(start + 1, end);
            }
            return raw.length() > 200 ? raw.substring(0, 200) : raw;
        } catch (Exception e) {
            return null;
        }
    }
}
