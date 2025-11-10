package com.example.prm_g3.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.prm_g3.R;
import com.example.prm_g3.RecipesListActivity;
import com.example.prm_g3.UserManager;
import com.example.prm_g3.adapters.RecipeAdapter;
import com.example.prm_g3.adapters.RecipeGridAdapter;
import com.example.prm_g3.models.Recipe;
import com.example.prm_g3.models.User;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;          // THÊM DÒNG NÀY
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
    private EditText edtSearch;
    private TextView tvGreeting;
    private DatabaseReference recipesRef;

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
        recipeList = new ArrayList<>();
        popularRecipeList = new ArrayList<>();
        featuredRecipeIds = new ArrayList<>();
        popularRecipeIds = new ArrayList<>();

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
    }

    @Override
    protected void onResume() {
        super.onResume();
        setupUserGreeting(); // Refresh greeting when coming back to main activity
        // Refresh favorites for current user
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
                recipeList.clear();
                popularRecipeList.clear();
                featuredRecipeIds.clear();
                popularRecipeIds.clear();

                List<Recipe> allRecipes = new ArrayList<>();
                List<String> allRecipeIds = new ArrayList<>();
                for (DataSnapshot data : snapshot.getChildren()) {
                    try {
                        Recipe r = data.getValue(Recipe.class);
                        if (r != null) {
                            allRecipes.add(r);
                            allRecipeIds.add(data.getKey());
                        }
                    } catch (Exception e) {
                        android.util.Log.e("MainActivity", "Error parsing recipe: " + data.getKey() + " - " + e.getMessage(), e);
                        // Skip this recipe and continue with others
                    }
                }

                if (allRecipes.isEmpty()) {
                    return;
                }

                // Featured recipes: top 3 highest rated
                // Sort recipes with their IDs together
                List<RecipeWithId> recipeWithIds = new ArrayList<>();
                for (int i = 0; i < allRecipes.size(); i++) {
                    recipeWithIds.add(new RecipeWithId(allRecipes.get(i), allRecipeIds.get(i)));
                }
                recipeWithIds.sort((a, b) -> Double.compare(b.recipe.rating, a.recipe.rating));

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
        edtSearch.setOnEditorActionListener((v, actionId, event) -> {
            String keyword = edtSearch.getText().toString().trim().toLowerCase();
            List<Recipe> filtered = new ArrayList<>();
            List<String> filteredIds = new ArrayList<>();

            for (int i = 0; i < recipeList.size(); i++) {
                Recipe r = recipeList.get(i);
                if (r.title.toLowerCase().contains(keyword)) {
                    filtered.add(r);
                    filteredIds.add(featuredRecipeIds.get(i));
                }
            }

            adapter = new RecipeAdapter(MainActivity.this, filtered, filteredIds);
            rvRecipes.setAdapter(adapter);
            return true;
        });
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
