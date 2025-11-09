# Hướng dẫn Debug Deep Link

## Vấn đề: Link `prmrecipe://recipe/recipe_001` không hoạt động đúng

### Bước 1: Kiểm tra Recipe ID thực tế trong Firebase

**Vấn đề phổ biến**: Recipe ID trong Firebase **KHÔNG PHẢI** là `recipe_001!

Khi tạo công thức mới, Firebase sử dụng `push().getKey()` để tạo ID tự động, ví dụ:
- `-N1234567890`
- `-Nabcdefghij`
- `-Nxyz1234567`

**Cách kiểm tra:**

1. Mở Firebase Console: https://console.firebase.google.com/
2. Vào Realtime Database
3. Xem node `recipes`
4. **Copy đúng Recipe ID** (key của công thức)

### Bước 2: Sử dụng đúng Recipe ID trong link

**SAI:**
```
prmrecipe://recipe/recipe_001
```

**ĐÚNG** (ví dụ):
```
prmrecipe://recipe/-N1234567890
```

### Bước 3: Xem Log để Debug

1. **Mở Android Studio**
2. **Kết nối thiết bị/emulator**
3. **Mở Logcat** (View → Tool Windows → Logcat)
4. **Filter theo tag**: `RecipeDetailActivity`
5. **Chạy deep link** bằng ADB hoặc trình duyệt
6. **Xem log** để biết:
   - Intent data có được nhận không
   - Recipe ID được extract đúng không
   - Recipe có tồn tại trong Firebase không

**Log sẽ hiển thị:**
```
=== DEEP LINK DEBUG ===
Intent action: android.intent.action.VIEW
Intent data (URI): prmrecipe://recipe/-N1234567890
Scheme: prmrecipe
Host: recipe
Path: /-N1234567890
Recipe ID extracted: -N1234567890
Final Recipe ID: -N1234567890
Loading recipe detail for ID: -N1234567890
Firebase path: recipes/-N1234567890
Recipe loaded successfully: Tên công thức
```

### Bước 4: Test với Recipe ID đúng

**Cách 1: Dùng script test**
```bash
# Thay -N1234567890 bằng Recipe ID thực tế từ Firebase
test_deep_link.bat -N1234567890
```

**Cách 2: Dùng ADB trực tiếp**
```bash
adb shell am start -a android.intent.action.VIEW -d "prmrecipe://recipe/-N1234567890"
```

**Cách 3: Dùng trình duyệt**
- Mở Chrome trên điện thoại
- Nhập: `prmrecipe://recipe/-N1234567890`
- Nhấn Enter

### Bước 5: Kiểm tra các lỗi thường gặp

#### Lỗi 1: "Không tìm thấy công thức"
**Nguyên nhân:**
- Recipe ID không đúng
- Recipe không tồn tại trong Firebase
- Recipe ID có ký tự đặc biệt cần encode

**Giải pháp:**
- Kiểm tra lại Recipe ID trong Firebase
- Đảm bảo Recipe ID chính xác (copy trực tiếp từ Firebase)
- Xem log để biết Recipe ID nào đang được sử dụng

#### Lỗi 2: Link không mở ứng dụng
**Nguyên nhân:**
- Intent filter chưa đúng
- Ứng dụng chưa được cài đặt
- Deep link bị chặn bởi hệ thống

**Giải pháp:**
- Kiểm tra `AndroidManifest.xml` có intent filter đúng không
- Uninstall và reinstall ứng dụng
- Thử trên thiết bị khác

#### Lỗi 3: Recipe ID bị null hoặc empty
**Nguyên nhân:**
- URI không được parse đúng
- Path không có trong URI

**Giải pháp:**
- Xem log để kiểm tra URI được nhận
- Đảm bảo link đúng format: `prmrecipe://recipe/{recipeId}`

### Bước 6: Lấy Recipe ID từ ứng dụng

**Cách dễ nhất:**
1. Mở ứng dụng
2. Vào một công thức bất kỳ
3. Nhấn nút chia sẻ
4. **Copy link** từ dialog chia sẻ
5. Link sẽ có format: `prmrecipe://recipe/{recipeId}`
6. **Recipe ID** là phần sau `/recipe/`

### Ví dụ Debug

**Tình huống:**
- Link test: `prmrecipe://recipe/recipe_001`
- Không hoạt động

**Debug:**
1. Mở Firebase Console
2. Xem node `recipes`
3. Tìm công thức tương ứng
4. Copy đúng key (ví dụ: `-N1234567890`)
5. Test lại với link: `prmrecipe://recipe/-N1234567890`

**Kết quả:** Link sẽ hoạt động đúng!

### Lưu ý quan trọng

1. **Recipe ID trong Firebase KHÔNG PHẢI** là `recipe_001`, `recipe_002`...
2. **Recipe ID thực tế** là key được Firebase tự động tạo (như `-N1234567890`)
3. **Luôn copy Recipe ID trực tiếp** từ Firebase Console
4. **Xem log** để biết chính xác Recipe ID nào đang được sử dụng
5. **Deep link phải dùng đúng Recipe ID** từ Firebase

### Checklist Debug

- [ ] Đã kiểm tra Recipe ID trong Firebase Console
- [ ] Đã copy đúng Recipe ID (key của công thức)
- [ ] Đã sử dụng đúng Recipe ID trong link
- [ ] Đã xem log trong Android Studio
- [ ] Đã kiểm tra Recipe có tồn tại trong Firebase
- [ ] Đã test với Recipe ID đúng từ Firebase

