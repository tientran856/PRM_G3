# Tài Liệu Dự Án PRM_G3 - Hướng Dẫn Đọc

## 📚 Các Tài Liệu Có Sẵn

Dự án này có 4 tài liệu giải thích chi tiết:

### 1. **EXPLANATION.md** - Tổng Quan Dự Án
   - Tổng quan về dự án và kiến trúc
   - Luồng hoạt động chính (10 luồng)
   - Data models và cấu trúc Firebase
   - Các tính năng chính
   - Best practices

### 2. **FLOW_DIAGRAMS.md** - Sơ Đồ Luồng Hoạt Động
   - 15 sơ đồ ASCII art mô tả các luồng
   - Cấu trúc Firebase Database
   - Navigation flow
   - Component interaction
   - Error handling flow

### 3. **CODE_EXPLANATION.md** - Giải Thích Chi Tiết Code
   - 10 đoạn code quan trọng với giải thích chi tiết
   - Ý nghĩa từng dòng code
   - Best practices được áp dụng
   - Error handling

### 4. **README_DOCUMENTATION.md** - File này
   - Hướng dẫn đọc tài liệu
   - Tóm tắt nhanh

---

## 🚀 Bắt Đầu Đọc Tài Liệu

### Nếu bạn là người mới:
1. **Đọc EXPLANATION.md trước** - Hiểu tổng quan về dự án
2. **Xem FLOW_DIAGRAMS.md** - Hiểu các luồng hoạt động
3. **Đọc CODE_EXPLANATION.md** - Hiểu chi tiết code

### Nếu bạn muốn tìm hiểu một tính năng cụ thể:
1. **Tìm trong EXPLANATION.md** - Xem luồng hoạt động của tính năng
2. **Xem FLOW_DIAGRAMS.md** - Xem sơ đồ luồng của tính năng
3. **Đọc CODE_EXPLANATION.md** - Xem code chi tiết

### Nếu bạn muốn debug một vấn đề:
1. **Xem FLOW_DIAGRAMS.md** - Hiểu luồng hoạt động
2. **Đọc CODE_EXPLANATION.md** - Xem error handling
3. **Xem EXPLANATION.md** - Hiểu context

---

## 📋 Tóm Tắt Nhanh Dự Án

### Chức Năng Chính:
- ✅ Xem, tìm kiếm, lọc công thức nấu ăn
- ✅ Tạo và chia sẻ công thức
- ✅ Đánh giá và bình luận (1-5 sao)
- ✅ Yêu thích công thức
- ✅ Thông báo real-time khi có bình luận mới
- ✅ Quản lý profile
- ✅ Kế hoạch bữa ăn

### Công Nghệ Sử Dụng:
- **Language**: Java
- **Framework**: Android SDK
- **Backend**: Firebase (Authentication, Realtime Database)
- **UI**: Material Design, RecyclerView
- **Image Loading**: Glide
- **QR Code**: ZXing

### Cấu Trúc:
```
PRM_G3/
├── activity/        # Các màn hình (MainActivity, RecipeDetailActivity, etc.)
├── adapters/        # RecyclerView Adapters
├── models/          # Data Models (Recipe, User, Comment, etc.)
├── utils/           # Utilities
└── res/             # Resources (layouts, drawables, etc.)
```

### Managers & Helpers:
- **UserManager**: Quản lý user hiện tại (Singleton)
- **FavoritesManager**: Quản lý danh sách yêu thích
- **NotificationHelper**: Tạo và hiển thị thông báo
- **RecipeLinkManager**: Quản lý links chia sẻ

---

## 🔍 Tìm Kiếm Nhanh

### Tìm luồng xác thực:
- **EXPLANATION.md**: Section "Luồng Xác Thực"
- **FLOW_DIAGRAMS.md**: Diagram #2
- **CODE_EXPLANATION.md**: Section #10

### Tìm luồng bình luận:
- **EXPLANATION.md**: Section "Luồng Bình Luận và Đánh Giá"
- **FLOW_DIAGRAMS.md**: Diagram #5
- **CODE_EXPLANATION.md**: Section #2

### Tìm luồng thông báo:
- **EXPLANATION.md**: Section "Luồng Thông Báo"
- **FLOW_DIAGRAMS.md**: Diagram #6
- **CODE_EXPLANATION.md**: Section #3

### Tìm luồng yêu thích:
- **EXPLANATION.md**: Section "Luồng Yêu Thích"
- **FLOW_DIAGRAMS.md**: Diagram #7
- **CODE_EXPLANATION.md**: Section #4

---

## 📖 Các File Code Quan Trọng

### Activities:
- `MainActivity.java` - Màn hình chính, hiển thị recipes
- `RecipeDetailActivity.java` - Chi tiết recipe, comments
- `AuthActivity.java` - Đăng nhập/Đăng ký
- `RecipesListActivity.java` - Danh sách recipes với search/filter
- `NotificationsActivity.java` - Danh sách thông báo
- `ProfileActivity.java` - Profile của user
- `FavoritesActivity.java` - Danh sách yêu thích
- `MealPlanActivity.java` - Kế hoạch bữa ăn

### Managers:
- `UserManager.java` - Quản lý user (Singleton)
- `FavoritesManager.java` - Quản lý favorites với cache
- `NotificationHelper.java` - Tạo notifications
- `RecipeLinkManager.java` - Quản lý share links

### Adapters:
- `RecipeAdapter.java` - Hiển thị featured recipes
- `RecipeGridAdapter.java` - Hiển thị popular recipes (grid)
- `CommentAdapter.java` - Hiển thị comments với edit/delete
- `NotificationAdapter.java` - Hiển thị notifications

### Models:
- `Recipe.java` - Model công thức
- `User.java` - Model người dùng
- `Comment.java` - Model bình luận
- `Notification.java` - Model thông báo

---

## 🎯 Các Tính Năng Đặc Biệt

### 1. Real-time Sync
- Sử dụng Firebase ValueEventListener
- Tự động cập nhật UI khi có thay đổi
- Không cần refresh thủ công

### 2. Deep Link
- Hỗ trợ link: `prmrecipe://recipe/{recipeId}`
- Mở RecipeDetailActivity từ link bên ngoài
- Parse URI để lấy recipeId

### 3. QR Code Sharing
- Tạo QR code từ share link
- Hỗ trợ hardcoded links và deep links
- Copy link hoặc share qua app khác

### 4. Notification System
- Thông báo khi có bình luận mới
- Lưu notification vào Firebase
- Click notification để mở recipe

### 5. Caching
- FavoritesManager cache favorites trong HashSet
- UserManager cache user hiện tại
- Truy vấn nhanh O(1)

---

## 🐛 Debugging Tips

### Kiểm tra Firebase connection:
- Xem log trong `loadRecipes()`, `submitComment()`
- Kiểm tra `onCancelled()` callback
- Xem error messages trong Toast

### Kiểm tra authentication:
- Kiểm tra `UserManager.getInstance().getCurrentUserId()`
- Xem log trong `AuthActivity`
- Kiểm tra Firebase Auth console

### Kiểm tra notifications:
- Kiểm tra permission (Android 13+)
- Xem log trong `NotificationHelper`
- Kiểm tra notification channel

### Kiểm tra deep link:
- Xem log trong `RecipeDetailActivity.onCreate()`
- Kiểm tra URI parsing
- Test với `adb shell am start -a android.intent.action.VIEW -d "prmrecipe://recipe/{recipeId}"`

---

## 📝 Ghi Chú

- Tất cả các file code đều có comment tiếng Việt
- Sử dụng try-catch để xử lý lỗi
- Log messages để debug
- Toast messages để thông báo user
- Validation input trước khi submit

---

## 🔗 Liên Kết Nhanh

- [EXPLANATION.md](./EXPLANATION.md) - Tổng quan dự án
- [FLOW_DIAGRAMS.md](./FLOW_DIAGRAMS.md) - Sơ đồ luồng
- [CODE_EXPLANATION.md](./CODE_EXPLANATION.md) - Giải thích code
- [README_DOCUMENTATION.md](./README_DOCUMENTATION.md) - File này

---

## 📞 Hỗ Trợ

Nếu có thắc mắc về code hoặc cần giải thích thêm, vui lòng:
1. Đọc lại các tài liệu trên
2. Xem comments trong code
3. Kiểm tra log messages
4. Xem Firebase console

---

**Chúc bạn đọc tài liệu hiệu quả! 🎉**

