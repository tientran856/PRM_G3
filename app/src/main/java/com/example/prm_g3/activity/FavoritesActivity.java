package com.example.prm_g3.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.prm_g3.FavoritesManager;
import com.example.prm_g3.R;
import com.example.prm_g3.RecipesListActivity;
import com.example.prm_g3.adapters.RecipeGridAdapter;
import com.example.prm_g3.models.Recipe;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class FavoritesActivity extends AppCompatActivity implements RecipeGridAdapter.OnRecipeClickListener {

    private ImageButton btnBack;
    private TextView tvTitle, tvFavoriteCount;
    private LinearLayout emptyMessageLayout;
    private RecyclerView recyclerViewFavorites;
    private RecipeGridAdapter adapter;
    private List<Recipe> favoriteRecipesList;
    private List<String> favoriteRecipeIds;

    private FavoritesManager favoritesManager;
    private DatabaseReference recipesRef;
    private boolean isLoading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            setContentView(R.layout.activity_favorites);
            initViews();
            setupRecyclerView();

            // Delay loading to ensure everything is initialized
            new android.os.Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    loadFavoriteRecipes();
                }
            }, 100);

        } catch (Exception e) {
            android.util.Log.e("FavoritesActivity", "Error in onCreate: ", e);
            Toast.makeText(this, "Lỗi khởi tạo: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tvTitle = findViewById(R.id.tvTitle);
        tvFavoriteCount = findViewById(R.id.tvFavoriteCount);
        emptyMessageLayout = findViewById(R.id.tvEmptyMessage);
        recyclerViewFavorites = findViewById(R.id.recyclerViewFavorites);

        favoritesManager = new FavoritesManager(this);
        recipesRef = FirebaseDatabase.getInstance().getReference("recipes");

        favoriteRecipesList = new ArrayList<>();
        favoriteRecipeIds = new ArrayList<>();

        tvTitle.setText("Công thức yêu thích");

        btnBack.setOnClickListener(v -> finish());

        // Listen for favorites changes
        favoritesManager.setOnFavoritesChangedListener(new FavoritesManager.OnFavoritesChangedListener() {
            @Override
            public void onFavoritesChanged() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        loadFavoriteRecipes();
                    }
                });
            }
        });

        setupBottomNav();
    }

    private void setupRecyclerView() {
        adapter = new RecipeGridAdapter(this, favoriteRecipesList, favoriteRecipeIds);
        adapter.setOnRecipeClickListener(this);
        adapter.setOnFavoriteChangeListener(new RecipeGridAdapter.OnFavoriteChangeListener() {
            @Override
            public void onFavoriteChanged() {
                // Reload favorites when a recipe is unfavorited
                loadFavoriteRecipes();
            }
        });

        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        recyclerViewFavorites.setLayoutManager(layoutManager);
        recyclerViewFavorites.setAdapter(adapter);
    }

    private void loadFavoriteRecipes() {
        try {
            android.util.Log.d("FavoritesActivity", "Loading favorite recipes...");

            // Prevent multiple simultaneous loads
            if (isLoading) {
                android.util.Log.d("FavoritesActivity", "Already loading, skipping...");
                return;
            }

            if (favoritesManager == null) {
                android.util.Log.e("FavoritesActivity", "FavoritesManager is null");
                showEmptyState();
                return;
            }

            Set<String> favoriteIds = favoritesManager.getFavoriteRecipes();

            if (favoriteIds == null || favoriteIds.isEmpty()) {
                android.util.Log.d("FavoritesActivity", "No favorites found, showing empty state");
                favoriteRecipesList.clear();
                favoriteRecipeIds.clear();
                if (adapter != null) {
                    adapter.notifyDataSetChanged();
                }
                updateFavoriteCount();
                showEmptyState();
                return;
            }

            isLoading = true;
            favoriteRecipesList.clear();
            favoriteRecipeIds.clear();

            if (adapter != null) {
                adapter.notifyDataSetChanged();
            }


            final int[] loadedCount = {0};
            final int totalCount = favoriteIds.size();

            for (String recipeId : favoriteIds) {
                if (recipeId != null && !recipeId.trim().isEmpty()) {
                    recipesRef.child(recipeId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            try {
                                Recipe recipe = snapshot.getValue(Recipe.class);
                                if (recipe != null) {
                                    favoriteRecipesList.add(recipe);
                                    favoriteRecipeIds.add(recipeId);
                                }

                                loadedCount[0]++;

                                // Update UI when all recipes are loaded
                                if (loadedCount[0] >= totalCount) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            isLoading = false; // Reset loading flag
                                            if (adapter != null) {
                                                adapter.notifyDataSetChanged();
                                            }
                                            updateFavoriteCount();
                                            if (favoriteRecipesList.isEmpty()) {
                                                showEmptyState();
                                            } else {
                                                hideEmptyState();
                                            }
                                        }
                                    });
                                }
                            } catch (Exception e) {
                                android.util.Log.e("FavoritesActivity", "Error processing recipe data: ", e);
                                loadedCount[0]++;
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            android.util.Log.e("FavoritesActivity", "Error loading recipe: " + error.getMessage());
                            loadedCount[0]++;
                        }
                    });
                } else {
                    loadedCount[0]++;
                }
            }
        } catch (Exception e) {
            android.util.Log.e("FavoritesActivity", "Error in loadFavoriteRecipes: ", e);
            isLoading = false; // Reset loading flag
            showEmptyState();
        }
    }

    private void showEmptyState() {
        emptyMessageLayout.setVisibility(View.VISIBLE);
        recyclerViewFavorites.setVisibility(View.GONE);
    }

    private void hideEmptyState() {
        emptyMessageLayout.setVisibility(View.GONE);
        recyclerViewFavorites.setVisibility(View.VISIBLE);
    }

    private void updateFavoriteCount() {
        int count = favoriteRecipesList.size();
        String countText = count + " công thức yêu thích";
        tvFavoriteCount.setText(countText);
    }

    @Override
    public void onRecipeClick(Recipe recipe, String recipeId) {
        // Navigate to recipe detail
        Intent intent = new Intent(this, RecipeDetailActivity.class);
        intent.putExtra("recipeId", recipeId);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh favorites for current user and reload
        favoritesManager.refreshForCurrentUser();
        loadFavoriteRecipes();
    }

    private void setupBottomNav() {
        com.google.android.material.bottomnavigation.BottomNavigationView bottomNavigationView =
            findViewById(R.id.bottomNavigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_favorite);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                Intent intent = new Intent(FavoritesActivity.this, MainActivity.class);
                startActivity(intent);
                return true;
            } else if (id == R.id.nav_recipes) {
                Intent intent = new Intent(FavoritesActivity.this, RecipesListActivity.class);
                startActivity(intent);
                return true;
            } else if (id == R.id.nav_plan) {
                Intent intent = new Intent(FavoritesActivity.this, MealPlanActivity.class);
                startActivity(intent);
                return true;
            } else if (id == R.id.nav_favorite) {
                return true;
            } else if (id == R.id.nav_profile) {
                Intent intent = new Intent(FavoritesActivity.this, ProfileActivity.class);
                startActivity(intent);
                return true;
            }
            return false;
        });
    }
}
