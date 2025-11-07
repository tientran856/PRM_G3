package com.example.prm_g3;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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
    private TextView tvTitle;
    private LinearLayout tvEmptyMessage;
    private RecyclerView recyclerViewFavorites;
    private RecipeGridAdapter adapter;
    private List<Recipe> favoriteRecipesList;
    private List<String> favoriteRecipeIds;

    private FavoritesManager favoritesManager;
    private DatabaseReference recipesRef;

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
        tvEmptyMessage = findViewById(R.id.tvEmptyMessage);
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
    }

    private void setupRecyclerView() {
        adapter = new RecipeGridAdapter(this, favoriteRecipesList, favoriteRecipeIds);
        adapter.setOnRecipeClickListener(this);

        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        recyclerViewFavorites.setLayoutManager(layoutManager);
        recyclerViewFavorites.setAdapter(adapter);
    }

    private void loadFavoriteRecipes() {
        try {
            android.util.Log.d("FavoritesActivity", "Loading favorite recipes...");

            if (favoritesManager == null) {
                android.util.Log.e("FavoritesActivity", "FavoritesManager is null");
                showEmptyState();
                return;
            }

            Set<String> favoriteIds = favoritesManager.getFavoriteRecipes();

            if (favoriteIds == null || favoriteIds.isEmpty()) {
                android.util.Log.d("FavoritesActivity", "No favorites found, showing empty state");
                showEmptyState();
                return;
            }

            favoriteRecipesList.clear();
            favoriteRecipeIds.clear();

            if (adapter != null) {
                adapter.notifyDataSetChanged();
            }

            // Show empty state initially, will hide when data loads
            showEmptyState();

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
                                            if (adapter != null) {
                                                adapter.notifyDataSetChanged();
                                            }
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
            showEmptyState();
        }
    }

    private void showEmptyState() {
        tvEmptyMessage.setVisibility(View.VISIBLE);
        recyclerViewFavorites.setVisibility(View.GONE);
    }

    private void hideEmptyState() {
        tvEmptyMessage.setVisibility(View.GONE);
        recyclerViewFavorites.setVisibility(View.VISIBLE);
    }

    @Override
    public void onRecipeClick(String recipeId) {
        // Navigate to recipe detail
        Intent intent = new Intent(this, RecipeDetailActivity.class);
        intent.putExtra("recipeId", recipeId);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload favorites in case something changed
        loadFavoriteRecipes();
    }
}
