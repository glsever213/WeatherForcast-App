# Dự án WeatherForecast App — Tài liệu tổng quan

Ứng dụng Android dự báo thời tiết tích hợp trợ lý AI. **API thời tiết: WeatherAPI.com**. **Không dùng database**: toàn bộ dữ liệu lưu qua **SharedPreferences** (vị trí, danh sách địa điểm, cache API, lịch sử chat AI). Giới hạn **20 địa điểm** đã lưu.

---

## 1. Danh sách màn hình (Activity)

| Activity | Vai trò |
|----------|---------|
| `MainActivity` | Màn khởi động: kiểm tra vị trí đã lưu → Home hoặc hỏi GPS |
| `HomeActivity` | Trang chủ: hero thời tiết, biểu đồ 24h, dự báo 3 ngày, các chỉ số |
| `SearchActivity` | Tìm kiếm địa điểm bằng WeatherAPI |
| `LocationManagementActivity` | Quản lý danh sách địa điểm đã lưu (CRUD tối đa 20) |
| `PreviewActivity` | Xem trước thời tiết địa điểm trước khi thêm |
| `FiveDayForecastActivity` | Chi tiết dự báo 3 ngày |
| `VisualMapActivity` | Bản đồ dự báo thời tiết (Windy.com embed qua WebView) |
| `AIActivity` | Trợ lý AI thời tiết (OpenAI Chat Completions) |

Tất cả Activity khai báo `android:screenOrientation="portrait"` trong `AndroidManifest.xml`.

---

## 2. Luồng nghiệp vụ

### 2.1 Khởi động app

1. `MainActivity` kiểm tra `WeatherPreferences.hasCurrentLocation()`:
   - **Có** → `HomeActivity.startClearTask` + `finish()`.
   - **Chưa có** → `MaterialAlertDialogBuilder` hỏi cho phép GPS.

2. **Đồng ý GPS**: xin quyền → `LocationHelper.fetchCurrent` → lấy lat/lon → `LocationNameResolver` tìm tên → `WeatherPreferences.setCurrentLocation` + `addOrUpdateLocation` (lưu ngay vào danh sách) → `HomeActivity`.

3. **Từ chối GPS**: `SearchActivity.startOnboarding` + `finish()`.
   - Bấm **Hủy** / back trong onboarding → `finishAffinity()` thoát app.
   - Chọn địa điểm → `PreviewActivity` (flow onboarding).

### 2.2 Home — hiển thị thời tiết

`HomeActivity` khi khởi động hoặc nhận Intent mới:
- Đọc lat/lon từ Intent hoặc `WeatherPreferences`.
- Hiển thị tên địa điểm + placeholder, gọi `WeatherRepository.fetchForecastForHome`.
- `bindForecastToHome`: cập nhật nhiệt độ, điều kiện, icon hero (Glide + WeatherAPI CDN), AQI, biểu đồ 24h (dữ liệu thật từ `forecastday[0].hour`), dự báo 3 ngày, các chỉ số (UV, độ ẩm, cảm giác, gió, mặt trời mọc/lặn, áp suất).
- Background gradient + Lottie animation theo điều kiện thời tiết (`WeatherConditionTheme`).
- Kéo **SwipeRefresh** → làm mới dữ liệu (bỏ qua throttle).
- Cuộn xuống: hero (icon + nhiệt độ + AQI) và tên địa điểm fade out mượt qua `applyHeroVisibility` + `MotionLayout`.

### 2.3 Tìm kiếm địa điểm (`SearchActivity`)

- Giao diện: nút back + input search full-width. Không có preset locations.
- Nút GPS → dùng vị trí hiện tại.
- Nhập text → gọi WeatherAPI Search → kết quả qua `SearchResultAdapter`.
- Chọn kết quả → `PreviewActivity` + `finish()`.
- Mode **onboarding** (`FLOW_ONBOARDING`): back → `finishAffinity()`.
- Mode **quản lý** (`FLOW_MANAGEMENT`): back → `finish()` + slide out.

### 2.4 Xem trước địa điểm (`PreviewActivity`)

- Nhận lat/lon/tên + `flowMode` qua `LocationContract`.
- **Onboarding**: FAB → `setCurrentLocation` + `addOrUpdateLocation` + `HomeActivity.startClearTask` + `finishAffinity()`.
- **Quản lý**: `addOrUpdateLocation` (tối đa 20) rồi `finish()` về `LocationManagementActivity`.

### 2.5 Quản lý địa điểm (`LocationManagementActivity`)

- `RecyclerView` + `SavedLocationsAdapter`: mỗi item có gradient nền theo điều kiện thời tiết, icon WeatherAPI CDN, nhiệt độ, điều kiện.
- Nút search compact (góc phải) → `SearchActivity.startForManagement`.
- **Long-press** item → chế độ chọn nhiều → Xóa đã chọn / Xóa tất cả / Xong.
- **Tap** item (không selection) → `setCurrentLocation` + `HomeActivity.startClearTop` + `finish()`.

### 2.6 Bản đồ dự báo (`VisualMapActivity`)

- Mở từ Home bằng nút "Xem bản đồ dự báo".
- Nhúng **Windy.com** (`embed.windy.com/embed2.html`) qua WebView với tọa độ hiện tại, overlay mây, zoom 8.

### 2.7 Trợ lý AI (`AIActivity`)

- Gọi lại `WeatherRepository` để lấy dữ liệu thời tiết hiện tại làm ngữ cảnh cho AI.
- Trong lúc tải: spinner, khóa input.
- Sau khi tải xong và **không có lịch sử**: hiện lời chào + 3 câu hỏi gợi ý.
- Sau khi tải xong và **có lịch sử hợp lệ**: hiện lại lịch sử chat, ẩn lời chào/gợi ý.
- Gửi tin → `WeatherChatRepository` gọi OpenAI Chat Completions.
- Nút **new chat** (góc trên phải) → xóa lịch sử, reset về màn chào.
- **Lịch sử chat**: lưu vào `SharedPreferences` (`chat_history`) khi `onStop`, tải lại khi `onCreate` nếu chưa quá **20 phút** (so sánh với `System.currentTimeMillis()`). Hết hạn hoặc bấm new chat → tự xóa.

---

## 3. Lưu trữ dữ liệu (SharedPreferences)

### 3.1 `WeatherPreferences` — file `weather_forcast_prefs`

| Nhóm | Key | Mô tả |
|------|-----|-------|
| Vị trí hiện tại | `current_lat`, `current_lon`, `current_name` | Tọa độ + tên đang hiển thị trên Home |
| Danh sách địa điểm | `locations_json` | Gson `List<SavedLocation>`, tối đa 20 |
| Cache body API | `cache_body_<key>` | JSON response WeatherAPI |
| Thời điểm cache | `cache_time_<key>` | Millisecond |
| Throttle | `api_last_<key>` | Tránh gọi API quá dày |

**Quy tắc**: mọi đọc/ghi liên quan đến vị trí, danh sách, cache đều qua `WeatherPreferences`. Không `getSharedPreferences` rải rác.

### 3.2 `ChatHistoryStore` — file `chat_history`

| Key | Nội dung |
|-----|----------|
| `saved_at` | Timestamp lúc lưu (ms) |
| `chat_items` | Gson `List<ChatItem>` — tin nhắn hiển thị |
| `ai_history` | Gson `List<OpenAiMessage>` — context gửi lên AI |

Hết hạn sau **20 phút** (`EXPIRY_MS = 20 * 60 * 1000L`). Tự xóa khi load nếu hết hạn.

---

## 4. Truyền dữ liệu giữa các màn — `LocationContract`

- Constants: `EXTRA_LAT`, `EXTRA_LON`, `EXTRA_DISPLAY_NAME`, `EXTRA_FLOW_MODE`.
- Values: `FLOW_ONBOARDING`, `FLOW_MANAGEMENT`.
- Helpers: `putLocation`, `readLat`, `readLon`, `readDisplayName`.
- `HomeActivity.EXTRA_*` là alias cùng key với `LocationContract`.

---

## 5. API WeatherAPI.com

- **Base URL:** `https://api.weatherapi.com/v1/`
- **Key:** `WEATHERAPI_KEY` trong `local.properties` → `BuildConfig.WEATHERAPI_KEY`, OkHttp Interceptor gắn tự động vào mọi request.
- **Free tier:** 1.000.000 calls/tháng — đủ cho app dev/personal.
- **Endpoint chính:**
  - `GET forecast.json` — dùng cho cả Home lẫn AI context (`WeatherRepository.fetchForecastForHome`), trả về `ForecastResponse` (current + forecastday + hour).
  - `GET search.json` — tìm kiếm địa điểm trong `SearchActivity`.
- **Icon:** `condition.icon` (URL `//cdn.weatherapi.com/...`) + `WeatherApiIcons.url(icon, isDay, sizePx)` → Glide load. `SIZE_LIST = 128`, `SIZE_HERO = 256`.
- **Parse:** Retrofit + Gson, POJO trong package `model.api`.

---

## 6. Hệ thống giao diện

### 6.1 Màu sắc & theme

- Background Home: `GradientShiftBackgroundView` (gradient động) + `LottieAnimationView` (animation thời tiết overlay).
- Gradient màu sắc theo điều kiện: `WeatherConditionTheme.resolve(conditionCode, isDaytime)` → `Colors(top, mid, bottom)`.
- Animation Lottie: `WeatherConditionTheme.resolveAnimation` → `SUNNY/CLOUDY/RAINY/STORM/NIGHT` → file raw `.json`.
- Glass cards: `glass_surface` (`#22FFFFFF`), `glass_stroke` (`#40FFFFFF`) — frosted glass trên nền tối.
- Text: `text_primary_on_dark` (trắng), `text_secondary_on_dark` (trắng mờ).

### 6.2 Icon

- **Navigation/UI**: bộ icon Lucide-style, stroke width 2, round caps/joins — vector drawable `ic_back_24`, `ic_search_24`, `ic_add_24`, `ic_settings_24`, `ic_map_24`, `ic_send_24`, `ic_ai`, `ic_new_chat`.
- **Thời tiết**: icon từ WeatherAPI CDN load bằng Glide — dùng nhất quán trên cả Home hero (`SIZE_HERO`) lẫn danh sách vị trí (`SIZE_LIST`).
- **Chỉ số metric**: `ic_metric_uv/humidity/feels/wind/sun/pressure`.

### 6.3 Item danh sách vị trí

- Background gradient động theo điều kiện thời tiết: `SavedLocationsAdapter.applyWeatherGradient` — `GradientDrawable.Orientation.TL_BR`, corner radius 18dp.
- Condition code 113 → sunny (cyan→navy); 116–143 → cloudy (gray-blue); ≥176 → rainy (dark indigo).

---

## 7. Biểu đồ 24h (MPAndroidChart)

- `ChartSamples.styleHourlyChart` — cấu hình tĩnh (trục, cảm ứng, legend).
- Dữ liệu thật: `HomeActivity.updateHourlyChart(entries, labels)` — X là sequential index (0,1,2,...), labels là giờ thực từ `HourItemDto.getTime()` (substring 11–16 của chuỗi `"yyyy-MM-dd HH:mm"`), lấy mỗi 3 giờ.
- Formatter set tại thời điểm `updateHourlyChart` để tránh glitch khi scroll.
- `setVisibleXRangeMaximum(4f)` — hiện 4 điểm, kéo ngang để xem thêm.
- Y-axis: `setTextColor(Color.WHITE)`, không disable label. Dataset: `setDrawValues(false)` — không in số trên line.
