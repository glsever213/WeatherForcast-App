# Sử dụng Git trên Android Studio

---

## Quy tắc chung của dự án

| Quy tắc | Nội dung |
|---------|----------|
| **1** | **KHÔNG** viết code trực tiếp trên nhánh `main`. |
| **2** | Nhánh trung tâm để mọi người gộp code làm việc hàng ngày là **`dev`** (hoặc tên khác do trưởng nhóm quy định — trong doc này gọi là `dev`). |
| **3** | Mỗi tính năng / phần việc làm trên **nhánh riêng**: `feature/tên-chức-năng` (vd: `feature/tim-kiem`, `feature/giao-dien-home`). |
| **4** | Gộp code vào `dev` bằng **Pull Request trên web**; khuyến nghị **không** tự `merge` bằng Terminal nếu nhóm mới quen. |

---

## Quy tắc đặt tên (Naming convention)

- **Tên repo:** WeatherForcast-app
- **`main` (hoặc `master`):** chỉ code **ổn định**, dùng để nộp code / release — không phải chỗ code hằng ngày.
- **`dev`:** nhánh **tích hợp** — mọi Pull Request feature được merge vào đây để cả nhóm test chung.
- **Nhánh cá nhân / theo task:** `feature/tên-chức-năng`. VD: `feature/login`, `feature/weather-api`, `feature/home-ui`, `feature/api-thoi-tiet`.

---

## Thành viên: tham gia dự án lần đầu (Clone)

Thao tác **một lần** khi máy chưa có mã nguồn.

1. Mở **Android Studio** (màn hình Welcome, chưa mở project).
2. Bấm **Get from VCS** (hoặc **Projects** → **Get from VCS** / **Clone Repository** tùy giao diện).
3. Dán **URL repo** GitHub vào ô URL.
4. Chọn thư mục lưu trên máy → bấm **Clone**.
5. Android Studio tải code và gợi ý mở project — chọn **Trust** / mở Gradle nếu được hỏi.

**Cách khác:** **File → New → Project from Version Control** → dán URL → Clone.

---

## Phần C — Luồng làm việc hàng ngày (chuẩn cho mọi thành viên)

Mục tiêu: luôn bắt đầu từ **`dev` mới nhất**, làm việc trên **`feature/...`**, đẩy lên GitHub và tạo Pull Request vào **`dev`**.

### C1. Cập nhật code mới nhất từ nhánh chung (Pull / Update)

**Luôn làm trước khi bắt đầu code mới** (hoặc trước khi tạo nhánh feature mới từ `dev`).

1. Nhìn **góc dưới bên phải** Android Studio → bấm vào **tên nhánh hiện tại** (vd: `main`, `feature/...`).
2. Trong danh sách, chọn nhánh **`dev`** → **Checkout** (hoặc **Checkout and Rebase** — nếu không chắc, dùng **Checkout**).
3. Sau khi đã ở nhánh **`dev`**, bấm biểu tượng **Update Project** (mũi tên xanh chéo **xuống**) trên thanh công cụ, hoặc phím tắt **Ctrl + T** (Windows) / **Cmd + T** (Mac).
4. Nếu hỏi kiểu cập nhật: chọn **Merge incoming changes into the current branch** (nếu có) → **OK**.

Kết quả: máy bạn có bản **`dev`** mới nhất trên server.

### C2. Tạo nhánh làm việc cá nhân (Branch)

1. Đảm bảo đang đứng nhánh **`dev`** và đã **Update** như bước C1.
2. Bấm lại **tên nhánh** góc dưới phải → **New Branch**.
3. Đặt tên theo quy ước: `feature/tên-chức-năng` (vd: `feature/tim-kiem`, `feature/giao-dien-home`).
4. Bấm **Create**. Từ lúc này, mọi commit nằm trên nhánh này — **không** ảnh hưởng trực tiếp `main` / `dev` cho đến khi merge qua PR.

### C3. Code bình thường

Sửa file trong project như thường.

### C4. Lưu và đẩy lên máy chủ (Commit & Push) — **khuyên dùng giao diện**

Khi xong một phần việc hoặc hết ngày:

1. Nhấn **Ctrl + K** (Windows) / **Cmd + K** (Mac) — cửa sổ **Commit** hiện (thường bên trái / panel Git).
2. **Tích chọn** các file cần đưa vào commit (Android Studio thường đã bỏ qua `build/`, file sinh ra nếu `.gitignore` đúng).
3. Nhập **Commit message** ngắn, rõ (vd: `Hoàn thành màn hình danh sách địa điểm`).
4. Bấm **mũi tên xổ xuống** cạnh nút **Commit** → chọn **Commit and Push…**
5. Trong hộp thoại Push, kiểm tra **nhánh đích** là nhánh `feature/...` của bạn → bấm **Push**.

**Lưu ý:** Không commit `local.properties` nếu chứa API key — kiểm tra trong danh sách file trước khi tích; file này thường nằm trong `.gitignore`.

### C5. Gộp code vào dự án chung (Pull Request — trên GitHub)

**Không khuyến khích** tự merge bằng Terminal khi nhóm mới bắt đầu.

1. Mở **trình duyệt** → vào trang repo trên GitHub.
2. GitHub thường hiện thông báo nhánh vừa push → bấm **Compare & pull request** (hoặc **Pull requests** → **New pull request**).
3. Cấu hình PR:
   - **base** (nhánh đích): **`dev`** — nơi muốn gộp code vào.
   - **compare** (nhánh nguồn): nhánh bạn vừa làm (vd: `feature/tim-kiem`).
4. Điền mô tả ngắn → **Create pull request**.
5. Báo **trưởng nhóm** / người review. Người có quyền bấm **Merge pull request** trên web để gộp vào **`dev`**.

---

## Phần D — Sau khi PR đã được merge: các thành viên khác lấy code mới

Ví dụ nhánh `dev` trên GitHub đã có code của bạn A; bạn B đang làm `feature/home-ui`:

1. Bấm **tên nhánh** góc dưới phải → chọn **`dev`** → **Checkout**.
2. Bấm **Update Project** (mũi tên xanh xuống) hoặc **Ctrl + T** để kéo `dev` mới nhất.
3. Bấm lại **tên nhánh** → chọn nhánh **`feature/home-ui`** của B → **Checkout** để tiếp tục làm việc.
4. **Nếu cần** đưa code mới từ `dev` vào nhánh feature của B (để tránh lệch quá xa): khi đang ở `feature/home-ui`, dùng menu **Git → Merge…** (hoặc **Update Project** với tùy chọn merge từ `dev` — tùy phiên bản) để merge `dev` vào nhánh hiện tại; hoặc nhờ trưởng nhóm hướng dẫn thao tác merge `dev` → `feature/...` khi conflict ít.

*(Chi tiết merge nhánh dài hạn có thể bổ sung sau; tối thiểu: luôn **Update `dev`** trước khi **tạo nhánh feature mới** từ `dev`.)*

---

## Phần E — Xử lý xung đột (Conflict)

Xảy ra khi hai người sửa trùng vùng code, hoặc khi merge/update.

1. Android Studio báo **Conflict** / file đỏ trong cửa sổ Git.
2. Chọn file có conflict → bấm **Merge…** (hoặc **Resolve conflicts**).
3. Giao diện **3 cột:**
   - **Trái:** thường là phiên bản từ server / nhánh được gộp vào.
   - **Phải:** phiên bản của bạn.
   - **Giữa:** kết quả cuối.
4. Dùng mũi tên **>>** / **<<** để đưa thay đổi vào cột giữa; có thể sửa tay ở cột giữa.
5. **Apply** khi xong → **Commit** và **Push** lại (Ctrl + K → Commit and Push).

---

## Bảng tra cứu nhanh (Android Studio)

| Thao tác | Windows | Mac | Giao diện (tham khảo) |
|----------|---------|-----|------------------------|
| Lấy code mới (Update / Pull) | **Ctrl + T** | **Cmd + T** | Nút mũi tên xanh **chéo xuống** trên thanh công cụ |
| Commit | **Ctrl + K** | **Cmd + K** | Git tool window — nút commit |
| Push | **Ctrl + Shift + K** | **Cmd + Shift + K** | Nút mũi tên xanh **chéo lên** / sau Commit and Push |
| Đổi / tạo nhánh | — | — | **Góc dưới phải** — tên nhánh hiện tại |

*(Tên menu có thể là **VCS** hoặc **Git** tùy phiên bản Android Studio.)*

---

## TÓm lại

1. **Không** code trực tiếp trên **`main`**.
2. Mỗi ngày (hoặc trước khi tạo feature mới): **Checkout `dev`** → **Update Project (Ctrl + T)**.
3. **New Branch** `feature/...` từ **`dev`** đã mới.
4. Code xong: **Ctrl + K** → chọn file → message → **Commit and Push…**
5. Lên **GitHub** tạo **Pull Request**: **base = `dev`**, **compare = feature/...**
6. Sau khi merge: mọi người **Checkout `dev`** → **Update** để đồng bộ.

---

## Phụ lục — Terminal (tùy chọn)

Ai quen dòng lệnh có thể dùng tab **Terminal** trong Android Studio (PowerShell trên Windows vẫn chạy `git` được):

```text
git checkout dev
git pull origin dev
git checkout -b feature/ten-chuc-nang
# ... sau khi sửa code ...
git add .
git commit -m "Mô tả ngắn"
git push -u origin feature/ten-chuc-nang
```

Khuyến nghị nhóm mới: **ưu tiên thao tác trong Android Studio + Pull Request trên web** như các mục trên.

---

*Tài liệu này thay thế phiên bản cũ chỉ tập trung lệnh Terminal; quy trình nhánh `main` / `dev` / `feature` do nhóm thống nhất.*
