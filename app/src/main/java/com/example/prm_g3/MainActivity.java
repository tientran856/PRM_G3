package com.example.prm_g3;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import com.example.prm_g3.activity.AuthActivity;
import com.example.prm_g3.adapters.RecipeAdapter;
import com.example.prm_g3.adapters.RecipeGridAdapter;
import com.example.prm_g3.models.Recipe;
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
    private List<String> popularRecipeIds;
    private EditText edtSearch;
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
        recipeList = new ArrayList<>();
        popularRecipeList = new ArrayList<>();
        popularRecipeIds = new ArrayList<>();

        // Featured recipes - Linear layout
        adapter = new RecipeAdapter(this, recipeList);
        rvRecipes.setLayoutManager(new LinearLayoutManager(this));
        rvRecipes.setAdapter(adapter);

        // Popular recipes - Grid layout 2 columns
        popularAdapter = new RecipeGridAdapter(this, popularRecipeList, popularRecipeIds);
        rvPopularRecipes.setLayoutManager(new GridLayoutManager(this, 2));
        rvPopularRecipes.setAdapter(popularAdapter);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        recipesRef = database.getReference("recipes");

        loadRecipes();

        setupBottomNav();
        setupSearch();
    }

    private void loadRecipes() {
        recipesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                recipeList.clear();
                popularRecipeList.clear();
                popularRecipeIds.clear();

                List<Recipe> allRecipes = new ArrayList<>();
                List<String> allRecipeIds = new ArrayList<>();
                for (DataSnapshot data : snapshot.getChildren()) {
                    Recipe r = data.getValue(Recipe.class);
                    if (r != null) {
                        allRecipes.add(r);
                        allRecipeIds.add(data.getKey());
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
            for (Recipe r : recipeList) {
                if (r.title.toLowerCase().contains(keyword)) {
                    filtered.add(r);
                }
            }
            adapter = new RecipeAdapter(MainActivity.this, filtered);
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
