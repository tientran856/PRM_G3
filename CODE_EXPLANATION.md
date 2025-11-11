# Giải Thích Chi Tiết Các Đoạn Code Quan Trọng

## 📝 Mục Lục
1. [MainActivity - Load Recipes](#mainactivity---load-recipes)
2. [RecipeDetailActivity - Submit Comment](#recipedetailactivity---submit-comment)
3. [NotificationHelper - Show Notification](#notificationhelper---show-notification)
4. [FavoritesManager - Toggle Favorite](#favoritesmanager---toggle-favorite)
5. [RecipeDetailActivity - Deep Link Handling](#recipedetailactivity---deep-link-handling)
6. [RecipesListActivity - Apply Filters](#recipeslistactivity---apply-filters)
7. [CommentAdapter - Edit/Delete Comment](#commentadapter---editdelete-comment)
8. [ShareRecipeDialog - Generate QR Code](#sharerecipedialog---generate-qr-code)
9. [UserManager - Singleton Pattern](#usermanager---singleton-pattern)
10. [AuthActivity - Register/Login](#authactivity---registerlogin)

---

## 1. MainActivity - Load Recipes

### Code:
```java
private void loadRecipes() {
    recipesRef.addValueEventListener(new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            allRecipes.clear();
            allRecipeIds.clear();

            for (DataSnapshot data : snapshot.getChildren()) {
                try {
                    Recipe r = data.getValue(Recipe.class);
                    if (r != null) {
                        allRecipes.add(r);
                        allRecipeIds.add(data.getKey());
                    }
                } catch (Exception e) {
                    Log.e("MainActivity", "Error parsing recipe: " + data.getKey(), e);
                }
            }

            // Extract unique categories
            Set<String> categoriesSet = new HashSet<>();
            for (Recipe recipe : allRecipes) {
                if (recipe.category != null && !recipe.category.trim().isEmpty()) {
                    categoriesSet.add(recipe.category.trim());
                }
            }

            // Update category filters
            updateCategoryFilters(new ArrayList<>(categoriesSet));

            // Apply category filter
            applyCategoryFilter();
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {
            Toast.makeText(MainActivity.this, "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show();
        }
    });
}
```

### Giải Thích:
1. **addValueEventListener**: Lắng nghe thay đổi real-time trong Firebase
2. **onDataChange**: Được gọi mỗi khi có thay đổi trong database
3. **DataSnapshot**: Chứa toàn bộ dữ liệu tại path `recipes/`
4. **getValue(Recipe.class)**: Parse JSON từ Firebase thành Java object
5. **data.getKey()**: Lấy ID của recipe (key trong Firebase)
6. **Try-catch**: Xử lý lỗi khi parse một recipe bị lỗi, không làm crash app
7. **Extract categories**: Tạo Set để loại bỏ category trùng lặp
8. **updateCategoryFilters**: Tạo các button filter động dựa trên categories có sẵn
9. **applyCategoryFilter**: Áp dụng filter và chia thành Featured/Popular

### Ý Nghĩa:
- Load tất cả recipes từ Firebase một lần
- Tự động cập nhật khi có recipe mới/xóa/sửa
- Tạo UI filter động dựa trên dữ liệu thực tế
- Xử lý lỗi gracefully để không crash app

---

## 2. RecipeDetailActivity - Submit Comment

### Code:
```java
private void submitComment(String comment, int rating) {
    String currentUserId = UserManager.getInstance().getCurrentUserId();
    if (currentUserId == null) {
        currentUserId = "user_002"; // Fallback
    }

    Map<String, Object> commentData = new HashMap<>();
    commentData.put("user_id", currentUserId);
    commentData.put("content", comment);
    commentData.put("rating", rating);
    commentData.put("created_at", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", 
        Locale.getDefault()).format(new Date()));
    commentData.put("sync_status", 1);

    DatabaseReference usersRef = FirebaseDatabase.getInstance()
        .getReference("users").child(currentUserId);
    
    usersRef.child("name").addListenerForSingleValueEvent(new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            String userName = snapshot.getValue(String.class);
            if (userName == null) userName = "Người dùng";

            commentData.put("user_name", userName);

            String commentId = recipeRef.child("comments").push().getKey();
            if (commentId != null) {
                recipeRef.child("comments").child(commentId).setValue(commentData)
                    .addOnSuccessListener(aVoid -> {
                        updateRecipeRating();
                        Toast.makeText(RecipeDetailActivity.this, "Đã gửi đánh giá", 
                            Toast.LENGTH_SHORT).show();
                        edtComment.setText("");
                        setRating(0);

                        // Send notification to recipe author
                        if (currentAuthorId != null && !currentAuthorId.isEmpty() &&
                            !currentAuthorId.equals(finalCurrentUserId)) {
                            notificationHelper.showCommentNotification(
                                recipeId, recipeTitle, userName, comment, currentAuthorId);
                        }

                        loadRecipeDetail();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(RecipeDetailActivity.this, "Lỗi: " + e.getMessage(), 
                            Toast.LENGTH_SHORT).show();
                    });
            }
        }
    });
}
```

### Giải Thích:
1. **getCurrentUserId()**: Lấy ID của user hiện tại từ UserManager
2. **Fallback user**: Nếu không có user, dùng user mặc định
3. **commentData Map**: Tạo object chứa dữ liệu comment
4. **SimpleDateFormat**: Format thời gian theo ISO format
5. **addListenerForSingleValueEvent**: Lấy user name một lần (không lắng nghe real-time)
6. **push().getKey()**: Tạo unique key cho comment mới
7. **setValue()**: Lưu comment vào Firebase
8. **addOnSuccessListener**: Xử lý khi lưu thành công
9. **updateRecipeRating()**: Tính lại rating trung bình
10. **showCommentNotification()**: Gửi thông báo cho author (nếu không phải chính họ)
11. **loadRecipeDetail()**: Reload để hiển thị comment mới

### Ý Nghĩa:
- Lưu comment với đầy đủ thông tin (user_id, user_name, content, rating)
- Tự động cập nhật rating trung bình của recipe
- Gửi thông báo real-time cho author
- Xử lý lỗi và hiển thị thông báo cho user

---

## 3. NotificationHelper - Show Notification

### Code:
```java
public void showCommentNotification(String recipeId, String recipeTitle, 
        String commenterName, String commentContent, String userId) {
    try {
        // Save notification to Firebase
        if (userId != null && !userId.isEmpty()) {
            saveNotificationToFirebase(userId, recipeId, recipeTitle, 
                commenterName, commentContent);
        }

        // Create Intent to open RecipeDetailActivity
        Intent intent = new Intent(context, RecipeDetailActivity.class);
        intent.putExtra("recipeId", recipeId);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
            context, 0, intent, 
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Create notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Bình luận mới")
            .setContentText(commenterName + " đã bình luận vào công thức: " + recipeTitle)
            .setStyle(new NotificationCompat.BigTextStyle()
                .bigText(commenterName + " đã bình luận: \"" + 
                    (commentContent.length() > 100 ? commentContent.substring(0, 100) + "..." 
                        : commentContent) + "\" vào công thức \"" + recipeTitle + "\""))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_SOUND | NotificationCompat.DEFAULT_VIBRATE)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);

        // Show notification
        if (notificationManager != null) {
            notificationManager.notify(NOTIFICATION_ID + recipeId.hashCode(), builder.build());
        }
    } catch (Exception e) {
        Log.e("NotificationHelper", "Error showing notification: " + e.getMessage(), e);
    }
}
```

### Giải Thích:
1. **saveNotificationToFirebase()**: Lưu notification vào database để hiển thị trong app
2. **PendingIntent**: Intent sẽ được thực thi khi user click vào notification
3. **FLAG_ACTIVITY_NEW_TASK**: Tạo task mới để mở activity
4. **NotificationCompat.Builder**: Tạo notification với các thuộc tính
5. **setSmallIcon**: Icon hiển thị trong status bar
6. **setContentTitle/Text**: Tiêu đề và nội dung notification
7. **BigTextStyle**: Hiển thị nội dung dài hơn khi mở rộng
8. **setPriority**: Độ ưu tiên (HIGH để hiển thị ngay)
9. **setAutoCancel**: Tự động đóng khi click
10. **setDefaults**: Âm thanh và rung mặc định
11. **notify()**: Hiển thị notification với unique ID

### Ý Nghĩa:
- Tạo system notification để thông báo cho user
- Lưu notification vào Firebase để hiển thị trong app
- Click vào notification sẽ mở RecipeDetailActivity
- Hỗ trợ Android 8+ với notification channel

---

## 4. FavoritesManager - Toggle Favorite

### Code:
```java
public void toggleFavorite(String recipeId) {
    Log.d(TAG, "Toggling favorite for recipe: " + recipeId);
    if (isFavorite(recipeId)) {
        removeFromFavorites(recipeId);
    } else {
        addToFavorites(recipeId);
    }
}

public void addToFavorites(String recipeId) {
    if (currentUserId == null) {
        Log.w(TAG, "No current user ID, cannot add to favorites");
        return;
    }

    if (isFavorite(recipeId)) {
        Log.d(TAG, "Recipe already in favorites: " + recipeId);
        return;
    }

    String favoriteId = favoritesRef.push().getKey();
    if (favoriteId != null && currentUserId != null) {
        Map<String, Object> favoriteData = new HashMap<>();
        favoriteData.put("user_id", currentUserId);
        favoriteData.put("recipe_id", recipeId);
        favoriteData.put("created_at", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", 
            Locale.getDefault()).format(new Date()));
        favoriteData.put("sync_status", 1);

        favoritesRef.child(favoriteId).setValue(favoriteData)
            .addOnSuccessListener(aVoid -> {
                cachedFavorites.add(recipeId);
                Log.d(TAG, "Successfully added to favorites: " + recipeId);
            })
            .addOnFailureListener(e -> 
                Log.e(TAG, "Failed to add to favorites: " + e.getMessage()));
    }
}

public boolean isFavorite(String recipeId) {
    return currentUserId != null && cachedFavorites.contains(recipeId);
}
```

### Giải Thích:
1. **toggleFavorite()**: Chuyển đổi trạng thái favorite
2. **isFavorite()**: Kiểm tra trong cache (HashSet) - O(1) complexity
3. **cachedFavorites**: HashSet để cache favorites, truy vấn nhanh
4. **push().getKey()**: Tạo unique key cho favorite record
5. **setValue()**: Lưu vào Firebase
6. **addOnSuccessListener**: Cập nhật cache khi thành công
7. **cachedFavorites.add()**: Thêm vào cache để truy vấn nhanh

### Ý Nghĩa:
- Sử dụng cache (HashSet) để truy vấn nhanh O(1)
- Đồng bộ với Firebase để persist data
- Tránh duplicate bằng cách kiểm tra trước khi thêm
- Real-time sync với ValueEventListener

---

## 5. RecipeDetailActivity - Deep Link Handling

### Code:
```java
// Get recipe ID from intent extra or deep link
recipeId = getIntent().getStringExtra("recipeId");

// Handle deep link
if (recipeId == null) {
    Intent intent = getIntent();
    Uri data = intent.getData();
    
    if (data != null) {
        // Check if it's our deep link scheme
        if ("prmrecipe".equals(data.getScheme()) && "recipe".equals(data.getHost())) {
            String path = data.getPath();
            
            if (path != null && !path.isEmpty()) {
                // Remove leading slash if present
                if (path.startsWith("/")) {
                    recipeId = path.substring(1);
                } else {
                    recipeId = path;
                }
            } else {
                // Try to get from last path segment
                String lastSegment = data.getLastPathSegment();
                if (lastSegment != null) {
                    recipeId = lastSegment;
                }
            }
        }
    }
}

if (recipeId == null || recipeId.isEmpty()) {
    Toast.makeText(this, "Không tìm thấy công thức", Toast.LENGTH_LONG).show();
    finish();
    return;
}
```

### Giải Thích:
1. **getStringExtra("recipeId")**: Lấy recipeId từ Intent extra (nếu mở từ trong app)
2. **getIntent().getData()**: Lấy URI từ deep link
3. **getScheme()**: Lấy scheme (prmrecipe://)
4. **getHost()**: Lấy host (recipe)
5. **getPath()**: Lấy path (/recipeId)
6. **substring(1)**: Bỏ dấu "/" đầu tiên
7. **getLastPathSegment()**: Lấy segment cuối cùng nếu path rỗng
8. **Validation**: Kiểm tra recipeId hợp lệ trước khi tiếp tục

### Ý Nghĩa:
- Hỗ trợ mở từ trong app (Intent extra) và từ deep link (URI)
- Parse URI để lấy recipeId
- Xử lý các trường hợp edge case (path có/không có "/")
- Validation để tránh crash

---

## 6. RecipesListActivity - Apply Filters

### Code:
```java
private void applyFilters() {
    filteredList.clear();
    filteredIds.clear();

    String searchQuery = edtSearch.getText().toString().trim().toLowerCase();

    for (int i = 0; i < recipeList.size(); i++) {
        Recipe recipe = recipeList.get(i);
        boolean matches = true;

        // Filter by author_id
        if (filterByAuthorId != null && !filterByAuthorId.isEmpty()) {
            if (recipe.author_id == null || !recipe.author_id.equals(filterByAuthorId)) {
                matches = false;
            }
        }

        // Filter by search query
        if (matches && !searchQuery.isEmpty()) {
            if (recipe.title == null || !recipe.title.toLowerCase().contains(searchQuery)) {
                matches = false;
            }
        }

        // Filter by difficulty
        if (matches && !selectedDifficulties.isEmpty()) {
            if (recipe.difficulty == null || !selectedDifficulties.contains(recipe.difficulty)) {
                matches = false;
            }
        }

        // Filter by category
        if (matches && !selectedCategories.isEmpty()) {
            if (recipe.category == null || !selectedCategories.contains(recipe.category.trim())) {
                matches = false;
            }
        }

        if (matches) {
            filteredList.add(recipe);
            filteredIds.add(recipeIds.get(i));
        }
    }

    // Update adapter
    updateAdapter();
}
```

### Giải Thích:
1. **clear()**: Xóa danh sách filtered trước khi filter lại
2. **toLowerCase()**: Chuyển search query thành chữ thường để so sánh không phân biệt hoa thường
3. **matches flag**: Đánh dấu recipe có khớp với filters không
4. **Multiple filters**: Áp dụng nhiều filter cùng lúc (AND logic)
5. **Early exit**: Nếu không khớp một filter, bỏ qua các filter còn lại
6. **trim()**: Loại bỏ khoảng trắng thừa
7. **contains()**: Tìm kiếm chuỗi con trong title
8. **updateAdapter()**: Cập nhật RecyclerView với danh sách đã filter

### Ý Nghĩa:
- Filter nhiều điều kiện cùng lúc (search, difficulty, category, author)
- Hiệu quả với danh sách nhỏ (< 1000 items)
- Real-time filter khi user nhập text
- Giữ nguyên thứ tự recipes sau khi filter

---

## 7. CommentAdapter - Edit/Delete Comment

### Code:
```java
private void showPopupMenu(View view, Comment comment, String commentId, int position) {
    PopupMenu popup = new PopupMenu(context, view);
    popup.getMenuInflater().inflate(R.menu.comment_menu, popup.getMenu());

    popup.setOnMenuItemClickListener(item -> {
        int itemId = item.getItemId();
        if (itemId == R.id.action_edit) {
            showEditDialog(comment, commentId, position);
            return true;
        } else if (itemId == R.id.action_delete) {
            showDeleteDialog(commentId, position);
            return true;
        }
        return false;
    });

    popup.show();
}

private void updateComment(String commentId, String newContent, int newRating, int position) {
    DatabaseReference commentRef = FirebaseDatabase.getInstance()
        .getReference("recipes")
        .child(recipeId)
        .child("comments")
        .child(commentId);

    Map<String, Object> updates = new HashMap<>();
    updates.put("content", newContent);
    updates.put("rating", newRating);
    updates.put("created_at", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", 
        Locale.getDefault()).format(new Date()));

    commentRef.updateChildren(updates)
        .addOnSuccessListener(aVoid -> {
            Toast.makeText(context, "Đã cập nhật bình luận", Toast.LENGTH_SHORT).show();

            // Update local data
            Comment comment = comments.get(position);
            comment.content = newContent;
            comment.rating = newRating;
            comment.timestamp = System.currentTimeMillis();
            notifyItemChanged(position);

            if (listener != null) {
                listener.onCommentUpdated();
            }
        })
        .addOnFailureListener(e -> {
            Toast.makeText(context, "Lỗi cập nhật: " + e.getMessage(), 
                Toast.LENGTH_SHORT).show();
        });
}
```

### Giải Thích:
1. **PopupMenu**: Hiển thị menu khi click vào nút menu
2. **inflate()**: Load menu từ XML
3. **setOnMenuItemClickListener**: Xử lý khi click vào menu item
4. **updateChildren()**: Update một phần của object trong Firebase (không cần update toàn bộ)
5. **notifyItemChanged()**: Cập nhật item trong RecyclerView
6. **onCommentUpdated()**: Callback để reload comments và update rating
7. **update local data**: Cập nhật data local để UI phản hồi ngay

### Ý Nghĩa:
- Cho phép user chỉnh sửa/xóa comment của mình
- Update một phần data trong Firebase (không cần ghi đè toàn bộ)
- Cập nhật UI ngay lập tức sau khi update
- Callback để reload comments và tính lại rating

---

## 8. ShareRecipeDialog - Generate QR Code

### Code:
```java
private void generateQRCode() {
    try {
        if (shareLink == null || shareLink.trim().isEmpty()) {
            Toast.makeText(context, "Không có link để tạo mã QR", Toast.LENGTH_SHORT).show();
            imgQRCode.setImageBitmap(null);
            return;
        }

        int width = 800;
        int height = 800;
        Hashtable<EncodeHintType, Object> hints = new Hashtable<>();
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.MARGIN, 1);

        QRCodeWriter writer = new QRCodeWriter();
        BitMatrix bitMatrix = writer.encode(shareLink, BarcodeFormat.QR_CODE, 
            width, height, hints);

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                bitmap.setPixel(x, y, bitMatrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
            }
        }
        imgQRCode.setImageBitmap(bitmap);
    } catch (WriterException e) {
        Toast.makeText(context, "Lỗi tạo mã QR: " + e.getMessage(), 
            Toast.LENGTH_SHORT).show();
    }
}
```

### Giải Thích:
1. **QRCodeWriter**: Library ZXing để tạo QR code
2. **ERROR_CORRECTION.H**: Mức sửa lỗi cao (có thể đọc được ngay cả khi QR code bị mờ)
3. **CHARACTER_SET.UTF-8**: Hỗ trợ ký tự đặc biệt
4. **MARGIN**: Khoảng trắng xung quanh QR code
5. **encode()**: Tạo BitMatrix từ link
6. **BitMatrix**: Ma trận bit đại diện cho QR code
7. **createBitmap()**: Tạo Bitmap từ BitMatrix
8. **setPixel()**: Set từng pixel (đen hoặc trắng)
9. **setImageBitmap()**: Hiển thị QR code trong ImageView

### Ý Nghĩa:
- Tạo QR code từ share link
- Hỗ trợ error correction cao để dễ đọc
- Hiển thị trong dialog để user có thể scan
- Xử lý lỗi khi tạo QR code thất bại

---

## 9. UserManager - Singleton Pattern

### Code:
```java
public class UserManager {
    private static UserManager instance;
    private User currentUser;
    private FirebaseAuth mAuth;

    private UserManager() {
        mAuth = FirebaseAuth.getInstance();
    }

    public static UserManager getInstance() {
        if (instance == null) {
            instance = new UserManager();
        }
        return instance;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    public String getCurrentUserId() {
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        return firebaseUser != null ? firebaseUser.getUid() : null;
    }

    public boolean isLoggedIn() {
        return mAuth.getCurrentUser() != null;
    }
}
```

### Giải Thích:
1. **Singleton Pattern**: Đảm bảo chỉ có một instance của UserManager
2. **private constructor**: Ngăn tạo instance từ bên ngoài
3. **getInstance()**: Lấy instance duy nhất (lazy initialization)
4. **currentUser**: Cache user hiện tại để truy cập nhanh
5. **FirebaseAuth**: Lấy user ID từ Firebase Auth
6. **isLoggedIn()**: Kiểm tra user đã đăng nhập chưa

### Ý Nghĩa:
- Đảm bảo chỉ có một instance quản lý user
- Cache user để truy cập nhanh, không cần query Firebase mỗi lần
- Cung cấp các method tiện ích để lấy thông tin user
- Thread-safe với lazy initialization

---

## 10. AuthActivity - Register/Login

### Code:
```java
private void login() {
    String email = getText(binding.etEmail);
    String pass = getText(binding.etPassword);
    if (!validate(email, pass, null)) return;

    mAuth.signInWithEmailAndPassword(email, pass)
        .addOnSuccessListener(authResult -> {
            FirebaseUser fUser = authResult.getUser();
            if (fUser != null) {
                loadUserData(fUser.getUid());
            }
        })
        .addOnFailureListener(e -> toast("Sai email hoặc mật khẩu"));
}

private void register() {
    String email = getText(binding.etEmail);
    String pass = getText(binding.etPassword);
    String name = getText(binding.etName);
    if (!validate(email, pass, name)) return;

    mAuth.createUserWithEmailAndPassword(email, pass)
        .addOnSuccessListener(authResult -> {
            FirebaseUser fUser = authResult.getUser();
            if (fUser != null) {
                saveUserToFirestore(fUser, name);
                toast("Đăng ký thành công!");
                goToMain();
            }
        })
        .addOnFailureListener(e -> toast(e.getMessage()));
}

private void saveUserToFirestore(FirebaseUser fUser, String name) {
    User user = new User(fUser.getUid(), name, fUser.getEmail());
    user.setJoined_at(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", 
        Locale.getDefault()).format(new Date()));
    user.setBio("Người dùng mới tham gia");

    DatabaseReference usersRef = FirebaseDatabase.getInstance()
        .getReference("users");
    usersRef.child(fUser.getUid()).setValue(user)
        .addOnSuccessListener(aVoid -> {
            UserManager.getInstance().setCurrentUser(user);
        })
        .addOnFailureListener(e -> {
            toast("Lỗi lưu thông tin người dùng");
        });
}
```

### Giải Thích:
1. **signInWithEmailAndPassword()**: Đăng nhập với email/password
2. **createUserWithEmailAndPassword()**: Tạo tài khoản mới
3. **addOnSuccessListener**: Xử lý khi thành công
4. **addOnFailureListener**: Xử lý khi thất bại
5. **loadUserData()**: Load thông tin user từ Firebase Database
6. **saveUserToFirestore()**: Lưu thông tin user vào Database (sau khi đăng ký)
7. **setCurrentUser()**: Lưu user vào UserManager để sử dụng trong app
8. **goToMain()**: Chuyển đến MainActivity sau khi đăng nhập/đăng ký thành công

### Ý Nghĩa:
- Xác thực user với Firebase Authentication
- Lưu thông tin user vào Database sau khi đăng ký
- Load thông tin user sau khi đăng nhập
- Quản lý session với UserManager
- Xử lý lỗi và hiển thị thông báo cho user

---

## 📚 Tóm Tắt

Các đoạn code trên thể hiện:
1. ✅ **Real-time sync** với Firebase ValueEventListener
2. ✅ **Error handling** với try-catch và callbacks
3. ✅ **Caching** để truy vấn nhanh (FavoritesManager, UserManager)
4. ✅ **UI updates** sau khi thao tác (notifyDataSetChanged)
5. ✅ **Deep link handling** để mở từ link bên ngoài
6. ✅ **Notification system** để thông báo real-time
7. ✅ **Filter và search** để tìm kiếm recipes
8. ✅ **QR code generation** để chia sẻ recipes
9. ✅ **Singleton pattern** để quản lý user
10. ✅ **Authentication** với Firebase Auth

Tất cả các đoạn code đều tuân theo best practices:
- Separation of concerns
- Error handling
- User feedback (Toast messages)
- Logging để debug
- Validation input
- Thread-safe operations

