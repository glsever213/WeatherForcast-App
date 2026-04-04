# Dự án dự báo thời tiết — Tài liệu kỹ thuật & luồng

Ứng dụng mobile dự báo thời tiết; **API nguồn: [WeatherAPI.com](https://www.weatherapi.com/docs/)**. **Không dùng database**: lưu vị trí người dùng, danh sách địa điểm, cache/throttle gọi API bằng **SharedPreferences**; **giới hạn 20** địa điểm đã lưu (CRUD trên danh sách).

**Danh sách màn hình (Activity):** `MainActivity`, `HomeActivity`, `SearchActivity`, `LocationManagementActivity`, `PreviewActivity`, `FiveDayForecastActivity`.

**Màn hình dọc:** mọi Activity khai báo `android:screenOrientation="portrait"` trong `AndroidManifest.xml` — giảm recreate do xoay, phù hợp đồ án; vẫn nên hủy request mạng khi destroy (`WeatherRepository.cancel()`).

---

## 1. Luồng nghiệp vụ (theo spec sản phẩm)

### 1.1 Mở app lần đầu / chưa có ngữ cảnh đủ

1. Hiện **popup** hỏi người dùng có cho phép app **truy cập vị trí hiện tại** không (`MainActivity` + `MaterialAlertDialogBuilder`).

2. **Trường hợp 1 — Đồng ý:** xin quyền → lấy **lat, lon** (`LocationHelper` + Fused Location) → lưu làm vị trí hiện tại (`WeatherPreferences.setCurrentLocation`) → vào **`HomeActivity`**. **Gọi API** `forecast.json` qua `WeatherRepository` + `WeatherApiService` (`q=lat,lon`), Gson map vào `ForecastResponse`; cache/throttle trong `WeatherPreferences`.

3. **Trường hợp 2 — Từ chối:** mở **`SearchActivity`** (onboarding) để người dùng **tìm địa điểm** thủ công.  
   - Nếu người dùng bấm **Hủy** (hoặc back tương đương) trên màn Search onboarding: **`finishAffinity()`** → thoát app (đúng spec “out app”).  
   - Ngược lại: chọn thành phố / GPS → có lat/lon → **`PreviewActivity`** → sau khi xác nhận có thể vào Home (onboarding: FAB → `HomeActivity` + xóa stack onboarding).

4. Sau khi đã có **lat/lon** (GPS hoặc chọn từ search/preview), luồng chung: **`HomeActivity`** hiển thị dự báo; kéo xuống xem **chi tiết** trong cùng trang. Nút **dấu +** (và chỗ tương đương “đổi vị trí”) mở **`LocationManagementActivity`**.

### 1.2 Từ Home — quản lý / đổi vị trí

- Góc trên có **dấu +** → **`LocationManagementActivity`** (nền **đen**, chữ sáng).
- Trên màn quản lý: **thanh search** (ô˛) → chạm → **`SearchActivity`** (luồng quản lý).  
  Tìm xong, chọn một địa điểm → **`PreviewActivity`** (xem trước).

### 1.3 Trang xem trước (`PreviewActivity`)

- **Giao diện:** khối **“Hôm nay”** (theo giờ) — `widget_today_hourly_block` + `BlurTarget`, **cùng kiểu** như phần trên màn **Dự báo 5 ngày**; **dưới** giữ FAB **+** và **“Thêm vào trang home”** (không dùng bảng 5 cột trên Preview).
- **Luồng quản lý:** bấm thêm → lưu vào danh sách (`addOrUpdateLocation`) → **quay lại** `LocationManagementActivity` (đóng Preview; Search đã `finish` khi mở Preview nên stack thường là: Quản lý → Preview).
- **Luồng onboarding (lần đầu từ chối GPS):** FAB → lưu current + (tuỳ) list → **`HomeActivity`** với `CLEAR_TASK` / `finishAffinity` theo code hiện tại.

### 1.4 Quản lý danh sách thành phố (`LocationManagementActivity`)

- **Giữ** (long-press) một item: vào chế độ **chọn nhiều** → tick từng ô → **Xóa đã chọn** / **Xóa tất cả** / **Xong**.
- **Bấm** vào một địa điểm (khi không ở chế độ chọn): **“dùng vị trí này”** → truyền **lat/lon** về Home (`HomeActivity.startClearTop` + `LocationContract`) → **tự động quay Home** → `HomeActivity` gọi lại API qua `WeatherRepository`.

### 1.5 Màn dự báo 5 ngày chi tiết (`FiveDayForecastActivity`)

- Mở từ Home (nút chi tiết); vẫn là một **trang riêng** trong project, truyền tọa độ qua `LocationContract`.

---

## 2. Chuẩn truyền lat / lon — `LocationContract`

- `EXTRA_LAT`, `EXTRA_LON`, `EXTRA_DISPLAY_NAME` — dùng `putLocation` / `readLat` / `readLon` / `readDisplayName`.
- `EXTRA_FLOW_MODE`: `FLOW_ONBOARDING` vs `FLOW_MANAGEMENT` để `PreviewActivity` biết FAB đi về Home hay chỉ thêm list.
- `HomeActivity.EXTRA_*` là **alias** cùng key với `LocationContract`.

---

## 3. SharedPreferences — `WeatherPreferences`

| Vùng | Mục đích |
|------|----------|
| **Vị trí hiện tại** | `current_lat`, `current_lon`, `current_name` |
| **CRUD danh sách (tối đa 20)** | JSON `locations_json` (`SavedLocation`), thêm/sửa/xóa/xóa hết |
| **Cache API** | `cache_body_*`, `cache_time_*` |
| **Giãn cách gọi API** | `api_last_*`, `canCallApi`, `markApiCalled` |

---

## 4. API WeatherAPI.com trong project

- **Base URL:** `https://api.weatherapi.com/v1/` (`WeatherApiClient`). Tài liệu: [WeatherAPI Docs](https://www.weatherapi.com/docs/).
- **Khóa API:** `WEATHERAPI_KEY` trong `local.properties` → `BuildConfig.WEATHERAPI_KEY`; OkHttp interceptor gắn query **`key`** mọi request.
- **Interface:** `WeatherApiService` — `GET current.json` (thời tiết hiện tại), `GET forecast.json` (dự báo, tham số `days` 1–14 theo gói). Tham số **`q`**: theo docs có thể là **`lat,lon`** (vd `21.0285,105.8542`), tên thành phố, v.v. — helper `WeatherApiQuery.latLon(lat, lon)`.
- **Icon:** CDN `cdn.weatherapi.com` hoặc trường `condition.icon` trong JSON — helper `WeatherApiIcons` (Glide trong adapter / Home).
- **Parse:** Retrofit + Gson: `WeatherApiService` trả về `Call<CurrentWeatherResponse>` / `Call<ForecastResponse>`; POJO trong `model.api` (`ForecastResponse`, `CurrentDto`, `ConditionDto`, …). **Không** dùng `ResponseBody` + parse tay cho hai endpoint này.
- **Tầng gọi mạng tập trung:** `data/WeatherRepository.java` — `HomeActivity` `enqueue` forecast, **hủy `Call` trong `onDestroy`** để tránh cập nhật UI sau khi Activity đóng; kéo **SwipeRefresh** bỏ qua throttle (`bypassThrottle`).

---

## 5. Công nghệ & thư viện (Gradle)

- AndroidX: AppCompat, Material, ConstraintLayout, RecyclerView, CardView, SwipeRefreshLayout, CoordinatorLayout, **ViewPager2** (giữ dependency — có thể dùng sau; Preview **không** dùng tab nữa).
- **BlurView** (Dimezis, JitPack `version-3.2.0`, package `eightbitlab.com.blurview`) + **BlurTarget** trên màn forecast / layout liên quan.
- **Retrofit 2**, Gson converter, **OkHttp** logging.
- **Play Services Location**.
- **Glide**, **MPAndroidChart**, **Lottie** (nếu có màn dùng).

---

## 6. Animation đã dùng

- **MotionLayout** hero Home (`scene_home_hero.xml`): tiến trình gắn với cuộn `NestedScrollView`; **easeInOut** trên transition; **không** `GONE` hero (tránh co giật layout); tiến trình qua **smoothstep** + khoảng cuộn dài hơn (`HERO_HIDE_DISTANCE_PX`).
- **Chuyển Activity:** `ActivityTransitions.slideIn` / `slideOut`.
- **Scale nhẹ** nút (Home).
- **`DepthPageTransformer`**: class sẵn có cho ViewPager2 — **hiện không gắn Preview** ( một màn Preview).

---

## 7. Cấu trúc package (Java)

- **`api/`** — `WeatherApiClient` (Retrofit singleton, OkHttp + query `key`), `WeatherApiService` (`current.json`, `forecast.json` → POJO), `WeatherApiIcons`, `WeatherApiQuery`.
- **`location/`** — `LocationContract`: chuẩn Intent cho lat/lon/tên/flow.
- **`model/`** — `SavedLocation`: danh sách địa điểm; `buildId` đồng bộ cache.
- **`model.api/`** — DTO Gson cho JSON WeatherAPI: `ForecastResponse`, `CurrentWeatherResponse`, `CurrentDto`, `ConditionDto`, `ApiForecastDayDto`, …
- **`prefs/`** — `WeatherPreferences`: SharedPreferences, CRUD danh sách + current + cache API.
- **`search/`** — `FakeGeocoding`: dữ liệu giả đến khi nối geocoding thật.
- **`data/`** — `PopularCity`, …; **`WeatherRepository`**: gọi `getForecast`, cache Gson string, throttle; Activity khác có thể tái sử dụng.
- **`ui/`** — Adapter (`DailyForecastAdapter`, `ForecastDayAdapter`, …), `TodayHourlySectionHelper`, `Forecast5dSamples`, `DepthPageTransformer`, …
- **`util/`** — `LocationHelper`, `ChartSamples`, `ActivityTransitions`, …
- **Activity gốc** — `Main`, `Home`, `Search`, `Preview`, `LocationManagement`, `FiveDayForecast`.

**Layout dùng chung:**

- `widget_forecast_5d_table.xml` — tiêu đề “Dự báo 5 ngày” + lịch + `RecyclerView` + ghi chú. Chỉ **`FiveDayForecastActivity`**. **`PreviewActivity`** chỉ dùng `widget_today_hourly_block` (giờ).
- `item_daily_forecast_line.xml` — **3 cột weight 1**: ngày (trái) | icon + note (giữa) | nhiệt độ (phải).
- `item_forecast_day_row.xml` — **Home**: chỉ nhãn ngày + thấp + thanh range + cao (**đã bỏ `ImageView` icon**).

---

## 8. API WeatherAPI.com

**Nguồn:** [WeatherAPI.com](https://www.weatherapi.com/docs/) — REST JSON v1.  
Base URL trong `WeatherApiClient`: `https://api.weatherapi.com/v1/`

**Khóa API:**

1. Đăng ký tại [weatherapi.com](https://www.weatherapi.com/) và lấy API key trong tài khoản.
2. Trong `local.properties` (thư mục gốc project, không commit):  
   `WEATHERAPI_KEY=your_key_here`
3. `app/build.gradle.kts` sinh `BuildConfig.WEATHERAPI_KEY`.

**Tham số chính (theo docs):**

- **`key`** — bắt buộc; app gắn tự động qua OkHttp interceptor (không cần thêm vào chữ ký hàm Retrofit).
- **`q`** — bắt buộc: vị trí. Với tọa độ GPS dùng chuỗi **`lat,lon`** (vd `48.8567,2.3508`). Dùng `WeatherApiQuery.latLon(lat, lon)`.
- **`days`** — chỉ `forecast.json`: số ngày dự báo (1–14, tùy gói).
- **`lang`** — tùy chọn (vd `vi`) để `condition.text` theo ngôn ngữ.

**Endpoint trong code:**

- `WeatherApiService.getCurrent(query, lang)` → `GET current.json`
- `WeatherApiService.getForecast(query, days, lang)` → `GET forecast.json`

**Gọi từ code (ví dụ):**

```java
import com.example.weatherforcastapp.model.api.CurrentWeatherResponse;
import com.example.weatherforcastapp.model.api.ForecastResponse;
// ...
WeatherApiService api = WeatherApiClient.api();
String q = WeatherApiQuery.latLon(lat, lon);
Call<CurrentWeatherResponse> current = api.getCurrent(q, "vi");
Call<ForecastResponse> forecast = api.getForecast(q, 5, "vi");
// enqueue → response.body().getCurrent().getTempC(), getForecast().getForecastday(), …
```

Trên Home, ưu tiên dùng **`WeatherRepository.fetchForecastForHome`** (một request `forecast.json` đã có cả `current` + `forecastday`).

**Icon:** trường `condition.icon` (URL relative `//cdn...`) hoặc `condition.code` — `WeatherApiIcons.url(...)` tạo URL cho Glide; mẫu trong `Forecast5dSamples` / `TodayHourlySectionHelper` dùng mã số WeatherAPI.

---

## 9. SharedPreferences — chỗ nào dùng, làm CRUD / cache thế nào

Lớp tập trung: **`WeatherPreferences`** (`prefs/WeatherPreferences.java`), file XML: `weather_forcast_prefs`.

| Nhóm | Key / hành vi | Ghi chú |
|------|----------------|---------|
| Vị trí đang xem | `current_lat`, `current_lon`, `current_name` | `setCurrentLocation`, `getCurrent*`, `clearCurrentLocation`, `hasCurrentLocation` |
| Danh sách địa điểm (tối đa **20**) | `locations_json` (Gson `List<SavedLocation>`) | `getSavedLocations`, `addOrUpdateLocation`, `removeLocation`, `removeLocations`, `removeAllLocations` |
| Cache body API | `cache_body_<key>` | `putApiResponseCache`, `getCachedApiBody` — `key` nên dùng `cacheKeyForCoords(lat,lon)` |
| Thời điểm cache | `cache_time_<key>` | `getCachedApiTime` |
| Throttle gọi API | `api_last_<key>` | `canCallApi`, `markApiCalled` |

**Quy tắc:** Bất kỳ chỗ nào cần “địa điểm user đã chọn” hoặc “đừng gọi API quá sớm” đều nên đi qua lớp này thay vì tự `getSharedPreferences` rải rác.

---

## 10. Luồng dữ liệu lat/lon (tóm tắt)

1. GPS / Search / Preview FAB → cập nhật `WeatherPreferences.setCurrentLocation` và/hoặc `addOrUpdateLocation`.
2. Mở màn khác cần tọa độ → `Intent` kèm `LocationContract.putLocation`, hoặc đọc từ prefs sau khi Home đã lưu.
3. Gọi API → luôn dùng cùng một cặp `lat`/`lon` với màn đang hiển thị (tránh lệch Preview vs Home).


---