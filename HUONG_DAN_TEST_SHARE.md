# Hướng dẫn kiểm tra chức năng chia sẻ công thức

## 1. Kiểm tra Deep Link (Đường link)

### Cách 1: Sử dụng ADB (Android Debug Bridge)

1. **Kết nối thiết bị/emulator với máy tính**
2. **Mở Command Prompt/Terminal** và chạy lệnh:

```bash
# Kiểm tra thiết bị đã kết nối
adb devices

# Test deep link (thay {recipeId} bằng ID công thức thực tế)
adb shell am start -a android.intent.action.VIEW -d "prmrecipe://recipe/{recipeId}"

# Ví dụ:
adb shell am start -a android.intent.action.VIEW -d "prmrecipe://recipe/-N1234567890"
```

### Cách 2: Sử dụng ứng dụng khác

1. **Mở trình duyệt trên điện thoại** (Chrome, Firefox, etc.)
2. **Nhập link vào thanh địa chỉ**: `prmrecipe://recipe/{recipeId}`
3. **Nhấn Enter** - ứng dụng sẽ tự động mở

### Cách 3: Sử dụng ứng dụng Note/Text Editor

1. **Tạo file text** với nội dung: `prmrecipe://recipe/{recipeId}`
2. **Long press vào link** → Chọn "Mở bằng" → Chọn ứng dụng PRM_G3

### Cách 4: Test từ ứng dụng khác (SMS, Email, etc.)

1. **Gửi link qua SMS hoặc Email** cho chính mình
2. **Click vào link** trong tin nhắn/email
3. **Ứng dụng sẽ tự động mở** công thức tương ứng

## 2. Kiểm tra Mã QR Code

### Cách 1: Sử dụng ứng dụng quét QR trên điện thoại khác

1. **Mở ứng dụng chia sẻ** trong PRM_G3
2. **Chụp màn hình mã QR** hoặc hiển thị trên màn hình lớn
3. **Dùng điện thoại khác** quét mã QR bằng:
   - Camera app (iOS/Android mới)
   - Ứng dụng quét QR (QR Code Reader, Barcode Scanner, etc.)
4. **Click vào link** sau khi quét → Ứng dụng sẽ mở

### Cách 2: Sử dụng ứng dụng quét QR trên cùng điện thoại

1. **Mở ứng dụng chia sẻ** trong PRM_G3
2. **Chụp màn hình mã QR**
3. **Mở ứng dụng quét QR** và chọn ảnh vừa chụp
4. **Click vào link** → Ứng dụng sẽ mở

### Cách 3: Test online với QR Code Generator

1. **Copy link** từ ứng dụng: `prmrecipe://recipe/{recipeId}`
2. **Truy cập**: https://www.qr-code-generator.com/
3. **Paste link** vào ô input
4. **Tải mã QR** và quét bằng điện thoại để kiểm tra

## 3. Kiểm tra Copy Link

1. **Mở dialog chia sẻ** trong ứng dụng
2. **Nhấn nút Copy** (icon copy bên cạnh link)
3. **Mở ứng dụng Note** và paste
4. **Kiểm tra** link đã được copy đúng chưa

## 4. Kiểm tra Chia sẻ qua ứng dụng khác

1. **Mở dialog chia sẻ**
2. **Nhấn "Chia sẻ qua ứng dụng"**
3. **Chọn ứng dụng** (SMS, Email, WhatsApp, Facebook, etc.)
4. **Kiểm tra** nội dung chia sẻ có đúng format:
   ```
   Tên công thức
   
   prmrecipe://recipe/{recipeId}
   ```

## 5. Debug và Log

### Xem log trong Android Studio

1. **Mở Logcat** trong Android Studio
2. **Filter theo tag**: `RecipeDetailActivity` hoặc `ShareRecipeDialog`
3. **Kiểm tra log** khi mở deep link

### Thêm log vào code để debug

Thêm các dòng log sau vào `RecipeDetailActivity.java`:

```java
Log.d("DeepLink", "Intent data: " + intent.getData());
Log.d("DeepLink", "Recipe ID from deep link: " + recipeId);
```

## 6. Kiểm tra Intent Filter

Đảm bảo `AndroidManifest.xml` đã được cấu hình đúng:

```xml
<intent-filter>
    <action android:name="android.intent.action.VIEW" />
    <category android:name="android.intent.category.DEFAULT" />
    <category android:name="android.intent.category.BROWSABLE" />
    <data
        android:scheme="prmrecipe"
        android:host="recipe" />
</intent-filter>
```

## 7. Lưu ý khi test

- **Recipe ID phải tồn tại** trong Firebase Database
- **Ứng dụng phải được cài đặt** trên thiết bị test
- **Deep link chỉ hoạt động** khi ứng dụng đã được cài đặt
- **QR code phải có độ phân giải đủ** để quét được (đã set 800x800)

## 8. Câu hỏi thường gặp

### Khi dữ liệu trên Firebase thay đổi, có cần đổi link và mã QR không?

**TRẢ LỜI: KHÔNG CẦN ĐỔI**

**Lý do:**
1. **Link và QR code dựa trên `recipeId`**: 
   - Link chia sẻ: `prmrecipe://recipe/{recipeId}`
   - Mã QR chứa link này
   - `recipeId` là định danh duy nhất, không thay đổi khi chỉnh sửa nội dung

2. **Khi chỉnh sửa nội dung công thức**:
   - Ví dụ: Đổi tên, mô tả, nguyên liệu, các bước nấu
   - `recipeId` vẫn giữ nguyên (ví dụ: `recipe_001`)
   - Link và QR code cũ vẫn hoạt động bình thường
   - Khi mở link/quét QR, ứng dụng sẽ tự động load dữ liệu mới nhất từ Firebase

3. **Chỉ cần tạo link/QR mới khi**:
   - Xóa công thức cũ và tạo công thức mới (recipeId khác)
   - Tạo công thức hoàn toàn mới

**Ví dụ:**
```
Công thức: recipe_001
Link: prmrecipe://recipe/recipe_001
QR code: chứa link trên

→ Chỉnh sửa tên công thức trên Firebase
→ Link và QR code vẫn là: prmrecipe://recipe/recipe_001
→ Khi mở link, sẽ hiển thị tên mới đã cập nhật
```

## 9. Troubleshooting

### Link không mở ứng dụng
- Kiểm tra lại intent filter trong AndroidManifest
- Đảm bảo `android:exported="true"` cho RecipeDetailActivity
- Thử uninstall và reinstall ứng dụng

### QR code không quét được
- Kiểm tra độ phân giải (đã set 800x800)
- Đảm bảo mã QR hiển thị rõ ràng
- Thử tăng kích thước QR code nếu cần

### Link mở nhưng không hiển thị công thức
- Kiểm tra recipeId có đúng không
- Kiểm tra công thức có tồn tại trong Firebase không
- Xem log để debug

