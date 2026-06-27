# CẤU TRÚC PROJECT — Hướng dẫn kỹ thuật

Tài liệu mô tả **cấu trúc thư mục**, **vai trò từng file/package**, **thư viện**, và **cách dùng API** trong app. Cập nhật theo mã nguồn hiện tại (Android, Java, View Binding).

---

## 1. Cấu trúc thư mục gốc

| Đường dẫn | Vai trò |
|-----------|---------|
| `settings.gradle.kts` | Khai báo module `:app`, tên project, kho Maven (Google, Maven Central, JitPack). |
| `gradle/libs.versions.toml` | **Catalog phiên bản thư viện**: tên artifact, version. `app/build.gradle.kts` tham chiếu qua `libs.*`. |
| `app/build.gradle.kts` | Module app: `compileSdk`, `minSdk`, View Binding, BuildConfig, đọc key từ `local.properties`. |
| `local.properties` | `WEATHERAPI_KEY=...` và `OPENAI_API_KEY=...` — Gradle đưa vào `BuildConfig`. Không commit. |
| `docs/` | Tài liệu kỹ thuật: `PROJECT_OVERVIEW.md`, `PROJECT_GUIDE.md`, `GIT_WORKFLOW.md`, `PROJECT_TEAM.md`, `bugs.md`. |

---

## 2. Package Java — `com.example.weatherforcastapp`

### 2.1 Activity gốc

| File | Vai trò |
|------|---------|
| `MainActivity.java` | Launcher: kiểm tra `WeatherPreferences.hasCurrentLocation()`. Có → Home. Chưa có → dialog GPS → xin quyền → `LocationHelper.fetchCurrent` → `LocationNameResolver` lấy tên → `setCurrentLocation` + `addOrUpdateLocation` → Home. Từ chối → `SearchActivity.startOnboarding`. |
| `HomeActivity.java` | Trang chủ. Gọi `WeatherRepository.fetchForecastForHome`. `bindForecastToHome`: cập nhật UI (nhiệt độ, icon hero qua Glide+CDN, điều kiện, AQI, biểu đồ 24h, dự báo 3 ngày, 6 chỉ số metric). `applyWeatherTheme` → gradient + Lottie. `applyHeroVisibility` → fade hero + tên theo scroll. `onDestroy` → `weatherRepo.cancel()`. |
| `SearchActivity.java` | Tìm kiếm: mode `FLOW_ONBOARDING` / `FLOW_MANAGEMENT`. Input full-width, không preset. Nút GPS. Kết quả qua `SearchResultAdapter`. Chọn → `PreviewActivity` + `finish()`. |
| `PreviewActivity.java` | Xem trước địa điểm. Nhận `LocationContract` extras. FAB: onboarding → Home + xóa stack; quản lý → `addOrUpdateLocation` + `finish()`. |
| `LocationManagementActivity.java` | Quản lý danh sách (tối đa 20). Nút search compact → `SearchActivity`. Long-press → multi-select → xóa. Tap → `HomeActivity.startClearTop`. `SavedLocationsAdapter` với gradient + icon CDN. |
| `FiveDayForecastActivity.java` | Chi tiết dự báo 3 ngày. Nhận tọa độ qua `LocationContract`. |
| `VisualMapActivity.java` | Bản đồ Windy.com nhúng qua WebView (`embed.windy.com/embed2.html`, overlay clouds, zoom 8). |
| `AIActivity.java` | Trợ lý AI. Load weather context → `WeatherContextBuilder`. Chat qua `WeatherChatRepository` (OpenAI). Lịch sử lưu/load `ChatHistoryStore` (20 phút). Nút new chat: xóa lịch sử, reset UI. `onStop` → save nếu có tin nhắn. |

### 2.2 Package `api/`

| File | Vai trò |
|------|---------|
| `WeatherApiClient.java` | Singleton Retrofit. Base URL `https://api.weatherapi.com/v1/`. OkHttp Interceptor gắn query `key=BuildConfig.WEATHERAPI_KEY`. Gson converter. |
| `WeatherApiService.java` | Interface Retrofit: `getForecast(q, days, aqi, lang)` → `Call<ForecastResponse>`; `searchLocations(q)` → `Call<List<LocationDto>>`. |
| `WeatherApiQuery.java` | `latLon(lat, lon)` → chuỗi `q` dạng `"lat,lon"`. |
| `WeatherApiIcons.java` | `url(iconFromApi, day, sizePx)` → URL đầy đủ CDN. `SIZE_LIST=128`, `SIZE_HERO=256`. Xử lý cả URL `//cdn...` lẫn icon ID dạng số. |

### 2.3 Package `ai/`

| File | Vai trò |
|------|---------|
| `WeatherChatRepository.java` | Gọi OpenAI Chat Completions API (`BuildConfig.OPENAI_API_KEY`). `send(context, history, userText, listener)` → `onReply` / `onError`. `cancel()`. |
| `WeatherContextBuilder.java` | Build system prompt từ `ForecastResponse` (nhiệt độ, điều kiện, độ ẩm, gió, AQI, dự báo 3 ngày). |
| `ChatHistoryStore.java` | Lưu/load lịch sử chat vào `SharedPreferences` file `chat_history`. Key: `saved_at` (timestamp ms), `chat_items` (Gson `List<ChatItem>`), `ai_history` (Gson `List<OpenAiMessage>`). Hết hạn sau 20 phút (`EXPIRY_MS`). `save`, `hasValid`, `loadChatItems`, `loadHistory`, `clear`. |
| `ai/model/OpenAiMessage.java` | DTO tin nhắn AI: `role` (system/user/assistant), `content`. Factory methods `system`, `user`, `assistant`. |

### 2.4 Package `location/`

| File | Vai trò |
|------|---------|
| `LocationContract.java` | Hợp đồng Intent: constants `EXTRA_LAT`, `EXTRA_LON`, `EXTRA_DISPLAY_NAME`, `EXTRA_FLOW_MODE`; values `FLOW_ONBOARDING`, `FLOW_MANAGEMENT`; helpers `putLocation`, `readLat/Lon/DisplayName`. |

### 2.5 Package `prefs/`

| File | Vai trò |
|------|---------|
| `WeatherPreferences.java` | SharedPreferences file `weather_forcast_prefs`. Nhóm: vị trí hiện tại (`current_lat/lon/name`), danh sách JSON (`locations_json`, Gson `List<SavedLocation>`, tối đa 20), cache API (`cache_body/time_<key>`), throttle (`api_last_<key>`). Singleton `get(context)`. |

### 2.6 Package `model/`

| File | Vai trò |
|------|---------|
| `SavedLocation.java` | Địa điểm đã lưu: tên, lat, lon, iconCode cache, nhiệt độ/điều kiện/high-low/humidity cache. `buildId(lat,lon)`. |

### 2.7 Package `model/api/`

POJO Gson map JSON WeatherAPI:

| File | Trường chính |
|------|-------------|
| `ForecastResponse` | `location`, `current`, `forecast` |
| `CurrentDto` | `tempC`, `feelslikeC`, `humidity`, `windKph`, `pressureMb`, `uv`, `isDay`, `condition`, `airQuality` |
| `ConditionDto` | `text`, `icon` (URL `//cdn...`), `code` |
| `ForecastBucketDto` | `forecastday: List<ApiForecastDayDto>` |
| `ApiForecastDayDto` | `date`, `day: DayAggregateDto`, `astro`, `hour: List<HourItemDto>` |
| `HourItemDto` | `time` (`"yyyy-MM-dd HH:mm"`), `tempC`, `condition`, `isDay` |
| `LocationDto` | `name`, `region`, `country`, `lat`, `lon` |

### 2.8 Package `search/`

| File | Vai trò |
|------|---------|
| `SearchResultAdapter.java` | RecyclerView adapter kết quả tìm kiếm. |
| `LocationLabelFormatter.java` | Format `LocationDto` → tên hiển thị và subtitle (`region • country`). |

### 2.9 Package `data/`

| File | Vai trò |
|------|---------|
| `WeatherRepository.java` | Điểm gọi API tập trung cho Home + AI. `fetchForecastForHome(lat, lon, lang, listener)` — enqueue `getForecast`, ghi cache, throttle. `cancel()` hủy Call đang chờ. |

### 2.10 Package `ui/`

| File | Vai trò |
|------|---------|
| `SavedLocationsAdapter.java` | Item địa điểm đã lưu: gradient nền theo condition code (`applyWeatherGradient`), Glide load icon CDN, click/long-press/selection. |
| `ForecastDayAdapter.java` | Hàng ngày trên Home (nhãn, thấp, cao). `setRows(List<Row>)`. |
| `DailyForecastAdapter.java` | Bảng chi tiết `FiveDayForecastActivity`. |
| `HourlyForecastAdapter.java` | Pill giờ + Glide icon (forecast chi tiết). |
| `AIAdapter.java` | Danh sách 3 câu gợi ý trong `AIActivity`. |
| `ui/chat/ChatAdapter.java` | Bong bóng chat AI: user (phải) / bot (trái). Methods: `add`, `setAll`, `clear`, `isEmpty`, `getItems`, `updateLast`. |
| `ui/chat/ChatItem.java` | DTO một tin nhắn: `text`, `fromUser`. Factory: `user(text)`, `bot(text)`. |
| `ui/widget/GradientShiftBackgroundView.java` | Custom View vẽ gradient 3 màu chuyển động — nền chính Home. `setGradientColors(top, mid, bottom)`. |
| `TodayHourlySectionHelper.java` | RecyclerView ngang theo giờ + BlurView (dùng ở Preview/5day). |
| `Forecast5dSamples.java` | Dữ liệu mẫu bảng 5 ngày. |

### 2.11 Package `util/`

| File | Vai trò |
|------|---------|
| `LocationHelper.java` | Fused Location: `fetchCurrent(ctx, callback)` — `getCurrentLocation` với fallback `getLastLocation`. |
| `LocationNameResolver.java` | Geocoder → tên thành phố từ lat/lon. |
| `ActivityTransitions.java` | `overridePendingTransition` slide vào/ra. `slideIn(activity)`, `slideOut(activity)`. |
| `ChartSamples.java` | Cấu hình tĩnh `LineChart` (`styleHourlyChart`): trục X (vị trí bottom, màu trắng), Y-axis (màu trắng, grid mờ), không legend, touch/drag bật, scale tắt. Formatter và granularity set tại `HomeActivity.updateHourlyChart`. |
| `WeatherConditionTheme.java` | `resolve(code, isDaytime)` → `Colors(top, mid, bottom)` cho gradient. `resolveAnimation(code, isDaytime)` → `AnimationType` (SUNNY/CLOUDY/RAINY/STORM/NIGHT). |
| `WeatherIconMapper.java` | `fromConditionCode(code, isDaytime)` → drawable resource ID (dùng làm placeholder). `fromWeatherApiIcon(iconUrl)` → parse URL lấy ID icon. |

---

## 3. Resource — `res/`

| Thư mục / file | Vai trò |
|----------------|---------|
| `layout/activity_*.xml` | Mỗi Activity một file. View Binding sinh class `*Binding`. |
| `layout/item_saved_location.xml` | Item địa điểm: tên, icon (40dp, Glide), điều kiện, high/low, humidity, nhiệt độ lớn. Background set bằng code (`applyWeatherGradient`). |
| `layout/item_chat_message.xml` | Bong bóng chat: `FrameLayout` + `textBubble`, gravity thay đổi theo `fromUser`. |
| `layout/item_forecast_day_row.xml` | Hàng ngày Home: nhãn + thấp + cao. |
| `xml/scene_home_hero.xml` | MotionScene cho MotionLayout hero Home (fade + translate khi scroll). |
| `values/themes.xml` | Theme Material3. `Theme.WeatherForcastApp.Home` (nền cửa sổ transparent). `Black`. `Forecast`. |
| `values/colors.xml` | `glass_surface (#22FFFFFF)`, `glass_stroke (#40FFFFFF)`, `text_primary_on_dark`, `text_secondary_on_dark`, `chart_line`, `chat_send_button_tint`. |
| `values/strings.xml` | Mọi chuỗi UI (tiếng Việt). |
| `drawable/bg_*.xml` | Nền: `bg_sky` (AI), `bg_glass_location_row`, `bg_chat_bubble_user/bot`, `bg_send_circle`, `rounded_input/box`. |
| `drawable/ic_*.xml` | Icon vector: navigation (`back`, `search`, `add`, `settings`, `map`, `send`, `ai`, `new_chat`), thời tiết (`weather_sunny/cloud/moon/rain/storm/placeholder`), metric (`metric_uv/humidity/feels/wind/sun/pressure`). |
| `raw/` | Animation Lottie: `sunny.json`, `cloudy.json`, `rain.json`, `storm.json`, `night.json`. |
| `anim/` | `slide_in_*`, `slide_out_*` cho `ActivityTransitions`. |

---

## 4. Thư viện

| Thư viện | Khai báo | Dùng ở đâu |
|----------|----------|------------|
| AndroidX AppCompat | `libs.versions.toml` | Mọi Activity |
| Material Components 3 | `libs.versions.toml` | Dialog, CardView, Button, theme |
| ConstraintLayout + MotionLayout | `libs.versions.toml` | Layout, hero animation |
| RecyclerView | `libs.versions.toml` | Home, quản lý, chat, tìm kiếm |
| SwipeRefreshLayout | `libs.versions.toml` | Home |
| CoordinatorLayout | `libs.versions.toml` | `activity_home.xml` |
| View Binding | `app/build.gradle.kts` buildFeatures | Mọi Activity |
| **Retrofit 2 + Gson converter** | `libs.versions.toml` | `WeatherApiClient`, `WeatherRepository`, `WeatherChatRepository` |
| **OkHttp logging** | `libs.versions.toml` | `WeatherApiClient` (debug) |
| **Play Services Location** | `libs.versions.toml` | `LocationHelper` |
| **Glide** | `libs.versions.toml` | `HomeActivity` (hero icon), `SavedLocationsAdapter`, `HourlyForecastAdapter` |
| **MPAndroidChart** | JitPack trong `settings.gradle.kts` | `HomeActivity` (biểu đồ 24h), `FiveDayForecastActivity` |
| **Lottie** | `libs.versions.toml` | `HomeActivity` (animation thời tiết) |
| **Gson** | Transitive từ Retrofit + dùng trực tiếp | `WeatherPreferences`, `ChatHistoryStore` |
| BlurView (Dimezis) | JitPack | `TodayHourlySectionHelper` |
| ViewPager2 | `libs.versions.toml` | Dependency có sẵn, chưa dùng UI hiện tại |

---

## 5. API — cách gọi

### 5.1 WeatherAPI.com

```java
// Từ WeatherRepository (dùng cho Home và AI context):
weatherRepo.fetchForecastForHome(lat, lon, "vi", new WeatherRepository.HomeForecastListener() {
    @Override public void onSuccess(ForecastResponse body) { ... }
    @Override public void onFailure(String message) { ... }
});

// Trực tiếp (ít dùng):
WeatherApiService api = WeatherApiClient.api();
api.getForecast(WeatherApiQuery.latLon(lat, lon), 3, "yes", "vi").enqueue(...);
```

**Key** gắn tự động qua OkHttp Interceptor — không cần truyền vào hàm.

### 5.2 Icon thời tiết

```java
// Home hero (SIZE_HERO = 256px):
String iconUrl = WeatherApiIcons.url(cond.getIcon(), cur.isDaytime(), WeatherApiIcons.SIZE_HERO);
Glide.with(this).load(iconUrl).placeholder(R.drawable.ic_weather_placeholder).into(binding.imageWeatherHero);

// Danh sách vị trí (SIZE_LIST = 128px):
String url = WeatherApiIcons.url(iconCode, true, WeatherApiIcons.SIZE_LIST);
Glide.with(ctx).load(url).placeholder(R.drawable.ic_weather_placeholder).into(holder.binding.iconWeather);
```

### 5.3 Local.properties — key cần thiết

```
WEATHERAPI_KEY=your_weatherapi_key_here
OPENAI_API_KEY=your_openai_key_here
```

---

## 6. File đọc đầu tiên khi vào project

1. `AndroidManifest.xml` — danh sách màn hình, quyền.
2. `MainActivity.java` → `HomeActivity.java`.
3. `location/LocationContract.java` + `prefs/WeatherPreferences.java`.
4. `api/WeatherApiClient.java` + `api/WeatherApiService.java`.
5. `data/WeatherRepository.java`.
6. `util/WeatherConditionTheme.java` — logic màu nền + animation.
7. Layout `activity_home.xml` (cấu trúc chính trang chủ).
