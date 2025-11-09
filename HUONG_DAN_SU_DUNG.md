# Hướng dẫn sử dụng 2 chức năng đã hoàn thiện

## 🎯 Chức năng 1: Chỉnh sửa và Xóa công thức

### Cách sử dụng:

#### 1. Chỉnh sửa công thức:

**Bước 1:** Mở công thức bạn đã tạo
- Vào danh sách công thức
- Chọn công thức của bạn
- **Lưu ý:** Chỉ công thức bạn tạo mới có nút "Chỉnh sửa"

**Bước 2:** Nhấn nút "Chỉnh sửa" (icon edit)
- Nút chỉ hiển thị nếu bạn là người tạo công thức
- Màn hình chỉnh sửa sẽ mở với dữ liệu hiện tại

**Bước 3:** Chỉnh sửa thông tin
- Thay đổi tên, mô tả, nguyên liệu, các bước nấu
- Thay đổi hình ảnh (nếu muốn)
- Thay đổi mã QR (nếu muốn)
  - Nhấn vào vùng chọn mã QR
  - Chọn ảnh mã QR mới từ gallery

**Bước 4:** Nhấn "Cập nhật"
- Công thức sẽ được cập nhật trong Firebase
- Mã QR mới sẽ được upload (nếu có)
- Màn hình chi tiết sẽ tự động refresh

#### 2. Xóa công thức:

**Bước 1:** Mở công thức bạn đã tạo
- **Lưu ý:** Chỉ công thức bạn tạo mới có nút "Xóa"

**Bước 2:** Nhấn nút "Xóa" (icon delete)
- Dialog xác nhận sẽ hiển thị

**Bước 3:** Xác nhận xóa
- Nhấn "Xóa" trong dialog
- Hoặc nhấn "Hủy" để hủy bỏ

**Bước 4:** Công thức sẽ bị xóa
- Công thức bị xóa khỏi Firebase Database
- Mã QR bị xóa khỏi Firebase Storage (nếu có)
- Màn hình sẽ tự động đóng

## 🎯 Chức năng 2: Chia sẻ công thức với mã QR

### Cách sử dụng:

#### 1. Tạo công thức với mã QR riêng:

**Bước 1:** Tạo mã QR bằng ứng dụng tạo QR code
- Mở ứng dụng tạo QR code (ví dụ: get-qr.com)
- Tạo mã QR với nội dung: `prmrecipe://recipe/{recipeId}`
  - **Lưu ý:** Recipe ID sẽ được tạo sau khi lưu công thức
  - Hoặc tạo mã QR sau khi đã có Recipe ID
- Tải mã QR về điện thoại

**Bước 2:** Tạo công thức mới
- Mở ứng dụng → "Tạo công thức mới"
- Điền thông tin công thức
- Trong phần "Mã QR Code (Tùy chọn)":
  - Nhấn "Nhấn để chọn mã QR"
  - Chọn ảnh mã QR đã tải về
  - Preview sẽ hiển thị

**Bước 3:** Nhấn "Lưu"
- Mã QR sẽ được upload lên Firebase Storage
- URL mã QR sẽ được lưu vào công thức

#### 2. Chia sẻ công thức:

**Bước 1:** Mở công thức
- Vào danh sách công thức
- Chọn công thức muốn chia sẻ

**Bước 2:** Nhấn nút "Chia sẻ" (icon share)
- Dialog chia sẻ sẽ hiển thị:
  - Tên công thức
  - Mã QR (đã lưu hoặc tự động tạo)
  - Link chia sẻ: `prmrecipe://recipe/{recipeId}`
  - Nút "Copy link"
  - Nút "Chia sẻ qua ứng dụng"

**Bước 3:** Chia sẻ
- **Copy link:** Nhấn nút copy → Paste vào bất kỳ đâu
- **Chia sẻ qua ứng dụng:** Nhấn "Chia sẻ qua ứng dụng" → Chọn SMS, Email, WhatsApp, etc.
- **Chia sẻ mã QR:** Chụp màn hình mã QR → Gửi ảnh cho người khác

#### 3. Người nhận mở công thức:

**Cách 1: Quét mã QR**
- Mở ảnh mã QR
- Quét bằng ứng dụng quét QR
- Click vào link sau khi quét
- Chọn "Mở bằng PRM_G3"
- Ứng dụng sẽ mở công thức

**Cách 2: Click link**
- Click vào link `prmrecipe://recipe/{recipeId}`
- Chọn "Mở bằng PRM_G3"
- Ứng dụng sẽ mở công thức

## 📋 Checklist sử dụng

### Chỉnh sửa công thức:
- [ ] Đảm bảo bạn là người tạo công thức
- [ ] Nhấn nút "Chỉnh sửa"
- [ ] Chỉnh sửa thông tin cần thiết
- [ ] Có thể thay đổi mã QR (nếu muốn)
- [ ] Nhấn "Cập nhật"
- [ ] Kiểm tra công thức đã được cập nhật

### Xóa công thức:
- [ ] Đảm bảo bạn là người tạo công thức
- [ ] Nhấn nút "Xóa"
- [ ] Xác nhận xóa trong dialog
- [ ] Kiểm tra công thức đã bị xóa

### Chia sẻ công thức:
- [ ] Tạo mã QR riêng (tùy chọn)
- [ ] Upload mã QR khi tạo/chỉnh sửa công thức
- [ ] Nhấn nút "Chia sẻ"
- [ ] Chọn cách chia sẻ (copy link, chia sẻ qua app, hoặc mã QR)
- [ ] Người nhận quét mã QR/click link
- [ ] Kiểm tra công thức đã mở đúng

## ✅ Kết quả

**2 chức năng đã hoàn thiện:**

1. ✅ **Chỉnh sửa và Xóa công thức**
   - Chỉ người tạo mới thấy nút chỉnh sửa/xóa
   - Có thể chỉnh sửa tất cả thông tin
   - Có thể thay đổi mã QR
   - Xóa công thức và mã QR

2. ✅ **Chia sẻ công thức với mã QR**
   - Upload và lưu mã QR riêng cho từng công thức
   - Hiển thị mã QR đã lưu khi chia sẻ
   - Tự động tạo mã QR nếu chưa có
   - Deep link hoạt động đúng

Tất cả đã sẵn sàng sử dụng! 🎉

