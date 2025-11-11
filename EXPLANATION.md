# Giải Thích Luồng Code và Kiến Trúc Dự Án PRM_G3

## 📋 Tổng Quan Dự Án

**PRM_G3** là một ứng dụng Android quản lý công thức nấu ăn (Recipe Management) được xây dựng bằng Java và Firebase. Ứng dụng cho phép người dùng:
- Xem, tìm kiếm và lọc công thức nấu ăn
- Tạo và chia sẻ công thức
- Đánh giá và bình luận công thức
- Lưu công thức yêu thích
- Lập kế hoạch bữa ăn
- Nhận thông báo khi có người bình luận vào công thức của mình

---

## 🏗️ Kiến Trúc và Cấu Trúc Dự Án

### Cấu Trúc Thư Mục

```
PRM_G3/
├── activity/           # Các Activity (màn hình) chính
├── adapters/          # RecyclerView Adapters
├── models/            # Data Models
├── utils/             # Utilities và helpers
└── res/               # Resources (layouts, drawables, etc.)
```

### Các Thành Phần Chính

1. **Activities**: Các màn hình của ứng dụng
2. **Adapters**: Quản lý hiển thị danh sách (RecyclerView)
3. **Models**: Định nghĩa cấu trúc dữ liệu
4. **Managers**: Quản lý logic nghiệp vụ (UserManager, FavoritesManager)
5. **Helpers**: Các lớp hỗ trợ (NotificationHelper, RecipeLinkManager)

---

## 🔄 Luồng Hoạt Động Chính

### 1. Luồng Khởi Động Ứng Dụng

```
App Start
    ↓
AndroidManifest.xml (MainActivity là LAUNCHER)
    ↓
MainActivity.onCreate()
    ↓
Kiểm tra FirebaseAuth.getCurrentUser()
    ├─→ NULL → Chuyển đến AuthActivity
    └─→ Có user → Tiếp tục load dữ liệu
    ↓
Load recipes từ Firebase
Load user info từ Firebase
Setup UI (search, categories, bottom nav)
```

**Code liên quan:**
- `MainActivity.java` (lines 62-109)
- `AuthActivity.java` (lines 24-36)

### 2. Luồng Xác Thực (Authentication)

```
AuthActivity
    ↓
User chọn Login/Register
    ↓
├─→ Login: signInWithEmailAndPassword()
└─→ Register: createUserWithEmailAndPassword()
    ↓
Firebase Auth xác thực
    ↓
Lưu user vào Firebase Database (nếu register)
    ↓
Load user data từ Firebase
    ↓
Lưu vào UserManager.getInstance()
    ↓
Chuyển đến MainActivity
```

**Code liên quan:**
- `AuthActivity.java` (lines 65-157)
- `UserManager.java` (quản lý user hiện tại)

### 3. Luồng Hiển Thị Danh Sách Công Thức

```
MainActivity.onCreate()
    ↓
loadRecipes() - Firebase ValueEventListener
    ↓
Firebase Database: recipes/
    ↓
Parse DataSnapshot → Recipe objects
    ↓
Extract categories từ recipes
    ↓
Update category filter buttons
    ↓
applyCategoryFilter()
    ├─→ Featured recipes (top 3 rating cao nhất)
    └─→ Popular recipes (còn lại)
    ↓
Display trong RecyclerView
    ├─→ RecipeAdapter (LinearLayout - featured)
    └─→ RecipeGridAdapter (GridLayout - popular)
```

**Code liên quan:**
- `MainActivity.java` (lines 158-334)
- `RecipeAdapter.java`
- `RecipeGridAdapter.java`

### 4. Luồng Xem Chi Tiết Công Thức

```
User click vào recipe
    ↓
RecipeDetailActivity được mở với recipeId
    ↓
loadRecipeDetail() - Firebase snapshot
    ↓
Load recipe data từ Firebase
    ├─→ Basic info (title, description, rating, etc.)
    ├─→ Ingredients
    ├─→ Steps
    ├─→ Comments
    └─→ Author info
    ↓
Display trong UI
    ├─→ Tab Ingredients
    ├─→ Tab Steps
    └─→ Tab Comments
```

**Code liên quan:**
- `RecipeDetailActivity.java` (lines 300-514)

### 5. Luồng Bình Luận và Đánh Giá

```
User nhập comment và chọn rating
    ↓
submitComment()
    ↓
Lấy currentUserId từ UserManager
    ↓
Tạo comment data:
    ├─→ user_id
    ├─→ content
    ├─→ rating
    ├─→ created_at
    └─→ user_name
    ↓
Lưu vào Firebase: recipes/{recipeId}/comments/{commentId}
    ↓
updateRecipeRating() - Tính lại rating trung bình
    ↓
Gửi notification cho author (nếu không phải chính họ)
    ↓
Reload comments để hiển thị comment mới
```

**Code liên quan:**
- `RecipeDetailActivity.java` (lines 516-616, 685-719)
- `CommentAdapter.java` (quản lý hiển thị comments)

### 6. Luồng Thông Báo (Notifications)

```
User A bình luận vào recipe của User B
    ↓
RecipeDetailActivity.submitComment()
    ↓
Kiểm tra: currentAuthorId != currentUserId
    ↓
NotificationHelper.showCommentNotification()
    ↓
Tạo Notification object
    ↓
Lưu vào Firebase: notifications/{notificationId}
    ↓
Hiển thị system notification (Android Notification)
    ↓
User B click vào notification
    ↓
Mở RecipeDetailActivity với recipeId
```

**Code liên quan:**
- `NotificationHelper.java` (lines 51-122)
- `NotificationsActivity.java` (hiển thị danh sách notifications)
- `RecipeDetailActivity.java` (lines 558-568, 843-914)

### 7. Luồng Yêu Thích (Favorites)

```
User click vào nút Favorite
    ↓
FavoritesManager.toggleFavorite(recipeId)
    ↓
Kiểm tra isFavorite(recipeId)
    ├─→ True → removeFromFavorites()
    └─→ False → addToFavorites()
    ↓
Firebase Database: favorites/{favoriteId}
    ├─→ user_id
    ├─→ recipe_id
    └─→ created_at
    ↓
Update cachedFavorites (HashSet)
    ↓
Update UI (button state)
```

**Code liên quan:**
- `FavoritesManager.java` (lines 83-162)
- `RecipeAdapter.java` (lines 115-128)

### 8. Luồng Tìm Kiếm và Lọc

```
User nhập text vào search bar
    ↓
RecipesListActivity.applyFilters()
    ↓
Filter recipes theo:
    ├─→ Search query (title)
    ├─→ Difficulty (Dễ, Trung bình, Khó)
    ├─→ Category
    └─→ Author (nếu filter by author)
    ↓
Update filteredList
    ↓
RecipeGridAdapter.notifyDataSetChanged()
```

**Code liên quan:**
- `RecipesListActivity.java` (lines 247-311, 370-464)

### 9. Luồng Chia Sẻ Công Thức

```
User click vào nút Share
    ↓
ShareRecipeDialog được hiển thị
    ↓
resolveShareLink()
    ├─→ Kiểm tra RecipeLinkManager (hardcoded links)
    └─→ Fallback: prmrecipe://recipe/{recipeId}
    ↓
Generate QR Code từ shareLink
    ↓
User có thể:
    ├─→ Copy link
    └─→ Share via other apps
```

**Code liên quan:**
- `ShareRecipeDialog.java`
- `RecipeLinkManager.java` (quản lý hardcoded links)

### 10. Luồng Deep Link

```
User click vào link: prmrecipe://recipe/{recipeId}
    ↓
Android System xử lý deep link
    ↓
AndroidManifest.xml intent-filter
    ↓
RecipeDetailActivity được mở với Intent data
    ↓
Parse URI để lấy recipeId
    ↓
Load recipe detail như bình thường
```

**Code liên quan:**
- `AndroidManifest.xml` (lines 19-26)
- `RecipeDetailActivity.java` (lines 78-124)

---

## 📊 Data Models

### 1. Recipe Model
```java
public class Recipe {
    String title, description, category, tags;
    String image_url, video_url;
    String difficulty;
    double rating;
    int prep_time, cook_time, servings;
    String author_id;
    Map<String, Ingredient> ingredients;
    Map<String, Step> steps;
}
```

**Ý nghĩa:**
- Lưu trữ thông tin công thức nấu ăn
- Ingredients và Steps là nested objects trong Firebase
- Rating được tính từ comments

### 2. User Model
```java
public class User {
    String id, name, email;
    String avatar_url, bio;
    String joined_at;
    int sync_status;
}
```

**Ý nghĩa:**
- Lưu trữ thông tin người dùng
- sync_status để đánh dấu trạng thái đồng bộ

### 3. Notification Model
```java
public class Notification {
    String id, userId, recipeId;
    String recipeTitle, commenterName, commentContent;
    String type; // "comment", "like", etc.
    long timestamp;
    boolean isRead;
}
```

**Ý nghĩa:**
- Lưu trữ thông báo cho người dùng
- type để phân loại thông báo
- isRead để đánh dấu đã đọc

### 4. Comment Model
```java
public class Comment {
    String id, user_id, user_name;
    String content;
    int rating;
    String created_at;
    long timestamp;
}
```

**Ý nghĩa:**
- Lưu trữ bình luận và đánh giá
- rating từ 1-5 sao
- timestamp để sắp xếp và format "time ago"

---

## 🔥 Firebase Integration

### Cấu Trúc Database

```
Firebase Database
├── users/
│   └── {userId}/
│       ├── id, name, email
│       ├── avatar_url, bio
│       └── joined_at
│
├── recipes/
│   └── {recipeId}/
│       ├── title, description, category
│       ├── rating, difficulty
│       ├── ingredients/
│       │   └── {ingredientId}/
│       │       ├── name
│       │       └── quantity
│       ├── steps/
│       │   └── {stepId}/
│       │       ├── step_number
│       │       └── instruction
│       └── comments/
│           └── {commentId}/
│               ├── user_id, user_name
│               ├── content, rating
│               └── created_at
│
├── favorites/
│   └── {favoriteId}/
│       ├── user_id
│       ├── recipe_id
│       └── created_at
│
└── notifications/
    └── {notificationId}/
        ├── userId, recipeId
        ├── recipeTitle, commenterName
        ├── type, timestamp
        └── isRead
```

### Firebase Services Sử Dụng

1. **Firebase Authentication**
   - Email/Password authentication
   - Quản lý session người dùng

2. **Firebase Realtime Database**
   - Lưu trữ recipes, users, comments, favorites, notifications
   - Real-time sync với ValueEventListener

3. **Firebase Storage** (có thể sử dụng cho images)
   - Lưu trữ ảnh công thức và avatar

---

## 🎯 Các Tính Năng Chính

### 1. Hiển Thị Công Thức
- **Featured Recipes**: Top 3 công thức có rating cao nhất (LinearLayout)
- **Popular Recipes**: Các công thức còn lại (GridLayout 2 cột)
- **Category Filter**: Lọc theo danh mục món ăn
- **Search**: Tìm kiếm theo tên công thức

### 2. Quản Lý Công Thức
- Xem chi tiết công thức (ingredients, steps, comments)
- Tạo công thức mới (CreateRecipeActivity)
- Chỉnh sửa/xóa công thức của mình
- Chia sẻ công thức (QR code, link)

### 3. Tương Tác
- Đánh giá và bình luận (1-5 sao)
- Yêu thích công thức
- Xem profile tác giả
- Chỉnh sửa/xóa comment của mình

### 4. Thông Báo
- Nhận thông báo khi có người bình luận vào công thức của mình
- Hiển thị danh sách thông báo
- Đánh dấu đã đọc
- Click vào thông báo để xem công thức

### 5. Quản Lý Người Dùng
- Đăng ký/Đăng nhập
- Xem và chỉnh sửa profile
- Xem công thức của mình
- Xem công thức của người khác

### 6. Kế Hoạch Bữa ăn
- Lập kế hoạch bữa ăn theo ngày (MealPlanActivity)
- Quản lý các bữa ăn trong tuần

---

## 🔐 Quản Lý Quyền và Bảo Mật

### Permissions
- `INTERNET`: Kết nối Firebase
- `POST_NOTIFICATIONS`: Hiển thị thông báo (Android 13+)

### Security Rules (Firebase)
- Users chỉ có thể chỉnh sửa/xóa comment của mình
- Users chỉ có thể xóa công thức của mình
- Authentication required cho các operations

---

## 📱 Navigation Flow

```
MainActivity (Home)
    ├─→ RecipesListActivity (Tất cả công thức)
    ├─→ RecipeDetailActivity (Chi tiết công thức)
    │   ├─→ UserProfileActivity (Profile tác giả)
    │   └─→ ShareRecipeDialog (Chia sẻ)
    ├─→ MealPlanActivity (Kế hoạch bữa ăn)
    ├─→ FavoritesActivity (Yêu thích)
    ├─→ ProfileActivity (Profile của mình)
    │   ├─→ EditProfileActivity (Chỉnh sửa profile)
    │   └─→ UserProfileActivity (Xem công thức của mình)
    └─→ NotificationsActivity (Thông báo)
        └─→ RecipeDetailActivity (Xem công thức từ thông báo)
```

---

## 🛠️ Các Manager và Helper Classes

### 1. UserManager
- **Singleton pattern**: Đảm bảo chỉ có một instance
- **Chức năng**: Quản lý thông tin user hiện tại
- **Methods**: getCurrentUser(), getCurrentUserId(), isLoggedIn()

### 2. FavoritesManager
- **Chức năng**: Quản lý danh sách yêu thích
- **Features**: 
  - Cache favorites trong HashSet để truy vấn nhanh
  - Real-time sync với Firebase
  - Listener để cập nhật UI khi có thay đổi

### 3. NotificationHelper
- **Chức năng**: Tạo và hiển thị thông báo
- **Features**:
  - Tạo notification channel (Android 8+)
  - Lưu notification vào Firebase
  - Hiển thị system notification với PendingIntent

### 4. RecipeLinkManager
- **Chức năng**: Quản lý links chia sẻ
- **Features**:
  - Hardcoded links cho một số công thức demo
  - Fallback về deep link mặc định
  - Normalize và match tên công thức (bỏ dấu, case-insensitive)

---

## 🎨 UI Components

### RecyclerView Adapters
1. **RecipeAdapter**: Hiển thị featured recipes (LinearLayout)
2. **RecipeGridAdapter**: Hiển thị popular recipes (GridLayout)
3. **CommentAdapter**: Hiển thị comments với edit/delete
4. **NotificationAdapter**: Hiển thị danh sách thông báo
5. **DayAdapter**: Hiển thị các ngày trong meal plan
6. **MealCategoryAdapter**: Hiển thị các bữa ăn

### Bottom Navigation
- Home: MainActivity
- Recipes: RecipesListActivity
- Plan: MealPlanActivity
- Favorite: FavoritesActivity
- Profile: ProfileActivity

---

## 🔄 Real-time Updates

### ValueEventListener
- **MainActivity**: Lắng nghe thay đổi trong recipes để cập nhật UI
- **FavoritesManager**: Lắng nghe thay đổi trong favorites
- **NotificationsActivity**: Lắng nghe thay đổi trong notifications
- **RecipeDetailActivity**: Lắng nghe comments mới để gửi notification

### Cơ Chế Hoạt Động
1. Đăng ký ValueEventListener với Firebase
2. Firebase tự động gọi onDataChange() khi có thay đổi
3. Update local data và UI
4. Remove listener khi không cần thiết (onDestroy)

---

## 🐛 Xử Lý Lỗi

### Error Handling
1. **Firebase Errors**: 
   - onCancelled() callback
   - Try-catch khi parse data
   - Toast messages để thông báo lỗi

2. **Null Checks**:
   - Kiểm tra null trước khi sử dụng
   - Fallback values (default user, empty lists)

3. **Network Errors**:
   - Firebase tự động retry
   - Offline support với Firebase persistence

---

## 📝 Best Practices Được Áp Dụng

1. **Singleton Pattern**: UserManager
2. **Adapter Pattern**: RecyclerView Adapters
3. **Observer Pattern**: ValueEventListener
4. **Separation of Concerns**: Managers, Helpers, Models
5. **Error Handling**: Try-catch, null checks
6. **Code Reusability**: Helper classes
7. **Real-time Sync**: Firebase ValueEventListener
8. **Caching**: FavoritesManager cache

---

## 🚀 Cải Tiến Có Thể Thực Hiện

1. **Offline Support**: Sử dụng Firebase persistence
2. **Image Caching**: Glide đã có cache, nhưng có thể tối ưu thêm
3. **Pagination**: Load recipes theo batch thay vì load tất cả
4. **Search Optimization**: Full-text search với Firebase Algolia
5. **Push Notifications**: Firebase Cloud Messaging
6. **Analytics**: Firebase Analytics
7. **Crash Reporting**: Firebase Crashlytics
8. **Testing**: Unit tests, UI tests

---

## 📚 Tóm Tắt

Dự án PRM_G3 là một ứng dụng quản lý công thức nấu ăn với các tính năng:
- ✅ Xem, tìm kiếm, lọc công thức
- ✅ Tạo và chia sẻ công thức
- ✅ Đánh giá và bình luận
- ✅ Yêu thích công thức
- ✅ Thông báo real-time
- ✅ Quản lý profile
- ✅ Kế hoạch bữa ăn

Kiến trúc được tổ chức rõ ràng với separation of concerns, sử dụng Firebase làm backend, và có real-time sync. Code được viết theo best practices với error handling và null safety.

