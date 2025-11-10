package com.example.prm_g3.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.prm_g3.R;
import com.example.prm_g3.RecipesListActivity;
import com.example.prm_g3.UserManager;
import com.example.prm_g3.adapters.RecipeGridAdapter;
import com.example.prm_g3.models.Recipe;
import com.example.prm_g3.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class ProfileActivity extends AppCompatActivity {

    private static final int EDIT_PROFILE_REQUEST_CODE = 1001;

    private ImageView imgAvatar, btnBack;
    private TextView tvUserName, tvUserEmail, tvUserBio, tvJoinedDate;
    private TextView btnViewAllRecipes, tvNoRecipes;
    private Button btnLogout, btnEditProfile;
    private RecyclerView rvMyRecipes;
    private RecipeGridAdapter myRecipesAdapter;
    private List<Recipe> myRecipesList;
    private List<String> myRecipeIds;

    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;
    private DatabaseReference recipesRef;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Configure status bar to show light icons (white) on dark background
        setupStatusBar();

        // Handle system window insets for status bar
        View rootView = findViewById(android.R.id.content);
        if (rootView != null) {
            rootView.setOnApplyWindowInsetsListener((v, insets) -> {
                int statusBarHeight = insets.getSystemWindowInsetTop();
                LinearLayout headerLayout = findViewById(R.id.headerLayout);
                if (headerLayout != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    // Adjust padding to account for status bar
                    headerLayout.setPadding(
                            headerLayout.getPaddingLeft(),
                            Math.max(statusBarHeight, (int) (24 * getResources().getDisplayMetrics().density)),
                            headerLayout.getPaddingRight(),
                            headerLayout.getPaddingBottom());
                }
                return insets;
            });
        }

        initViews();
        setupAuth();
        loadUserProfile();
        loadMyRecipes();
        setupListeners();
        setupBottomNav();
    }

    private void setupStatusBar() {
        // Configure status bar to show light icons (white) on dark background
        // Set status bar background to dark (to match header)
        getWindow().setStatusBarColor(Color.parseColor("#0D0D1A"));

        // Use WindowInsetsControllerCompat for modern approach (API 23+)
        WindowInsetsControllerCompat windowInsetsController = WindowCompat.getInsetsController(getWindow(),
                getWindow().getDecorView());

        if (windowInsetsController != null) {
            // Show light status bar icons (white icons on dark background)
            windowInsetsController.setAppearanceLightStatusBars(false);
        }
        // Fallback for older devices (API 23-29)
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            View decorView = getWindow().getDecorView();
            int flags = decorView.getSystemUiVisibility();
            // Disable light status bar (keep white icons on dark background)
            flags &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            decorView.setSystemUiVisibility(flags);
        }
    }

    private void initViews() {
        imgAvatar = findViewById(R.id.imgAvatar);
        btnBack = findViewById(R.id.btnBack);
        tvUserName = findViewById(R.id.tvUserName);
        tvUserEmail = findViewById(R.id.tvUserEmail);
        tvUserBio = findViewById(R.id.tvUserBio);
        tvJoinedDate = findViewById(R.id.tvJoinedDate);
        btnLogout = findViewById(R.id.btnLogout);
        btnEditProfile = findViewById(R.id.btnEditProfile);
        btnViewAllRecipes = findViewById(R.id.btnViewAllRecipes);
        tvNoRecipes = findViewById(R.id.tvNoRecipes);
        rvMyRecipes = findViewById(R.id.rvMyRecipes);

        myRecipesList = new ArrayList<>();
        myRecipeIds = new ArrayList<>();
        myRecipesAdapter = new RecipeGridAdapter(this, myRecipesList, myRecipeIds);

        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        rvMyRecipes.setLayoutManager(layoutManager);
        rvMyRecipes.setAdapter(myRecipesAdapter);

        recipesRef = FirebaseDatabase.getInstance().getReference("recipes");
    }

    private void setupAuth() {
        mAuth = FirebaseAuth.getInstance();

        if (!UserManager.getInstance().isLoggedIn()) {
            startActivity(new Intent(this, AuthActivity.class));
            finish();
            return;
        }

        currentUserId = UserManager.getInstance().getCurrentUserId();
        usersRef = FirebaseDatabase.getInstance().getReference("users");
    }

    private void loadUserProfile() {
        usersRef.child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    User user = snapshot.getValue(User.class);
                    if (user != null) {
                        displayUserInfo(user);
                    }
                } else {
                    // Nếu chưa có thông tin user trong database, tạo mới từ Firebase Auth
                    createUserProfile();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ProfileActivity.this, "Lỗi tải thông tin người dùng", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createUserProfile() {
        if (UserManager.getInstance().isLoggedIn()) {
            User newUser = new User(
                    currentUserId,
                    UserManager.getInstance().getCurrentUserDisplayName() != null
                            ? UserManager.getInstance().getCurrentUserDisplayName()
                            : "Người dùng",
                    UserManager.getInstance().getCurrentUserEmail() != null
                            ? UserManager.getInstance().getCurrentUserEmail()
                            : "");

            usersRef.child(currentUserId).setValue(newUser)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Tạo hồ sơ người dùng thành công", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Lỗi tạo hồ sơ người dùng", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void displayUserInfo(User user) {
        tvUserName.setText(user.getName());
        tvUserEmail.setText(user.getEmail());

        if (user.getBio() != null && !user.getBio().isEmpty()) {
            tvUserBio.setText(user.getBio());
        } else {
            tvUserBio.setText("Chưa có thông tin giới thiệu");
        }

        if (user.getJoined_at() != null) {
            tvJoinedDate.setText("Tham gia: " + formatJoinedDate(user.getJoined_at()));
        } else {
            tvJoinedDate.setText("Tham gia: Không rõ");
        }

        // TODO: Load avatar image using Glide or Picasso if avatar_url is available
        if (user.getAvatar_url() != null && !user.getAvatar_url().isEmpty()) {
            // Load avatar image here
        }
    }

    private String formatJoinedDate(String timestamp) {
        try {
            // Nếu timestamp là ISO format
            if (timestamp.contains("T")) {
                return timestamp.substring(0, 10); // Lấy phần ngày yyyy-MM-dd
            }
            // Nếu timestamp là milliseconds
            long millis = Long.parseLong(timestamp);
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy");
            return sdf.format(new java.util.Date(millis));
        } catch (Exception e) {
            return timestamp;
        }
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        btnLogout.setOnClickListener(v -> showLogoutDialog());

        btnEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, EditProfileActivity.class);
            startActivityForResult(intent, EDIT_PROFILE_REQUEST_CODE);
        });

        btnViewAllRecipes.setOnClickListener(v -> {
            // Navigate to MyRecipesActivity or RecipesListActivity with filter
            Intent intent = new Intent(ProfileActivity.this, RecipesListActivity.class);
            intent.putExtra("filter_by_author", currentUserId);
            startActivity(intent);
        });
    }

    private void loadMyRecipes() {
        if (currentUserId == null || currentUserId.isEmpty()) {
            tvNoRecipes.setVisibility(View.VISIBLE);
            rvMyRecipes.setVisibility(View.GONE);
            return;
        }

        recipesRef.orderByChild("author_id").equalTo(currentUserId)
                .limitToFirst(4) // Chỉ lấy 4 công thức đầu tiên để hiển thị
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        myRecipesList.clear();
                        myRecipeIds.clear();

                        for (DataSnapshot data : snapshot.getChildren()) {
                            try {
                                Recipe recipe = data.getValue(Recipe.class);
                                if (recipe != null) {
                                    myRecipesList.add(recipe);
                                    myRecipeIds.add(data.getKey());
                                }
                            } catch (Exception e) {
                                android.util.Log.e("ProfileActivity", "Error parsing recipe: " + data.getKey(), e);
                            }
                        }

                        if (myRecipesList.isEmpty()) {
                            tvNoRecipes.setVisibility(View.VISIBLE);
                            rvMyRecipes.setVisibility(View.GONE);
                        } else {
                            tvNoRecipes.setVisibility(View.GONE);
                            rvMyRecipes.setVisibility(View.VISIBLE);
                            myRecipesAdapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        android.util.Log.e("ProfileActivity", "Error loading my recipes: " + error.getMessage());
                        tvNoRecipes.setVisibility(View.VISIBLE);
                        rvMyRecipes.setVisibility(View.GONE);
                    }
                });
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Đăng xuất")
                .setMessage("Bạn có chắc chắn muốn đăng xuất?")
                .setPositiveButton("Đăng xuất", (dialog, which) -> performLogout())
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void performLogout() {
        mAuth.signOut();
        UserManager.getInstance().clearUser();
        Intent intent = new Intent(this, AuthActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == EDIT_PROFILE_REQUEST_CODE && resultCode == RESULT_OK) {
            if (data != null && data.getBooleanExtra("updated", false)) {
                // Reload user profile to show updated information
                loadUserProfile();
                Toast.makeText(this, "Hồ sơ đã được cập nhật", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void setupBottomNav() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_profile);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
                return true;
            } else if (id == R.id.nav_recipes) {
                Intent intent = new Intent(ProfileActivity.this, RecipesListActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
                return true;
            } else if (id == R.id.nav_plan) {
                Intent intent = new Intent(ProfileActivity.this, MealPlanActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
                return true;
            } else if (id == R.id.nav_favorite) {
                Intent intent = new Intent(ProfileActivity.this, FavoritesActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
                return true;
            } else if (id == R.id.nav_profile) {
                return true; // Already on profile page
            }
            return false;
        });
    }
}
