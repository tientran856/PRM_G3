# Sơ Đồ Luồng Hoạt Động - PRM_G3

## 1. Luồng Khởi Động Ứng Dụng

```
┌─────────────────┐
│   App Start     │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│  MainActivity   │
│   onCreate()    │
└────────┬────────┘
         │
         ▼
┌─────────────────────────┐
│ FirebaseAuth.getCurrent │
│        User()           │
└────────┬────────────────┘
         │
    ┌────┴────┐
    │         │
    ▼         ▼
┌───────┐  ┌──────────────┐
│ NULL  │  │ Has User     │
└───┬───┘  └──────┬───────┘
    │             │
    ▼             ▼
┌───────────┐  ┌──────────────────┐
│AuthActivity│  │ Load Recipes     │
│            │  │ Load User Info   │
│  Login/    │  │ Setup UI         │
│  Register  │  │                  │
└─────┬─────┘  └──────────────────┘
      │
      ▼
┌───────────┐
│MainActivity│
└───────────┘
```

## 2. Luồng Xác Thực

```
┌──────────────────┐
│  AuthActivity    │
└────────┬─────────┘
         │
    ┌────┴────┐
    │         │
    ▼         ▼
┌───────┐  ┌──────────┐
│ Login │  │ Register │
└───┬───┘  └─────┬────┘
    │            │
    ▼            ▼
┌─────────────────────────┐
│ Firebase Authentication  │
│ signInWithEmailAndPass  │
│ createUserWithEmailAnd  │
│         Pass            │
└────────┬────────────────┘
         │
    ┌────┴────┐
    │         │
    ▼         ▼
┌───────┐  ┌──────────────────┐
│Success│  │    Failure       │
└───┬───┘  └──────────────────┘
    │
    ▼
┌──────────────────┐
│ Save User to DB  │
│ (if register)    │
└────────┬─────────┘
         │
         ▼
┌──────────────────┐
│ Load User Data   │
└────────┬─────────┘
         │
         ▼
┌──────────────────┐
│ UserManager.set  │
│  CurrentUser()   │
└────────┬─────────┘
         │
         ▼
┌──────────────────┐
│  MainActivity    │
└──────────────────┘
```

## 3. Luồng Hiển Thị Công Thức

```
┌──────────────────┐
│  MainActivity    │
│  loadRecipes()   │
└────────┬─────────┘
         │
         ▼
┌──────────────────────┐
│ Firebase Database    │
│   recipes/           │
└────────┬─────────────┘
         │
         ▼
┌──────────────────────┐
│ Parse DataSnapshot   │
│   → Recipe objects   │
└────────┬─────────────┘
         │
         ▼
┌──────────────────────┐
│ Extract Categories   │
│ Update Category      │
│   Filter Buttons     │
└────────┬─────────────┘
         │
         ▼
┌──────────────────────┐
│ applyCategoryFilter()│
└────────┬─────────────┘
         │
    ┌────┴────┐
    │         │
    ▼         ▼
┌──────────┐  ┌──────────────┐
│ Featured │  │   Popular    │
│ Recipes  │  │   Recipes    │
│ (Top 3)  │  │   (Rest)     │
└────┬─────┘  └──────┬───────┘
     │               │
     ▼               ▼
┌──────────┐  ┌──────────────┐
│RecipeAdapter│ │RecipeGridAdapter│
│(LinearLayout)│ │(GridLayout)    │
└──────────┘  └──────────────────┘
```

## 4. Luồng Xem Chi Tiết Công Thức

```
┌──────────────────┐
│ User clicks      │
│   recipe item    │
└────────┬─────────┘
         │
         ▼
┌──────────────────┐
│RecipeDetailActivity│
│  onCreate()      │
└────────┬─────────┘
         │
         ▼
┌──────────────────┐
│ Get recipeId     │
│ from Intent      │
└────────┬─────────┘
         │
         ▼
┌──────────────────┐
│ loadRecipeDetail()│
└────────┬─────────┘
         │
         ▼
┌──────────────────────┐
│ Firebase: recipes/   │
│   {recipeId}         │
└────────┬─────────────┘
         │
         ▼
┌──────────────────────┐
│ Load Recipe Data:    │
│ ├─ Basic info        │
│ ├─ Ingredients       │
│ ├─ Steps             │
│ ├─ Comments          │
│ └─ Author info       │
└────────┬─────────────┘
         │
         ▼
┌──────────────────────┐
│ Display in UI:       │
│ ├─ Tab Ingredients   │
│ ├─ Tab Steps         │
│ └─ Tab Comments      │
└──────────────────────┘
```

## 5. Luồng Bình Luận và Đánh Giá

```
┌──────────────────┐
│ User enters      │
│ comment & rating │
└────────┬─────────┘
         │
         ▼
┌──────────────────┐
│ submitComment()  │
└────────┬─────────┘
         │
         ▼
┌──────────────────┐
│ Get currentUserId│
│ from UserManager │
└────────┬─────────┘
         │
         ▼
┌──────────────────┐
│ Create comment   │
│ data object      │
└────────┬─────────┘
         │
         ▼
┌──────────────────────┐
│ Firebase: recipes/   │
│ {recipeId}/comments/ │
│   {commentId}        │
└────────┬─────────────┘
         │
         ▼
┌──────────────────┐
│ updateRecipeRating│
│ Calculate average │
│     rating        │
└────────┬─────────┘
         │
         ▼
┌──────────────────┐
│ Check if author  │
│ != commenter     │
└────────┬─────────┘
         │
    ┌────┴────┐
    │         │
    ▼         ▼
┌───────┐  ┌──────────────────┐
│  Yes  │  │       No          │
└───┬───┘  └──────────────────┘
    │
    ▼
┌──────────────────┐
│ Send Notification│
│ to recipe author │
└────────┬─────────┘
         │
         ▼
┌──────────────────┐
│ Reload comments  │
│ Update UI        │
└──────────────────┘
```

## 6. Luồng Thông Báo

```
┌──────────────────┐
│ User A comments  │
│ on User B's recipe│
└────────┬─────────┘
         │
         ▼
┌──────────────────┐
│ NotificationHelper│
│ showCommentNotif │
└────────┬─────────┘
         │
         ▼
┌──────────────────┐
│ Create Notification│
│     object        │
└────────┬─────────┘
         │
         ▼
┌──────────────────────┐
│ Firebase: notifications/│
│   {notificationId}   │
└────────┬─────────────┘
         │
         ▼
┌──────────────────┐
│ Show System      │
│ Notification     │
│ (Android)        │
└────────┬─────────┘
         │
         ▼
┌──────────────────┐
│ User B clicks    │
│ notification     │
└────────┬─────────┘
         │
         ▼
┌──────────────────┐
│RecipeDetailActivity│
│ with recipeId    │
└──────────────────┘
```

## 7. Luồng Yêu Thích

```
┌──────────────────┐
│ User clicks      │
│ Favorite button  │
└────────┬─────────┘
         │
         ▼
┌──────────────────┐
│FavoritesManager  │
│ toggleFavorite() │
└────────┬─────────┘
         │
    ┌────┴────┐
    │         │
    ▼         ▼
┌───────┐  ┌──────────┐
│Remove │  │   Add    │
└───┬───┘  └─────┬────┘
    │            │
    │            ▼
    │    ┌──────────────┐
    │    │ Firebase:    │
    │    │ favorites/   │
    │    │ {favoriteId} │
    │    └──────┬───────┘
    │           │
    └───────────┘
         │
         ▼
┌──────────────────┐
│ Update cached    │
│ Favorites        │
│ (HashSet)        │
└────────┬─────────┘
         │
         ▼
┌──────────────────┐
│ Update UI        │
│ (button state)   │
└──────────────────┘
```

## 8. Luồng Tìm Kiếm và Lọc

```
┌──────────────────┐
│ User enters      │
│ search query     │
└────────┬─────────┘
         │
         ▼
┌──────────────────┐
│ RecipesListActivity│
│ applyFilters()   │
└────────┬─────────┘
         │
         ▼
┌──────────────────┐
│ Filter recipes:  │
│ ├─ Search query  │
│ ├─ Difficulty    │
│ ├─ Category      │
│ └─ Author        │
└────────┬─────────┘
         │
         ▼
┌──────────────────┐
│ Update filtered  │
│ List             │
└────────┬─────────┘
         │
         ▼
┌──────────────────┐
│RecipeGridAdapter │
│ notifyDataSetChanged│
└──────────────────┘
```

## 9. Luồng Chia Sẻ

```
┌──────────────────┐
│ User clicks      │
│ Share button     │
└────────┬─────────┘
         │
         ▼
┌──────────────────┐
│ ShareRecipeDialog│
│   show()         │
└────────┬─────────┘
         │
         ▼
┌──────────────────┐
│ resolveShareLink()│
└────────┬─────────┘
         │
    ┌────┴────┐
    │         │
    ▼         ▼
┌───────┐  ┌──────────┐
│Hardcode│  │Deep Link │
│ Link  │  │(fallback)│
└───┬───┘  └─────┬────┘
    │            │
    └─────┬──────┘
          │
          ▼
┌──────────────────┐
│ Generate QR Code │
│ from shareLink   │
└────────┬─────────┘
         │
         ▼
┌──────────────────┐
│ User options:    │
│ ├─ Copy link     │
│ └─ Share via app │
└──────────────────┘
```

## 10. Luồng Deep Link

```
┌──────────────────┐
│ User clicks link:│
│ prmrecipe://recipe│
│    /{recipeId}   │
└────────┬─────────┘
         │
         ▼
┌──────────────────┐
│ Android System   │
│ handles deep link│
└────────┬─────────┘
         │
         ▼
┌──────────────────┐
│AndroidManifest.xml│
│ intent-filter    │
└────────┬─────────┘
         │
         ▼
┌──────────────────┐
│RecipeDetailActivity│
│ onCreate()       │
└────────┬─────────┘
         │
         ▼
┌──────────────────┐
│ Parse URI to get │
│    recipeId      │
└────────┬─────────┘
         │
         ▼
┌──────────────────┐
│ Load recipe      │
│ detail as normal │
└──────────────────┘
```

## 11. Cấu Trúc Firebase Database

```
Firebase Database
│
├── users/
│   └── {userId}/
│       ├── id
│       ├── name
│       ├── email
│       ├── avatar_url
│       ├── bio
│       └── joined_at
│
├── recipes/
│   └── {recipeId}/
│       ├── title
│       ├── description
│       ├── category
│       ├── rating
│       ├── difficulty
│       ├── author_id
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
│               ├── user_id
│               ├── user_name
│               ├── content
│               ├── rating
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
        ├── userId
        ├── recipeId
        ├── recipeTitle
        ├── commenterName
        ├── commentContent
        ├── type
        ├── timestamp
        └── isRead
```

## 12. Navigation Flow

```
                    MainActivity (Home)
                           │
        ┌──────────────────┼──────────────────┐
        │                  │                  │
        ▼                  ▼                  ▼
RecipesListActivity  RecipeDetailActivity  MealPlanActivity
        │                  │                  │
        │                  ├──→ UserProfileActivity
        │                  │
        │                  └──→ ShareRecipeDialog
        │
        ▼
CreateRecipeActivity
        │
        ▼
FavoritesActivity
        │
        ▼
ProfileActivity
        │
        ├──→ EditProfileActivity
        │
        └──→ UserProfileActivity
                │
                ▼
        NotificationsActivity
                │
                └──→ RecipeDetailActivity
```

## 13. Component Interaction

```
┌──────────────┐
│  Activity    │
└──────┬───────┘
       │
       ├──→ Adapter ──→ RecyclerView
       │
       ├──→ Manager ──→ Firebase
       │
       ├──→ Helper ──→ System Services
       │
       └──→ Model ──→ Data Structure
```

## 14. Real-time Update Flow

```
┌──────────────────┐
│ Firebase Database│
│   Change Event   │
└────────┬─────────┘
         │
         ▼
┌──────────────────┐
│ ValueEventListener│
│  onDataChange()  │
└────────┬─────────┘
         │
         ▼
┌──────────────────┐
│ Update Local Data│
└────────┬─────────┘
         │
         ▼
┌──────────────────┐
│ Update UI        │
│ notifyDataSetChanged│
└──────────────────┘
```

## 15. Error Handling Flow

```
┌──────────────────┐
│   Operation      │
└────────┬─────────┘
         │
    ┌────┴────┐
    │         │
    ▼         ▼
┌───────┐  ┌──────────┐
│Success│  │  Error   │
└───┬───┘  └─────┬────┘
    │            │
    │            ▼
    │    ┌──────────────┐
    │    │ onCancelled()│
    │    │ or Exception │
    │    └──────┬───────┘
    │           │
    │           ▼
    │    ┌──────────────┐
    │    │ Show Toast   │
    │    │ Log Error    │
    │    └──────────────┘
    │
    ▼
┌──────────────────┐
│ Continue Normal  │
│     Flow         │
└──────────────────┘
```

---

## Tóm Tắt

Các sơ đồ trên mô tả:
1. ✅ Luồng khởi động và xác thực
2. ✅ Luồng hiển thị và tìm kiếm công thức
3. ✅ Luồng bình luận và đánh giá
4. ✅ Luồng thông báo real-time
5. ✅ Luồng yêu thích
6. ✅ Luồng chia sẻ và deep link
7. ✅ Cấu trúc Firebase Database
8. ✅ Navigation flow
9. ✅ Component interaction
10. ✅ Real-time update và error handling

Tất cả các luồng đều được tích hợp với Firebase để đảm bảo real-time sync và data consistency.

