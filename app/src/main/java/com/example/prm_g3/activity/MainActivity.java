package com.example.prm_g3.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.Color;

import com.example.prm_g3.R;
import com.example.prm_g3.RecipesListActivity;
import com.example.prm_g3.UserManager;
import com.example.prm_g3.adapters.RecipeAdapter;
import com.example.prm_g3.adapters.RecipeGridAdapter;
import com.example.prm_g3.models.Recipe;
import com.example.prm_g3.models.User;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth; // THÊM DÒNG NÀY
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Collections;

public class MainActivity extends AppCompatActivity {

    private RecyclerView rvRecipes, rvPopularRecipes;
    private RecipeAdapter adapter;
    private RecipeGridAdapter popularAdapter;
    private List<Recipe> recipeList;
    private List<Recipe> popularRecipeList;
    private List<String> featuredRecipeIds;
    private List<String> popularRecipeIds;
    private List<Recipe> allRecipes;
    private List<String> allRecipeIds;
    private EditText edtSearch;
    private TextView tvGreeting;
    private LinearLayout searchBarLayout;
    private LinearLayout categoryContainer;
    private ImageView btnNotifications;
    private DatabaseReference recipesRef;
    private String selectedCategory = null;
    private List<TextView> categoryButtons = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            startActivity(new Intent(this, AuthActivity.class));
            finish();
            return;
        }

        rvRecipes = findViewById(R.id.rvRecipes);
        rvPopularRecipes = findViewById(R.id.rvPopularRecipes);
        edtSearch = findViewById(R.id.edtSearch);
        tvGreeting = findViewById(R.id.tvGreeting);
        categoryContainer = findViewById(R.id.categoryContainer);
        searchBarLayout = findViewById(R.id.searchBarLayout);
        btnNotifications = findViewById(R.id.btnNotifications);
        recipeList = new ArrayList<>();
        popularRecipeList = new ArrayList<>();
        featuredRecipeIds = new ArrayList<>();
        popularRecipeIds = new ArrayList<>();
        allRecipes = new ArrayList<>();
        allRecipeIds = new ArrayList<>();

        // Featured recipes - Linear layout
        adapter = new RecipeAdapter(this, recipeList, featuredRecipeIds);
        rvRecipes.setLayoutManager(new LinearLayoutManager(this));
        rvRecipes.setAdapter(adapter);

        // Popular recipes - Grid layout 2 columns
        popularAdapter = new RecipeGridAdapter(this, popularRecipeList, popularRecipeIds);
        rvPopularRecipes.setLayoutManager(new GridLayoutManager(this, 2));
        rvPopularRecipes.setAdapter(popularAdapter);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        recipesRef = database.getReference("recipes");

        loadRecipes();
        setupUserGreeting();

        setupBottomNav();
        setupSearch();
        setupNotificationsButton();

        // Request notification permission for Android 13+
        requestNotificationPermission();
    }

    private void setupNotificationsButton() {
        btnNotifications.setOnClickListener(v -> {
            // Mở NotificationsActivity để xem danh sách thông báo
            Intent intent = new Intent(MainActivity.this, NotificationsActivity.class);
            startActivity(intent);
        });
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                // Request permission
                ActivityCompat.requestPermissions(this,
                        new String[] { Manifest.permission.POST_NOTIFICATIONS },
                        100);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
            @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("MainActivity", "Notification permission granted");
            } else {
                Log.d("MainActivity", "Notification permission denied");
                Toast.makeText(this, "Bạn cần cấp quyền thông báo để nhận thông báo bình luận",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh favorites when returning to main activity
        if (adapter != null) {
            adapter.refreshFavorites();
        }
        if (popularAdapter != null) {
            popularAdapter.refreshFavorites();
        }
    }

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
                        android.util.Log.e("MainActivity",
                                "Error parsing recipe: " + data.getKey() + " - " + e.getMessage(), e);
                        // Skip this recipe and continue with others
                    }
                }

                if (allRecipes.isEmpty()) {
                    return;
                }

                // Extract unique categories from recipes
                Set<String> categoriesSet = new HashSet<>();
                for (Recipe recipe : allRecipes) {
                    if (recipe.category != null && !recipe.category.trim().isEmpty()) {
                        categoriesSet.add(recipe.category.trim());
                    }
                }

                // Update category filter buttons
                updateCategoryFilters(new ArrayList<>(categoriesSet));

                // Apply category filter and update recipes
                applyCategoryFilter();

                // Highlight "All" button by default if no category is selected
                if (selectedCategory == null && !categoryButtons.isEmpty()) {
                    setSelectedFilter(categoryButtons.get(0)); // First button is "Tất cả"
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupSearch() {
        // Make search bar clickable - navigate to RecipesListActivity
        View.OnClickListener searchClickListener = v -> {
            String searchQuery = edtSearch.getText().toString().trim();
            Intent intent = new Intent(MainActivity.this, RecipesListActivity.class);
            if (!searchQuery.isEmpty()) {
                intent.putExtra("search_query", searchQuery);
            }
            startActivity(intent);
        };

        searchBarLayout.setOnClickListener(searchClickListener);
        edtSearch.setOnClickListener(searchClickListener);
    }

    private void updateCategoryFilters(List<String> categories) {
        // Clear existing buttons
        categoryContainer.removeAllViews();
        categoryButtons.clear();

        // Sort categories alphabetically
        Collections.sort(categories);

        // Create "All" button first
        TextView allButton = createCategoryButton("Tất cả");
        allButton.setOnClickListener(v -> {
            selectedCategory = null;
            resetFilterButtons();
            setSelectedFilter(allButton);
            applyCategoryFilter();
        });
        categoryContainer.addView(allButton);
        categoryButtons.add(allButton);

        // Create buttons for each category
        for (String category : categories) {
            TextView categoryButton = createCategoryButton(category);
            categoryButton.setOnClickListener(v -> {
                if (selectedCategory != null && selectedCategory.equals(category)) {
                    // Deselect if already selected - go back to "All"
                    selectedCategory = null;
                    resetFilterButtons();
                    // Highlight "All" button (first button)
                    if (!categoryButtons.isEmpty()) {
                        setSelectedFilter(categoryButtons.get(0));
                    }
                } else {
                    selectedCategory = category;
                    resetFilterButtons();
                    setSelectedFilter(categoryButton);
                }
                applyCategoryFilter();
            });
            categoryContainer.addView(categoryButton);
            categoryButtons.add(categoryButton);
        }
    }

    private TextView createCategoryButton(String text) {
        TextView button = new TextView(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMarginEnd((int) (8 * getResources().getDisplayMetrics().density));
        button.setLayoutParams(params);
        button.setBackgroundResource(R.drawable.category_button_bg);
        button.setGravity(android.view.Gravity.CENTER);
        int padding = (int) (16 * getResources().getDisplayMetrics().density);
        button.setPadding(padding, (int) (8 * getResources().getDisplayMetrics().density),
                padding, (int) (8 * getResources().getDisplayMetrics().density));
        button.setText(text);
        button.setTextColor(Color.parseColor("#333333"));
        button.setTextSize(13);
        button.setClickable(true);
        button.setFocusable(true);
        return button;
    }

    private void applyCategoryFilter() {
        // Filter the already loaded recipes without reloading from Firebase
        if (allRecipes.isEmpty()) {
            return;
        }

        // Apply category filter
        List<RecipeWithId> filteredRecipeWithIds = new ArrayList<>();
        for (int i = 0; i < allRecipes.size(); i++) {
            Recipe recipe = allRecipes.get(i);
            if (selectedCategory == null || (recipe.category != null && recipe.category.equals(selectedCategory))) {
                filteredRecipeWithIds.add(new RecipeWithId(recipe, allRecipeIds.get(i)));
            }
        }

        // Featured recipes: top 3 highest rated from filtered recipes
        List<RecipeWithId> recipeWithIds = new ArrayList<>(filteredRecipeWithIds);
        recipeWithIds.sort((a, b) -> Double.compare(b.recipe.rating, a.recipe.rating));

        recipeList.clear();
        popularRecipeList.clear();
        featuredRecipeIds.clear();
        popularRecipeIds.clear();

        int featuredCount = Math.min(3, recipeWithIds.size());
        for (int i = 0; i < featuredCount; i++) {
            recipeList.add(recipeWithIds.get(i).recipe);
            featuredRecipeIds.add(recipeWithIds.get(i).recipeId);
        }

        // Popular recipes: all remaining recipes (or all if less than 3)
        if (recipeWithIds.size() > featuredCount) {
            for (int i = featuredCount; i < recipeWithIds.size(); i++) {
                popularRecipeList.add(recipeWithIds.get(i).recipe);
                popularRecipeIds.add(recipeWithIds.get(i).recipeId);
            }
        } else {
            // If we have less than 3 recipes, show all in popular
            for (RecipeWithId rwi : recipeWithIds) {
                popularRecipeList.add(rwi.recipe);
                popularRecipeIds.add(rwi.recipeId);
            }
        }

        adapter.notifyDataSetChanged();
        popularAdapter.notifyDataSetChanged();
    }

    private void resetFilterButtons() {
        for (TextView button : categoryButtons) {
            button.setBackgroundResource(R.drawable.category_button_bg);
            button.setTextColor(Color.parseColor("#333333"));
        }
    }

    private void setSelectedFilter(TextView selectedButton) {
        // You can customize the selected state background and color here
        // For now, just change text color to indicate selection
        selectedButton.setTextColor(Color.parseColor("#FF6B35")); // Orange color for selection
    }

    private void setupBottomNav() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_home);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                // Already on home page
                return true;
            } else if (id == R.id.nav_recipes) {
                Intent intent = new Intent(MainActivity.this, RecipesListActivity.class);
                startActivity(intent);
                return true;
            } else if (id == R.id.nav_plan) {
                Intent intent = new Intent(MainActivity.this, MealPlanActivity.class);
                startActivity(intent);
                return true;
            } else if (id == R.id.nav_favorite) {
                Intent intent = new Intent(MainActivity.this, FavoritesActivity.class);
                startActivity(intent);
                return true;
            } else if (id == R.id.nav_profile) {
                Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                startActivity(intent);
                return true;
            }
            return false;
        });
    }

    private void setupUserGreeting() {
        // Check if user data is already loaded
        if (UserManager.getInstance().getCurrentUser() != null) {
            updateGreeting(UserManager.getInstance().getCurrentUser().getName());
        } else {
            // Load user data from Firebase
            String currentUserId = UserManager.getInstance().getCurrentUserId();
            if (currentUserId != null) {
                DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(currentUserId);
                userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            User user = snapshot.getValue(User.class);
                            if (user != null) {
                                UserManager.getInstance().setCurrentUser(user);
                                updateGreeting(user.getName());
                            } else {
                                updateGreeting(null);
                            }
                        } else {
                            updateGreeting(null);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("MainActivity", "Lỗi tải thông tin user: " + error.getMessage());
                        updateGreeting(null);
                    }
                });
            } else {
                updateGreeting(null);
            }
        }
    }

    private void updateGreeting(String userName) {
        // Get current time for appropriate greeting
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        int hour = calendar.get(java.util.Calendar.HOUR_OF_DAY);

        String timeGreeting;
        if (hour < 12) {
            timeGreeting = "Chào buổi sáng";
        } else if (hour < 18) {
            timeGreeting = "Chào buổi chiều";
        } else {
            timeGreeting = "Chào buổi tối";
        }

        String greeting;
        if (userName != null && !userName.isEmpty()) {
            greeting = timeGreeting + ", " + userName + "!";
        } else {
            greeting = timeGreeting + ", Chef!";
        }

        tvGreeting.setText(greeting);
    }

    // Helper class to keep recipe and ID together during sorting
    private static class RecipeWithId {
        Recipe recipe;
        String recipeId;

        RecipeWithId(Recipe recipe, String recipeId) {
            this.recipe = recipe;
            this.recipeId = recipeId;
        }
    }
}
