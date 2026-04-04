# Phân việc nhóm 4 người

**Nguyên tắc tránh đá nhau khi merge**

1. **Một hợp đồng dữ liệu cố định:** mọi tọa độ qua `location/LocationContract.java` (không tự thêm `putExtra("lat")` kiểu khác).  
2. **SharedPreferences:** chỉ qua `prefs/WeatherPreferences.java`. Nếu cần **hàm mới** (ví dụ flag “đã hỏi GPS”, key nền theo weather): **chủ nhóm** làm **một PR nhỏ “prefs + contract”** merge trước, hoặc chia rõ: mỗi người chỉ **thêm method mới** ở cuối class, không sửa logic cũ của người khác trong cùng hàm.  
3. **Tránh hai người sửa cùng một file lớn cùng lúc:** dưới đây ghi rõ **file owner**; nếu bắt buộc chạm chung (ví dụ `HomeActivity`), dùng **interface / helper class mới** trong package riêng để diff tách dòng.  
4. **Tài nguyên drawable:** ưu tiên **thêm file icon mới** hoặc sửa theo **danh sách màn hình được giao** để giảm conflict `merge` XML drawable.  
5. **Sau mỗi nhánh:** `./gradlew assembleDebug` trước khi PR.

(phần bổ sung không cần quan tâm ở dưới:
- **`data/WeatherRepository.java` đã có:** Home dùng để `enqueue` + `cancel`; màn khác (FiveDay, Preview) có thể mở rộng repository hoặc thêm method mới thay vì lặp Retrofit trong Activity.  
- **Chuẩn hóa reverse geocoding** (từ lat/lon ra tên hiển thị) khi GPS thành công — hiện `MainActivity` có thể dùng placeholder tên; giao cho track GPS hoặc Search tùy team.  
- **Kiểm thử thủ công** luồng: cài app → xóa data app → mở lại (mô phỏng “xóa hết dữ liệu và cache”).
  
- )
---

## Bảng gán người (tổng quan)

| Người | Trọng tâm | Hạng mục nghiệp vụ được cover |
|-------|-----------|-----------------------------------|
| **A** | Search thật + gợi ý địa điểm | Search; (hỗ trợ) tên địa điểm chuẩn khi chọn từ kết quả |
| **B** | Quản lý danh sách + đổi vị trí về Home | Thêm/xóa/chọn vị trí; SharedPreferences danh sách + `setCurrentLocation`; không chờ API thời tiết |
| **C** | Home + tích hợp API + hiển thị dữ liệu | Trang Home; gọi API; cache/throttle; biểu đồ/list thật |
| **D** | GPS / cold start + Preview UI + nền + icon | Vị trí khi vào app hoặc sau clear data; Preview; gradient/nền theo thời tiết; icon toàn app |

Các hạng mục **nền theo thời tiết** và **icon** gắn chặt **dữ liệu điều kiện** từ API — người **D** có thể làm **UI + hook chỗ gọi** (ví dụ `GradientShiftBackgroundView.setGradientColors`) với **giá trị giả / enum** do **C** cung cấp tên hàm hoặc PR nhỏ; hoặc **D** chỉnh layout/màu cố định trước, **C** nối sau trong cùng file bằng merge có thứ tự (doc khuyên: **C merge trước phần parse `condition`, D merge phần màu/icon**).

---

## Lê Thành Đạt — Search (tìm địa điểm thật)

**Mục tiêu:** Thay tìm kiếm giả bằng kết quả thật; mọi lần chọn vẫn mở `PreviewActivity` với đủ lat/lon + tên (giữ `LocationContract`).

**File / thư mục làm việc chính**

- `app/src/main/java/com/example/weatherforcastapp/SearchActivity.java`  
- `app/src/main/java/com/example/weatherforcastapp/search/FakeGeocoding.java` (**thay thế hoặc** tạo class mới, ví dụ `WeatherApiSearch.java` / `GeocodingRepository.java`, rồi gọi từ `SearchActivity`)  
- `app/src/main/res/layout/activity_search.xml`  
- `app/src/main/res/layout/item_search_suggestion.xml` (nếu làm danh sách gợi ý)  
- `app/src/main/java/com/example/weatherforcastapp/data/PopularCity.java`, `PopularCities.java` (nếu đồng bộ danh sách gợi ý)  
- `app/src/main/res/values/strings.xml` (chuỗi lỗi, hint)

**Thư viện đã có sẵn hỗ trợ**

- Retrofit / OkHttp — có thể thêm endpoint search nếu dùng **WeatherAPI search** (xem docs WeatherAPI: search/autocomplete — *cần đọc docs chính thức khi implement*), hoặc API geocoding khác.  
- Play Services Location — đã dùng trong `SearchActivity` chip định vị qua `LocationHelper` (không cần đổi nếu chỉ làm search text).

**Đầu ra mong đợi**

- Nhập từ khóa / chọn gợi ý → có **lat, lon, displayName** → `PreviewActivity.start(...)` giữ nguyên chữ ký hiện tại.  
- `SearchActivity.EXTRA_MODE` vẫn phân biệt onboarding vs quản lý.  
- Không phá luồng: onboarding **Hủy** → `finishAffinity()`.

**Vì sao ít phụ thuộc người khác:** Chỉ cần contract Intent và `PopularCity`/constructor tương đương; không cần Home đã gọi API xong.

**Xung đột tiềm ẩn:** `SearchActivity.java` — tránh người khác sửa cùng file; nếu cần sửa mode onboarding, báo chủ nhóm.

---

## Vũ Quốc Hùng — Quản lý vị trí: thêm (qua Preview), xóa, chọn về Home, SharedPreferences

**Mục tiêu:** Hoàn thiện CRUD danh sách và **chạm một dòng → đổi vị trí hiện tại → quay Home** với lat/lon đã lưu trong prefs (đúng spec mô tả). Phần “thêm mới” từ Preview có logic riêng — **Hùng** đảm bảo list + giới hạn 20 + xóa; đồng bộ với yêu cầu “nút thêm đã tồn tại” do **Hưởng** trên `PreviewActivity` (Hùng có thể cung cấp hàm `prefs.containsLocation(lat,lon)` nếu thêm vào `WeatherPreferences` trong PR nhỏ).

**File / thư mục làm việc chính**

- `app/src/main/java/com/example/weatherforcastapp/LocationManagementActivity.java`  
- `app/src/main/java/com/example/weatherforcastapp/ui/SavedLocationsAdapter.java`  
- `app/src/main/res/layout/activity_location_management.xml`  
- `app/src/main/res/layout/item_saved_location.xml`  
- `app/src/main/java/com/example/weatherforcastapp/prefs/WeatherPreferences.java` — **chỉ** thêm helper rõ ràng (ví dụ `hasSavedLocationWithCoords`, `findById`) nếu cần; tránh refactor lớn.  
- `app/src/main/java/com/example/weatherforcastapp/model/SavedLocation.java` (nếu cần field phụ — hạn chế)

**Thư viện đã có**

- Gson (qua prefs), RecyclerView, Material.

**Đầu ra mong đợi**

- Search bar trên quản lý → vẫn mở `SearchActivity.startForManagement` như hiện tại.  
- Long-press → chế độ chọn + xóa đã chọn / xóa tất cả / xong (đã có khung).  
- Tap item → `setCurrentLocation` + `HomeActivity.startClearTop` + `finish()` (đã có — kiểm tra lại sau khi Home load API thật).  
- Đảm bảo **không mất đồng bộ** id với `SavedLocation.buildId` và `cacheKeyForCoords`.

**Vì sao ít phụ thuộc:** Không cần API thời tiết; chỉ cần danh sách và prefs.

**Xung đột tiềm ẩn:** `WeatherPreferences.java` với **Huy** (cache API). **Giải pháp:** Hùng chỉ sửa nhóm key `locations_json` / `current_*`; Huy chỉ sửa `cache_body_*` / `api_last_*` — tách biệt key sẵn có; mọi hàm mới thêm ở cuối file, review chéo.

---

## Nguyễn Công Huy — Trang Home: hiển thị dữ liệu thật + gọi API + cache

**Mục tiêu:** Bổ sung / tinh chỉnh phần hiển thị và dữ liệu trên Home. **Đã có sẵn:** `WeatherRepository` (forecast + cache + throttle), POJO `model.api`, `HomeActivity.bindForecastToHome`, `SwipeRefresh` làm mới. Việc còn lại tiêu biểu: nối **biểu đồ** (`ChartSamples` / hourly từ `forecastday[].hour`), thêm field DTO nếu cần, hoặc gọi thêm `getCurrent` nếu tách riêng.

**File / thư mục làm việc chính**

- `app/src/main/java/com/example/weatherforcastapp/HomeActivity.java`  
- `app/src/main/java/com/example/weatherforcastapp/data/WeatherRepository.java` — thêm overload / method cho màn khác hoặc tách `getCurrent` khi cần  
- `app/src/main/java/com/example/weatherforcastapp/api/WeatherApiService.java` (chỉ khi cần endpoint mới — giữ chữ ký `Call<…POJO>`)  
- `app/src/main/java/com/example/weatherforcastapp/model/api/` — mở rộng DTO theo JSON WeatherAPI  
- `app/src/main/java/com/example/weatherforcastapp/ui/ForecastDayAdapter.java`  
- `app/src/main/java/com/example/weatherforcastapp/util/ChartSamples.java` — nối dữ liệu thật  
- `app/src/main/res/layout/activity_home.xml` (chỉ phần bind ID — tránh đổi id đang dùng)  
- `app/src/main/java/com/example/weatherforcastapp/prefs/WeatherPreferences.java` — nhánh cache/throttle **chỉ** nếu Công Huy là người sửa các method cache (Quốc Hùng không đổi cùng đoạn)

**Thư viện đã có**

- Retrofit, OkHttp, Gson, Glide, `WeatherApiIcons`, MPAndroidChart, SwipeRefreshLayout, NestedScrollView + MotionLayout.

**Đầu ra mong đợi**

- Home đã hiển thị **nhiệt độ, điều kiện, 3 hàng dự báo, icon hero** từ API/cache; PR của Công Huy tập trung **mở rộng** (chart, metrics, v.v.).  
- Đổi vị trí từ `LocationManagement` → Home nhận `onNewIntent` / prefs → **gọi lại** `fetchForecastForHome`.  
- Giữ quy ước: **Retrofit trả về POJO**, không trả lại `ResponseBody` cho các endpoint đã typ hóa.

**Phụ thuộc có kiểm soát:** Cần key API local; không cần Quốc Hùng xong UI — chỉ cần tọa độ trong prefs (có thể set tay khi test).

**Xung đột:** `HomeActivity.java` nặng — **Nguyên Hưởng** nếu sửa nền chỉ đụng `GradientShiftBackgroundView` trong XML; **Công Huy** ưu tiên sửa `bindForecastToHome` / `WeatherRepository` / `ChartSamples` để tách phần merge.

---

## Giang Nguyên Hưởng — GPS khi vào app / sau khi xóa data; Preview UI; nền theo thời tiết; icon toàn app; nút “thêm” khi đã tồn tại

**Mục tiêu:**  
1) **GPS:** Đảm bảo khi **chưa có** `hasCurrentLocation()` (gồm cả user xóa data app), luồng `MainActivity` vẫn hỏi quyền / đưa Search đúng spec; có thể bổ sung lấy tên địa điểm thật sau GPS.  
2) **Preview:** Làm lại giao diện `activity_preview.xml` (5 ngày ở giữa nếu spec — *hiện code là khối giờ “Hôm nay” + FAB*; thống nhất với designer/spec); nút **Thêm vào trang home**: nếu `SavedLocation.buildId(lat,lon)` đã có trong `getSavedLocations()` thì **disable** + thông báo “đã thêm”.  
3) **Nền theo thời tiết:** `GradientShiftBackgroundView` hoặc drawable/theme — đổi màu theo nhóm điều kiện (mưa/nắng/đêm). Có thể nhận `condition_code` hoặc enum từ **Nguyễn Công Huy** qua callback static / interface; giai đoạn 1 có thể mock theo mã cố định.  
4) **Icon:** Rà soát `res/drawable`, `mipmap`, chỗ `android:src` / Glide — thống nhất bộ icon (vector), kích thước, dark theme.

**File / thư mục làm việc chính**

- `app/src/main/java/com/example/weatherforcastapp/MainActivity.java`  
- `app/src/main/java/com/example/weatherforcastapp/util/LocationHelper.java` (tinh chỉnh timeout / accuracy nếu cần)  
- `app/src/main/res/layout/activity_main.xml`  
- `app/src/main/java/com/example/weatherforcastapp/PreviewActivity.java` + `app/src/main/res/layout/activity_preview.xml`  
- `app/src/main/java/com/example/weatherforcastapp/ui/widget/GradientShiftBackgroundView.java`  
- `app/src/main/res/layout/activity_home.xml` (nền + decor)  
- `app/src/main/res/drawable/**` (icon, gradient)  
- `app/src/main/res/values/colors.xml`, `themes.xml`  
- Có thể đụng nhẹ `FiveDayForecastActivity` / `activity_five_day_forecast.xml` nếu đồng bộ style icon/nền (tránh cùng sprint với Công Huy nếu Công Huy đang sửa sâu FiveDay — hoặc chia ngày merge)

**Thư viện đã có**

- Material dialog, Play Services Location, BlurView (Preview/FiveDay), Lottie (nếu dùng animation thời tiết).

**Đầu ra mong đợi**

- Cài app lần đầu hoặc **Settings → Xóa dữ liệu** → mở app → lại popup / luồng Search như spec.  
- Preview đẹp, đúng FAB behavior; trùng địa điểm → không thêm trùng, UX rõ ràng.  
- Home (và tuỳ chọn màn khác) có **nền / họa tiết** phản ánh tình trạng thời tiết (ít nhất 2–3 trạng thái).  
- Bộ icon nhất quán, không vỡ trên dark background.

**Phụ thuộc:** Logic “đã tồn tại” cần đọc prefs — dùng API public của `WeatherPreferences`; nếu thiếu hàm `containsCoords`, nhờ **Vũ Quốc Hùng** thêm trong PR nhỏ hoặc **Giang Nguyên Hưởng** thêm method chỉ đọc list (tránh trùng với Vũ Quốc Hùng: một người commit hàm đó).

**Xung đột:** Rất nhiều file `drawable` — chia theo **prefix** (vd `ic_nav_*` do Giang Nguyên Hưởng) hoặc làm một PR icon-only.

---

## Tóm lại

| Yêu cầu | Người chính                                            | Ghi chú                                                             |
|---------|--------------------------------------------------------|---------------------------------------------------------------------|
| Search | Lê Thành Đạt                                           | Thay `FakeGeocoding` / nối API search                               |
| Thêm / xóa vị trí | Vũ Quốc Hùng (+ Preview FAB Giang Nguyên Hưởng)        | List + xóa ở `LocationManagement`; thêm qua `PreviewActivity`       |
| Bấm vị trí có sẵn → đổi vị trí → back Home (SharedPreferences) | Vũ Quốc Hùng                                           | Đã có `onOpen` + `startClearTop`; kiểm tra sau khi Công Huy nối API |
| Hiển thị dữ liệu Home | Nguyễn Công Huy                                        | API + UI Home                                                       |
| Chuyển background / phông nền theo thời tiết | Giang Nguyên Hưởng (+ hook dữ liệu từ Nguyễn Công Huy) | `GradientShiftBackgroundView` + màu                                 |
| Tìm & sửa icon toàn app | Giang Nguyên Hưởng                                     | `drawable`, layout `src`                                            |
| Làm lại UI Preview + nút thêm trùng | Giang Nguyên Hưởng (+ helper prefs Vũ Quốc Hùng)       | `PreviewActivity` + `activity_preview.xml`                          |
| GPS khi vào app / sau khi xóa data | Giang Nguyên Hưởng + `MainActivity`                    | Đã có `hasCurrentLocation`; verify edge cases                       |


---

## Checklist trước khi coi “xong sprint”

- [ ] Mọi Intent lat/lon dùng `LocationContract`.  
- [ ] `local.properties` có `WEATHERAPI_KEY` (đúng với code hiện tại).  
- [ ] `assembleDebug` pass.  
- [ ] Luồng: deny GPS → Search → không hủy → chọn city → Home; hủy Search onboarding → thoát app.  
- [ ] Giới hạn 20 địa điểm vẫn hoạt động.

---