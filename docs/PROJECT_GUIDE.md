# CẤU TRÚC PROJECT 

Tài liệu này mô tả **cấu trúc thư mục**, **vai trò từng file/package chính**, **thư viện khai báo ở đâu**, **cách dùng**, và **API mạng** trong app. Nội dung bám theo **mã nguồn hiện tại** (Android, Java, View Binding).

---

## 1. Cấu trúc thư mục gốc (ngoài `app/`)

| Đường dẫn | Vai trò |
|-----------|---------|
| `settings.gradle.kts` | Khai báo module `:app`, tên project, kho Maven (Google, Maven Central, **JitPack** cho BlurView, MPAndroidChart). |
| `gradle/libs.versions.toml` | **Catalog phiên bản thư viện**: tên artifact, version. Đây là nơi **định nghĩa tên** mà `app/build.gradle.kts` tham chiếu qua `libs.*`. |
| `app/build.gradle.kts` | **Module ứng dụng**: `compileSdk`, `minSdk`, **View Binding**, **BuildConfig**, đọc `WEATHERAPI_KEY` từ `local.properties`, khối `dependencies { implementation(libs...) }`. |
| `local.properties` (máy dev, thường không commit) | Chứa `WEATHERAPI_KEY=...` — Gradle đưa vào `BuildConfig.WEATHERAPI_KEY`. |
| `docs/` | Tài liệu: overview, doc2, và các file hướng dẫn bổ sung. |

---

## 2. Cấu trúc `app/src/main` — nơi code & resource chạy trên máy

### 2.1 `AndroidManifest.xml`

- **Quyền:** `INTERNET`, `ACCESS_COARSE_LOCATION`, `ACCESS_FINE_LOCATION`.
- **Launcher:** `MainActivity`.
- **Các Activity:** `HomeActivity` (`singleTop`), `SearchActivity`, `LocationManagementActivity`, `PreviewActivity`, `FiveDayForecastActivity` — theme từ `themes.xml` (`Home`, `Black`, `Forecast`).
- **Hướng màn hình:** cả sáu Activity dùng `android:screenOrientation="portrait"` (không xoay ngang).

### 2.2 Java — package `com.example.weatherforcastapp`

| File / thư mục | Vai trò cụ thể |
|----------------|----------------|
| **`MainActivity.java`** | Màn khởi động: nếu `WeatherPreferences.hasCurrentLocation()` → `HomeActivity.startClearTask` và `finish()`. Nếu chưa có vị trí → `MaterialAlertDialogBuilder` hỏi đồng ý/từ chối GPS; đồng ý → xin quyền → `LocationHelper.fetchCurrent` → `setCurrentLocation` → Home; từ chối hoặc từ chối quyền → `SearchActivity.startOnboarding` + `finish()`. |
| **`HomeActivity.java`** | Trang chủ: nhận `Intent` / prefs; gọi **`WeatherRepository.fetchForecastForHome`** (Retrofit Gson → `ForecastResponse`); `onDestroy` → `weatherRepo.cancel()`; `bindForecastToHome` cập nhật nhiệt độ, điều kiện, icon hero, `ForecastDayAdapter`; cache + throttle qua `WeatherPreferences`; **SwipeRefresh** làm mới (bỏ throttle). `ChartSamples` vẫn mẫu; **MotionLayout** hero theo scroll. |
| **`SearchActivity.java`** | Tìm kiếm: mode `MODE_ONBOARDING` / `MODE_MANAGEMENT` qua extra `EXTRA_MODE`. Chip Hà Nội / TP.HCM / “Định vị”; nhập text + Enter gọi `FakeGeocoding.search`. Chọn xong → `PreviewActivity.start` + `LocationContract.putFlowMode` (onboarding vs quản lý) và `finish()`. **Hủy onboarding** → `finishAffinity()` thoát app; mode quản lý → `finish()` + slide out. |
| **`PreviewActivity.java`** | Xem trước: đọc lat/lon/tên + `flowMode` từ Intent; `widget_today_hourly_block` qua `TodayHourlySectionHelper`; FAB `fabAddToHome`: onboarding → `setCurrentLocation` + `addOrUpdateLocation` + `HomeActivity.startClearTask` + `finishAffinity()`; quản lý → chỉ `addOrUpdateLocation` (tối đa 20) rồi `finish()`. |
| **`LocationManagementActivity.java`** | Quản lý: `RecyclerView` + `SavedLocationsAdapter`; `cardSearchEntry` → `SearchActivity.startForManagement`; long-press → chế độ chọn + thanh xóa; xóa chọn / xóa hết; tap dòng (không selection) → `setCurrentLocation` + `HomeActivity.startClearTop` + `finish()`. |
| **`FiveDayForecastActivity.java`** | Màn 5 ngày: khối giờ (`TodayHourlySectionHelper`) + bảng `widget_forecast_5d_table` với `DailyForecastAdapter(Forecast5dSamples.defaultSlots())` — dữ liệu mẫu. |

#### Package con

| Package | File | Vai trò |
|---------|------|---------|
| **`api/`** | `WeatherApiClient.java` | Singleton Retrofit, base URL `https://api.weatherapi.com/v1/`, OkHttp **Interceptor** gắn query `key` = `BuildConfig.WEATHERAPI_KEY`, Gson converter, logging BASIC. |
| | `WeatherApiService.java` | Interface Retrofit: `getCurrent` → `Call<CurrentWeatherResponse>`, `getForecast` → `Call<ForecastResponse>` (Gson tự map). |
| | `WeatherApiQuery.java` | `latLon(lat, lon)` → chuỗi `q` dạng `lat,lon`. |
| | `WeatherApiIcons.java` | Ghép URL icon CDN WeatherAPI hoặc dùng URL từ field `condition.icon`. |
| **`location/`** | `LocationContract.java` | Hợp đồng Intent: `EXTRA_LAT`, `EXTRA_LON`, `EXTRA_DISPLAY_NAME`, `EXTRA_FLOW_MODE`, `FLOW_ONBOARDING` / `FLOW_MANAGEMENT`; `putLocation`, `readLat`/`readLon`/… |
| **`prefs/`** | `WeatherPreferences.java` | **SharedPreferences** file `weather_forcast_prefs`: vị trí hiện tại (`current_*`), JSON danh sách `locations_json` (Gson `List<SavedLocation>`, tối đa 20), cache body/time API, throttle `api_last_*`. |
| **`model/`** | `SavedLocation.java` | Một địa điểm đã lưu; `buildId(lat,lon)` dùng cho id và `cacheKeyForCoords`. |
| **`model.api/`** | `ForecastResponse`, `CurrentWeatherResponse`, `CurrentDto`, `ConditionDto`, … | POJO Gson khớp JSON WeatherAPI (current / forecastday). |
| **`search/`** | `FakeGeocoding.java` | Map text cố định (Hà Nội, HCM, …) + fallback tọa độ; **thay thế** khi nối API search thật. |
| **`data/`** | `PopularCity.java`, `PopularCities.java` | Model / dữ liệu thành phố gợi ý (phục vụ UI/search). |
| | `WeatherRepository.java` | Một điểm gọi `getForecast` cho Home: `enqueue`, ghi cache, throttle; `cancel()` hủy `Call` đang chờ. |
| **`ui/`** | `SavedLocationsAdapter.java` | Item danh sách đã lưu: click, long-press selection, checkbox. |
| | `ForecastDayAdapter.java` | Hàng ngày trên Home; constructor có dữ liệu mẫu, API cập nhật qua `setRows`. |
| | `DailyForecastAdapter.java` | Bảng 5 ngày chi tiết (layout 3 cột). |
| | `HourlyForecastAdapter.java` | Pill giờ + Glide load icon WeatherAPI. |
| | `TodayHourlySectionHelper.java` | Gắn RecyclerView ngang mẫu + **BlurView** với `BlurTarget` id `blurTargetRoot`. |
| | `Forecast5dSamples.java` | Dữ liệu mẫu cho bảng 5 ngày. |
| | `DepthPageTransformer.java` | Transformer ViewPager2 (sẵn có, có thể chưa gắn màn hình). |
| | **`ui/widget/`** | `GradientShiftBackgroundView.java` | Custom View vẽ **gradient xanh** chuyển động nhẹ — dùng làm nền Home trong `activity_home.xml`. |
| **`util/`** | `LocationHelper.java` | **Play Services Location**: `FusedLocationProviderClient`, `getCurrentLocation` / fallback `getLastLocation`. |
| | `ActivityTransitions.java` | Gọi `overridePendingTransition` cho slide vào/ra. |
| | `ChartSamples.java` | Cấu hình mẫu cho `MPAndroidChart` trên Home. |

### 2.3 Resource — `res/`

| Thư mục / file | Vai trò |
|----------------|---------|
| `layout/` | Mỗi Activity có `activity_*.xml`; include `widget_today_hourly_block.xml`, `widget_forecast_5d_table.xml`; item `item_*.xml`. **View Binding** sinh class `*Binding` trong `databinding`. |
| `xml/scene_home_hero.xml` | **MotionScene** cho `MotionLayout` hero ở Home. |
| `values/themes.xml` | Theme Material3, `Theme.WeatherForcastApp.Home` (nền cửa sổ trong suốt cho gradient phía sau), `Black`, `Forecast`. |
| `values/strings.xml`, `colors.xml`, `dimens.xml` | Chuỗi, màu, kích thước. |
| `values/type_weather.xml` | Kiểu chữ tùy chỉnh. |
| `drawable/` | Icon vector (`ic_add_24`, `ic_search_24`, …), nền gradient (`bg_gradient_*`), card. |
| `anim/` | `slide_in_*`, `slide_out_*` dùng với `ActivityTransitions` / theme `AnimActivity` nếu gắn. |
| `mipmap/` | Launcher icon. |

---

## 3. Thư viện — khai báo ở đâu, dùng ở đâu, hoạt động ra sao

### 3.1 Nơi khai báo phiên bản

- **`gradle/libs.versions.toml`**: định nghĩa `[versions]` và `[libraries]` (AndroidX, Material, Retrofit, OkHttp, Glide, Play Services Location, MPAndroidChart, Lottie, BlurView JitPack, …).
- **`app/build.gradle.kts`**: `implementation(libs.androidx.appcompat)` v.v. — **đây là chỗ “import dependency” vào module**.

### 3.2 Bảng thư viện chính và cách project đang dùng

| Thư viện | Vai trò kỹ thuật | Dùng trong code / layout (ví dụ) |
|-----------|------------------|-----------------------------------|
| **AndroidX AppCompat** | Activity tương thích, `AppCompatActivity`. | Mọi Activity. |
| **Material Components** | `MaterialAlertDialogBuilder`, `MaterialCardView`, theme Material3. | `MainActivity` dialog; layout có `MaterialCardView`. |
| **ConstraintLayout** | Bố cục linh hoạt. | Các `activity_*.xml`. |
| **RecyclerView** | Danh sách cuộn. | Home, quản lý, bảng 5 ngày, giờ ngang. |
| **CardView** | Bo góc thẻ. | Item layout. |
| **SwipeRefreshLayout** | Kéo làm mới. | `HomeActivity` — gọi lại API (`bypassThrottle`). |
| **CoordinatorLayout** | Điều phối behavior (cuộn, app bar). | `activity_home.xml`. |
| **ViewPager2** | Trang trượt ngang. | Dependency có; Preview không còn tab ViewPager theo doc cũ. |
| **View Binding** | `buildFeatures { viewBinding = true }` — tránh `findViewById`. | `ActivityHomeBinding`, `ActivitySearchBinding`, … |
| **Retrofit 2 + Gson converter** | HTTP + map JSON → POJO. | `WeatherApiClient`, `WeatherApiService`, `model.api`, `WeatherRepository`. |
| **OkHttp logging** | Log request/response (debug). | `WeatherApiClient.buildRetrofit()`. |
| **Play Services Location** | Fused location API. | `LocationHelper` — `LocationServices.getFusedLocationProviderClient`. |
| **Glide** | Tải ảnh (icon thời tiết). | `HomeActivity`, `HourlyForecastAdapter`. |
| **MPAndroidChart** | Biểu đồ. | `HomeActivity` + `ChartSamples`. |
| **Lottie** | Animation JSON. | Đã thêm dependency; cần layout/code nếu dùng. |
| **BlurView (Dimezis, JitPack)** | Làm mờ nền phía sau khối “kính”. | `TodayHourlySectionHelper.setupBlur` + `BlurTarget` trong layout forecast/preview. |
| **Gson** | (Transitive từ Retrofit converter + dùng trực tiếp) Serialize `List<SavedLocation>` trong prefs. | `WeatherPreferences`. |

---

## 4. API mạng — loại gì, ở đâu, cách gọi

### 4.1 Dịch vụ thực tế trong code: WeatherAPI.com

- **Base URL:** `https://api.weatherapi.com/v1/` (`WeatherApiClient.java`).
- **Khóa:** biến `BuildConfig.WEATHERAPI_KEY`, nguồn từ `local.properties` key `WEATHERAPI_KEY` (`app/build.gradle.kts` dòng đọc Properties và `buildConfigField`).
- **Cách gắn key:** không truyền trong `@Query` của interface; **OkHttp Interceptor** thêm `key` vào mọi request (`WeatherApiClient`).

### 4.2 Endpoint trong `WeatherApiService.java`

| Method | HTTP | Tham số chính | Ý nghĩa |
|--------|------|----------------|---------|
| `getCurrent(q, lang)` | `GET current.json` | `q` = vị trí (tên hoặc `lat,lon`), `lang` ví dụ `vi` | Thời tiết hiện tại. |
| `getForecast(q, days, lang)` | `GET forecast.json` | `days` 1–14 (tùy gói) | Dự báo nhiều ngày + giờ trong `forecastday`. |

**Tham số `q` cho GPS:** dùng `WeatherApiQuery.latLon(lat, lon)` → ví dụ `21.0285,105.8542`.

### 4.3 Parse JSON (Gson + POJO)

- `WeatherApiService` trả về **`Call<CurrentWeatherResponse>`** và **`Call<ForecastResponse>`** — Retrofit Gson gán trực tiếp vào object; tránh `ResponseBody` + `string()` + parse tay (dễ lệch kiểu / null).
- Có thể bổ sung field vào DTO trong `model.api` khi cần thêm trường từ API (Gson bỏ qua key không khai báo).

### 4.4 Icon thời tiết từ API

- `WeatherApiIcons.url(...)` / `urlDayIconCode` — CDN `cdn.weatherapi.com` hoặc URL đầy đủ từ JSON.

---

## 5. SharedPreferences — lưu gì, code ở đâu

- **Lớp duy nhất nên dùng:** `WeatherPreferences.java`.
- **Tên file XML:** `weather_forcast_prefs`.
- **Nội dung:** vị trí đang xem, danh sách tối đa 20, cache JSON theo key tọa độ, thời điểm gọi API lần trước (throttle).

---

## 6. Luồng người dùng (tóm tắt — khớp code + doc overview)

1. **Vào app:** `MainActivity` — có vị trí đã lưu → Home; không → dialog GPS.  
2. **Đồng ý GPS:** quyền → `LocationHelper` → lưu prefs → Home.  
3. **Từ chối / không cấp quyền:** `SearchActivity` onboarding; **Hủy** → `finishAffinity()`.  
4. **Chọn địa điểm (search/chip):** → `PreviewActivity` với `FLOW_ONBOARDING` hoặc `FLOW_MANAGEMENT`.  
5. **FAB Preview:** onboarding → Home + lưu; quản lý → thêm list + đóng Preview.  
6. **Home:** `+` → `LocationManagementActivity`; chọn item đã lưu → cập nhật current + `HomeActivity` clear top.  
7. **Chi tiết 5 ngày:** từ Home → `FiveDayForecastActivity`.

---

## 7. File “điểm vào” khi đọc code lần đầu

1. `AndroidManifest.xml` — danh sách màn hình.  
2. `MainActivity.java` → `HomeActivity.java`.  
3. `location/LocationContract.java` + `prefs/WeatherPreferences.java`.  
4. `api/WeatherApiClient.java` + `WeatherApiService.java`.  
5. Layout `activity_home.xml` (nền `GradientShiftBackgroundView` + Motion hero).

---
