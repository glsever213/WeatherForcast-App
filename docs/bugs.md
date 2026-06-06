# BUGS & GIAO LẠI VIỆC SAU KHI MERGE

> Tài liệu này ghi lại **những chỗ làm chưa đúng sau khi merge 4 nhánh** (Huy, Hùng, Đạt, Hưởng) và **giao lại việc cụ thể** cho từng người — gồm **cả lỗi logic lẫn giao diện (UI)** trong cùng một chỗ.
>
> Phần quan trọng nhất là **Mục 1 — HÀM CHUNG LẤY / TRUYỀN LAT-LON**. Tất cả thành viên đụng tới lat/lon **bắt buộc đọc Mục 1 trước**, vì lỗi chung của cả nhóm là *không biết lấy lat/lon ở đâu cho thống nhất* nên mỗi người tự fix cứng một kiểu.

## Mục lục

- [Mục 1 — Hàm chung lấy / truyền lat-lon](#1-hàm-chung-lấy--truyền-lat-lon-đọc-kỹ-phần-này)
- [Mục 2 — Bảng màu & bộ icon chung (làm TRƯỚC)](#2-bảng-màu--bộ-icon-chung-làm-trước)
- [Mục 3 — Giao lại việc theo từng người](#3-giao-lại-việc-theo-từng-người)
  - [Huy — Home + trang 5 ngày](#31-huy--trang-home-homeactivity--trang-5-ngày)
  - [Hùng — Quản lý vị trí / CRUD](#32-hùng--quản-lý-vị-trí--crud)
  - [Đạt — Search + GPS](#33-đạt--search-searchactivity--gps)
  - [Hưởng — Preview + theme + icon](#34-hưởng--preview--theme-nền-theo-thời-tiết--icon)
- [Mục 4 — Checklist trước khi merge lại](#4-checklist-trước-khi-merge-lại)

---

## 1. HÀM CHUNG LẤY / TRUYỀN LAT-LON (đọc kỹ phần này)

### 1.1 Vấn đề đang gặp

Mọi trang trong app đều cần **lat/lon của người dùng** (Home, Preview, 5 ngày, theme nền, đổi vị trí…). Nhưng hiện tại mỗi người làm một kiểu:

- Có người **fix cứng** toạ độ Hà Nội (`21.0278, 105.8342`).
- Có người đọc từ `Intent`.
- Có người đọc từ `SharedPreferences`.
- Có người để toạ độ mẫu / `@SuppressWarnings("unused")` rồi dùng dữ liệu giả.

-> Toang: Home một toạ độ, Preview một toạ độ, 5 ngày lại toạ độ khác — **không thống nhất**, theme nền không đúng thời tiết thật, đổi vị trí xong vẫn ra Hà Nội.

### 1.2 Quy ước CHUẨN — chỉ có **2 nơi**, mỗi nơi một nhiệm vụ

Trong project **đã có sẵn 2 lớp** dùng để xử lý lat/lon. **Không tự tạo thêm cách mới, không tự `putExtra("lat")`, không fix cứng số.**

#### (A) NƠI LƯU "VỊ TRÍ HIỆN TẠI" — nguồn sự thật duy nhất: `WeatherPreferences`

`prefs/WeatherPreferences.java` là **kho lưu vị trí người dùng đang xem**. Đây là **nguồn sự thật (single source of truth)**.

```java
WeatherPreferences prefs = WeatherPreferences.get(context);

// LẤY lat/lon hiện tại (mọi trang chỉ HIỂN THỊ thì dùng cái này)
double lat  = prefs.getCurrentLat();
double lon  = prefs.getCurrentLon();
String name = prefs.getCurrentName();

// GHI lat/lon (chỉ gọi khi vị trí THỰC SỰ thay đổi)
prefs.setCurrentLocation(lat, lon, name);

// KIỂM TRA đã có vị trí chưa
boolean has = prefs.hasCurrentLocation();
```

#### (B) NƠI TRUYỀN lat/lon GIỮA 2 MÀN HÌNH: `LocationContract`

`location/LocationContract.java` dùng để **đặt lat/lon vào Intent** khi mở một Activity khác.

```java
// Bên GỬI (trước startActivity)
Intent i = new Intent(this, NextActivity.class);
LocationContract.putLocation(i, lat, lon, name);
startActivity(i);

// Bên NHẬN (trong onCreate / onNewIntent)
double lat  = LocationContract.readLat(getIntent(), 0);
double lon  = LocationContract.readLon(getIntent(), 0);
String name = LocationContract.readDisplayName(getIntent());
```

### 1.3 Khi nào dùng (A), khi nào dùng (B)? — BẢNG QUYẾT ĐỊNH

| Loại màn hình | Việc của màn | Lấy / truyền lat-lon thế nào |
|---|---|---|
| **Màn ĐỔI vị trí** (MainActivity-GPS, SearchActivity → chọn, LocationManagement → tap chọn) | Tạo ra lat/lon mới | Gọi `prefs.setCurrentLocation(...)` **và** truyền tiếp bằng `LocationContract.putLocation(...)` khi mở màn sau |
| **Màn CHỈ HIỂN THỊ vị trí hiện tại** (HomeActivity, FiveDayForecastActivity, theme nền) | Chỉ đọc để hiển thị | Đọc `prefs.getCurrentLat()` / `prefs.getCurrentLon()` / `prefs.getCurrentName()` |
| **Màn xem trước một địa điểm cụ thể** (PreviewActivity) | Xem 1 địa điểm vừa chọn (chưa chắc là vị trí hiện tại) | Nhận qua `LocationContract.readLat/readLon`; chỉ `setCurrentLocation` khi người dùng bấm xác nhận |

### 1.4 Quy tắc vàng (ai cũng phải thuộc)

1. **KHÔNG fix cứng toạ độ** (`21.0278, 105.8342`, `21.03, 105.85`, …) trong code chạy thật. Toạ độ cứng chỉ được dùng tạm khi test cục bộ và **phải xoá trước khi commit**.
2. Màn chỉ hiển thị → **luôn lấy từ `prefs.getCurrentLat()/getCurrentLon()`**.
3. Khi mở màn khác cần toạ độ → **luôn truyền qua `LocationContract.putLocation()`**, không tự đặt key Intent khác.
4. Chỉ màn "đổi vị trí" mới được gọi `setCurrentLocation()`. Màn hiển thị **không được** ghi đè lại vị trí.
5. Gọi API thời tiết → query lấy từ `WeatherApiQuery.latLon(lat, lon)` với đúng cặp lat/lon của màn đang hiển thị.

> Tóm 1 câu: **"Vị trí hiện tại" chỉ nằm ở một chỗ là `WeatherPreferences`. Muốn hiển thị thì `get`, muốn đổi thì `set`, muốn đưa sang màn khác thì bỏ vào `LocationContract`."**

---

## 2. BẢNG MÀU & BỘ ICON CHUNG (làm TRƯỚC)

Lỗi giao diện chung của cả nhóm: **mỗi màn một kiểu màu, icon lệch nhau**, còn nhiều placeholder xấu (`-°`, `— / —`).

**Nguyên tắc:**
- Dùng chung `res/values/colors.xml` + `themes.xml`. **Không** ai hard-code màu lẻ trong layout.
- Dùng chung **một bộ icon vector**, cùng kích thước, hiển thị tốt trên nền tối; đặt tên theo prefix (`ic_weather_*`, `ic_nav_*`).
- **Người chốt bảng màu + bộ icon: Hưởng** → ra một **PR nhỏ "colors + theme + icon" merge trước**, rồi Huy / Hùng / Đạt mới áp vào màn của mình.

> Thứ tự để không đá nhau: **Hưởng chốt màu + icon trước → cả nhóm theo.**

---

## 3. GIAO LẠI VIỆC THEO TỪNG NGƯỜI

> Mỗi người gồm **(A) Lỗi logic / dữ liệu** và **(B) Giao diện** trong cùng một mục.

### 3.1 Huy — Trang Home (`HomeActivity`) + Trang 5 ngày

**Lời / hiện trạng:** Huy đang **fix cứng lat/lon**, không biết dùng hàm chuyển lat/lon chung nào. Ngoài ra tôi giao thêm việc sửa lại giao diện và dữ liệu ở **trang xem trước 5 ngày**.

#### (A) Lỗi logic / dữ liệu

- [`HomeActivity.java:193`](../app/src/main/java/com/example/weatherforcastapp/HomeActivity.java#L193) — ngay sau khi đã lưu đúng vị trí ở dòng 192, dòng 193 lại **ghi đè cứng**:
  ```java
  prefs.setCurrentLocation(la, lo, LocationContract.readDisplayName(intent)); // dòng 192 - đúng
  prefs.setCurrentLocation(21.0278, 105.8342, "Hà Nội");                      // dòng 193 - SAI, fix cứng đè lên
  ```
  → **Xoá ngay** dòng 193 và đoạn comment fix cứng ở [`HomeActivity.java:112-114`](../app/src/main/java/com/example/weatherforcastapp/HomeActivity.java#L112).
- Home **chỉ được đọc** vị trí qua hàm chung: `prefs.getCurrentLat()`, `prefs.getCurrentLon()`, `prefs.getCurrentName()` (Mục 1.2-A). Khi nhận `onNewIntent` thì lấy từ `LocationContract` rồi `setCurrentLocation` **một lần đúng giá trị thật**, không đè Hà Nội.

**Trang 5 ngày (`FiveDayForecastActivity`):**
- [`FiveDayForecastActivity.java:40-43`](../app/src/main/java/com/example/weatherforcastapp/FiveDayForecastActivity.java#L40) — đã đọc lat/lon từ `LocationContract` **nhưng đánh dấu `@SuppressWarnings("unused")`** và **không dùng**.
- [`FiveDayForecastActivity.java:57-59`](../app/src/main/java/com/example/weatherforcastapp/FiveDayForecastActivity.java#L57) — đang đổ **dữ liệu mẫu** `Forecast5dSamples.defaultSlots()`, không gọi API thật.
- → Bỏ `@SuppressWarnings("unused")`, dùng lat/lon đã đọc; nếu Intent không có thì fallback `prefs.getCurrentLat()/getCurrentLon()`. **Gọi API thật** (tái dùng `WeatherRepository.fetchForecastForHome` hoặc thêm method 5 ngày) thay cho dữ liệu mẫu. **Không fix cứng.**

#### (B) Giao diện

- **Home (`activity_home.xml`):** áp bảng màu chung của Hưởng; bỏ màu hard-code lẻ; phần hero / nhiệt độ / danh sách ngày hiển thị gọn, đúng dữ liệu thật.
- **Trang 5 ngày (`activity_five_day_forecast.xml`):** chỉnh bố cục cho **hợp lý**, cân đối khối "Hôm nay" (giờ) và bảng 5 ngày; **thống nhất màu** với Home và phần còn lại.

---

### 3.2 Hùng — Quản lý vị trí / CRUD (`LocationManagementActivity`, `SavedLocationsAdapter`)

**Lời / hiện trạng:** component giao diện cho "từng item vị trí" còn xấu, **chưa hiển thị nhiệt độ** (đang để `—°` / `— / —`), thiếu icon; và **khi bấm chọn vị trí thì lat/lon phải đi qua hàm chung**.

#### (A) Lỗi logic / dữ liệu

- [`SavedLocationsAdapter.java:71-73`](../app/src/main/java/com/example/weatherforcastapp/ui/SavedLocationsAdapter.java#L71) — đang **hard-code** placeholder, không bao giờ ra số thật:
  ```java
  holder.binding.textCondition.setText(... "—" ...);
  holder.binding.textHighLow.setText("— / —");
  holder.binding.textTempBig.setText("—°");
  ```
  → **Hiển thị nhiệt độ thật** cho từng item: dùng dữ liệu cache trong `SavedLocation` (ví dụ `getCachedSummaryLine()`), hoặc gọi API gọn nhẹ theo lat/lon của item. Không để `-°` / `-%`.
- Phần **tap → đổi vị trí** ở [`LocationManagementActivity.java:85-86`](../app/src/main/java/com/example/weatherforcastapp/LocationManagementActivity.java#L85) hiện đã gọi `setCurrentLocation(...)` + `HomeActivity.startClearTop(...)` — **về cơ bản đúng hướng**. Cần bảo đảm lat/lon **luôn** lấy từ `location.getLatitude()/getLongitude()` của chính item đang bấm rồi đẩy qua hàm chung (Mục 1.2), không lấy nhầm vị trí khác.
- Giữ giới hạn 20 địa điểm và đồng bộ `SavedLocation.buildId` / `cacheKeyForCoords` như cũ.

#### (B) Giao diện

`item_saved_location.xml` + `activity_location_management.xml`:
- Làm **đẹp cụm item vị trí**: bố cục rõ ràng tên thành phố / điều kiện / nhiệt độ.
- **Thêm icon thời tiết** cho từng item (dùng bộ icon chung của Hưởng).
- Áp bảng màu chung; màn quản lý nền tối, chữ sáng, dễ đọc.

---

### 3.3 Đạt — Search (`SearchActivity`) + GPS

**Lời / hiện trạng:**
- Giao diện **Search khá xấu, chưa đẹp**.
- **GPS không truy cập / không dùng được** trên máy ảo Android — nghi do cấu hình Android Studio / emulator chứ không hẳn lỗi code.

#### (A) Lỗi logic / dữ liệu — phân tích GPS

- Hàm lấy GPS chung là [`LocationHelper.fetchCurrent(...)`](../app/src/main/java/com/example/weatherforcastapp/util/LocationHelper.java#L36), đã có sẵn fallback `getCurrentLocation` → `getLastLocation` → timeout 15s. **Code đã đúng pattern.**
- Trên **máy ảo Android**, GPS thường trả `null` nếu chưa **set vị trí giả**. Cần làm trong emulator:
  1. Mở **Extended Controls** (dấu `...` cạnh emulator) → tab **Location**.
  2. Nhập một toạ độ (hoặc chọn trên bản đồ) → bấm **Set Location** / **Save point** → Send.
  3. Bảo đảm emulator dùng **Google APIs / Google Play** image (không phải bản AOSP thiếu Play Services), vì `FusedLocationProviderClient` cần Google Play Services.
  4. Cấp quyền vị trí cho app khi popup hỏi (màn `MainActivity`).
- Nếu vẫn `null`: kiểm tra `AndroidManifest` đã có `ACCESS_FINE_LOCATION` / `ACCESS_COARSE_LOCATION` (đã có), và thử bật "Location" trong Settings của máy ảo.

**Việc cần làm:** **không cần sửa logic `LocationHelper`** (đã đúng). Việc là **cấu hình emulator có vị trí giả + dùng image Google Play** rồi test lại; ghi lại các bước để cả nhóm chạy được. Mọi nơi cần GPS đều gọi qua `LocationHelper.fetchCurrent(...)`, không tự khởi tạo `FusedLocationProviderClient` riêng. Có lat/lon → đẩy qua hàm chung (Mục 1).

#### (B) Giao diện

`activity_search.xml` + `item_search_suggestion.xml`:
- **Làm lại Search cho đẹp**: ô tìm kiếm rõ ràng, danh sách gợi ý gọn, chip định vị/thành phố thống nhất style.
- Áp bảng màu + icon chung của Hưởng.
- Trạng thái rỗng / đang tìm / lỗi hiển thị thân thiện (dùng chuỗi trong `strings.xml`).

---

### 3.4 Hưởng — Preview + Theme nền theo thời tiết + Icon

**Lời / hiện trạng:**
- Hưởng **đang phải đợi Huy hoàn thành trang Home** (và **fix lat/lon**) thì mới làm **theme dựa theo thời tiết** được, và để **thống nhất màu**.
- Hưởng đã có **giao diện popup preview** rồi nên sẽ bổ sung thêm.
- Hưởng cũng là người **chốt bảng màu + bộ icon chung** (xem Mục 2) 

#### Phụ thuộc (ghi rõ để khỏi chờ nhau mơ hồ)

- Theme nền theo thời tiết cần **điều kiện thời tiết thật** + **lat/lon đúng**. Cả hai phụ thuộc Huy: 
a) xoá fix cứng lat/lon (Mục 3.1-A), 
b) cung cấp nhóm điều kiện (mưa/nắng/đêm) hoặc `condition_code` từ API trên Home.
- Trong lúc chờ: Hưởng **vẫn làm trước** phần đổi màu `GradientShiftBackgroundView` với **enum/mã điều kiện mock**, rồi nối dữ liệu thật sau (merge theo thứ tự: Huy nối `condition` trước, Hưởng nối màu/icon sau).

#### Việc của Hưởng

1. **Chốt bảng màu chung** trong `colors.xml` + `themes.xml`: nền, chữ chính/phụ, màu nhấn, màu trạng thái (nắng / mưa / đêm) — PR nhỏ merge trước.
2. **Bộ icon toàn app:** rà `res/drawable` / `mipmap` / `android:src` / Glide → thống nhất một bộ vector, cùng kích thước, tốt trên nền tối. (phần này nếu các ae tự import icon rồi thì thôi)
3. **Theme nền theo thời tiết:** `ui/widget/GradientShiftBackgroundView.java` đổi gradient theo nhóm điều kiện (mưa / nắng / nhiều mây / đêm). Lat/lon nếu cần lấy từ **hàm chung** (Mục 1.2).
4. **Preview (`PreviewActivity` + `activity_preview.xml`):** hoàn thiện popup đã có, thống nhất màu/icon; nút **"Thêm vào trang home"** nếu địa điểm đã tồn tại (`prefs.hasSavedLocationWithCoords(lat, lon)`) → disable + báo "đã thêm".

---

## 4. CHECKLIST TRƯỚC KHI MERGE LẠI

**Logic / lat-lon:**
- [ ] Không còn bất kỳ chỗ nào fix cứng `21.0278, 105.8342` / `21.03, 105.85` trong code chạy thật.
- [ ] Mọi màn **hiển thị** đọc lat/lon qua `WeatherPreferences.getCurrentLat()/getCurrentLon()`.
- [ ] Mọi lần **truyền** lat/lon giữa Activity đều qua `LocationContract.putLocation()`.
- [ ] Chỉ màn **đổi vị trí** mới gọi `setCurrentLocation()`.
- [ ] Item quản lý vị trí hiển thị **nhiệt độ thật**, không còn `-°` / `-%`.
- [ ] Trang 5 ngày dùng **dữ liệu API thật** theo lat/lon chung, không còn dữ liệu mẫu.
- [ ] GPS chạy được trên emulator (image Google Play + đã set vị trí giả).

**Giao diện:**
- [ ] Tất cả màn dùng **chung bảng màu** trong `colors.xml` / `themes.xml`, không hard-code màu lẻ.
- [ ] Dùng **chung bộ icon** (vector), hiển thị tốt trên nền tối.
- [ ] Không còn placeholder xấu `-°` / `-%` / `— / —` trên màn thật.
- [ ] Search, item vị trí, Preview, 5 ngày trông cùng "một ngôn ngữ thiết kế".

**Build:**
- [ ] `./gradlew assembleDebug` pass.
