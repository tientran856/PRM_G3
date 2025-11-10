package com.example.prm_g3.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
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
import java.util.List;

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
    private TextView btnCategoryMain, btnCategoryDessert, btnCategoryFast;
    private LinearLayout searchBarLayout;
    private DatabaseReference recipesRef;
    private String selectedCategory = null;

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
        btnCategoryMain = findViewById(R.id.btnCategoryMain);
        btnCategoryDessert = findViewById(R.id.btnCategoryDessert);
        btnCategoryFast = findViewById(R.id.btnCategoryFast);
        searchBarLayout = findViewById(R.id.searchBarLayout);
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
        setupFilters();
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

                // Apply category filter if selected
                List<RecipeWithId> filteredRecipeWithIds = new ArrayList<>();
                for (int i = 0; i < allRecipes.size(); i++) {
                    Recipe recipe = allRecipes.get(i);
                    if (selectedCategory == null
                            || (recipe.category != null && recipe.category.equals(selectedCategory))) {
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

    private void setupFilters() {
        // Reset all button backgrounds
        resetFilterButtons();

        btnCategoryMain.setOnClickListener(v -> {
            if (selectedCategory != null && selectedCategory.equals("Món chính")) {
                // Deselect if already selected
                selectedCategory = null;
                resetFilterButtons();
            } else {
                selectedCategory = "Món chính";
                setSelectedFilter(btnCategoryMain);
            }
            applyCategoryFilter();
        });

        btnCategoryDessert.setOnClickListener(v -> {
            if (selectedCategory != null && selectedCategory.equals("Món tráng miệng")) {
                // Deselect if already selected
                selectedCategory = null;
                resetFilterButtons();
            } else {
                selectedCategory = "Món tráng miệng";
                setSelectedFilter(btnCategoryDessert);
            }
            applyCategoryFilter();
        });

        btnCategoryFast.setOnClickListener(v -> {
            if (selectedCategory != null && selectedCategory.equals("Món ăn nhanh")) {
                // Deselect if already selected
                selectedCategory = null;
                resetFilterButtons();
            } else {
                selectedCategory = "Món ăn nhanh";
                setSelectedFilter(btnCategoryFast);
            }
            applyCategoryFilter();
        });
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
        btnCategoryMain.setBackgroundResource(R.drawable.category_button_bg);
        btnCategoryMain.setTextColor(Color.parseColor("#333333"));
        btnCategoryDessert.setBackgroundResource(R.drawable.category_button_bg);
        btnCategoryDessert.setTextColor(Color.parseColor("#333333"));
        btnCategoryFast.setBackgroundResource(R.drawable.category_button_bg);
        btnCategoryFast.setTextColor(Color.parseColor("#333333"));
    }

    private void setSelectedFilter(TextView selectedButton) {
        resetFilterButtons();
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
                Toast.makeText(this, "Trang chủ", Toast.LENGTH_SHORT).show();
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
