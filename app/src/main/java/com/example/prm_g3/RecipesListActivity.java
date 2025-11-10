package com.example.prm_g3;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.prm_g3.activity.CreateRecipeActivity;
import com.example.prm_g3.activity.FavoritesActivity;
import com.example.prm_g3.activity.MealPlanActivity;
import com.example.prm_g3.activity.ProfileActivity;
import com.example.prm_g3.adapters.RecipeGridAdapter;
import com.example.prm_g3.models.Recipe;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class RecipesListActivity extends AppCompatActivity {

    private RecyclerView rvRecipes;
    private RecipeGridAdapter adapter;
    private List<Recipe> recipeList;
    private List<Recipe> filteredList;
    private List<String> recipeIds;
    private List<String> filteredIds;
    private EditText edtSearch;
    private ImageView btnFilter;
    private Button btnCreateNew;
    private DatabaseReference recipesRef;
    private String pendingSearchQuery = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipes_list);

        // Handle system window insets for status bar
        View rootView = findViewById(android.R.id.content);
        if (rootView != null) {
            rootView.setOnApplyWindowInsetsListener((v, insets) -> {
                int statusBarHeight = insets.getSystemWindowInsetTop();
                LinearLayout headerLayout = findViewById(R.id.headerLayout);
                if (headerLayout != null) {
                    headerLayout.setPadding(
                            headerLayout.getPaddingLeft(),
                            statusBarHeight + 16,
                            headerLayout.getPaddingRight(),
                            headerLayout.getPaddingBottom());
                }
                return insets;
            });
        }

        initViews();
        setupRecyclerView();

        // Handle search query from Intent first (before loading recipes)
        handleSearchIntent();

        loadRecipes();
        setupSearch();
        setupBottomNav();
        setupCreateButton();
        setupFilterButton();
    }

    private void initViews() {
        rvRecipes = findViewById(R.id.rvRecipes);
        edtSearch = findViewById(R.id.edtSearch);
        btnFilter = findViewById(R.id.btnFilter);
        btnCreateNew = findViewById(R.id.btnCreateNew);

        // Ensure button is clickable
        if (btnCreateNew != null) {
            btnCreateNew.setClickable(true);
            btnCreateNew.setFocusable(true);
            Log.d("RecipesListActivity", "btnCreateNew initialized: " + (btnCreateNew != null));
        } else {
            Log.e("RecipesListActivity", "btnCreateNew is NULL!");
        }

        recipeList = new ArrayList<>();
        filteredList = new ArrayList<>();
        recipeIds = new ArrayList<>();
        filteredIds = new ArrayList<>();
    }

    private void setupRecyclerView() {
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        rvRecipes.setLayoutManager(layoutManager);

        adapter = new RecipeGridAdapter(this, filteredList, filteredIds);
        rvRecipes.setAdapter(adapter);

        // Ensure RecyclerView is visible
        rvRecipes.setVisibility(View.VISIBLE);
    }

    private void updateAdapter() {
        if (adapter == null) {
            adapter = new RecipeGridAdapter(this, filteredList, filteredIds);
            rvRecipes.setAdapter(adapter);
        } else {
            // Update adapter with new lists
            adapter = new RecipeGridAdapter(this, filteredList, filteredIds);
            rvRecipes.setAdapter(adapter);
        }
        adapter.notifyDataSetChanged();
    }

    private void loadRecipes() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        recipesRef = database.getReference("recipes");

        recipesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                recipeList.clear();
                recipeIds.clear();

                for (DataSnapshot data : snapshot.getChildren()) {
                    try {
                        Recipe r = data.getValue(Recipe.class);
                        if (r != null) {
                            recipeList.add(r);
                            recipeIds.add(data.getKey());
                        }
                    } catch (Exception e) {
                        Log.e("RecipesListActivity", "Error parsing recipe: " + data.getKey() + " - " + e.getMessage(),
                                e);
                        // Skip this recipe and continue with others
                    }
                }

                Log.d("RecipesListActivity", "Loaded " + recipeList.size() + " recipes");

                // Update filtered lists
                filteredList.clear();
                filteredIds.clear();

                // Apply pending search query if exists
                if (pendingSearchQuery != null && !pendingSearchQuery.isEmpty()) {
                    filterRecipes(pendingSearchQuery);
                    pendingSearchQuery = null; // Clear after applying
                } else {
                    filteredList.addAll(recipeList);
                    filteredIds.addAll(recipeIds);
                }

                // Update adapter
                updateAdapter();
                Log.d("RecipesListActivity", "Adapter updated, filteredList size: " + filteredList.size());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("RecipesListActivity", "Error loading recipes: " + error.getMessage());
                Toast.makeText(RecipesListActivity.this, "Lỗi tải dữ liệu: " + error.getMessage(), Toast.LENGTH_SHORT)
                        .show();
            }
        });
    }

    private void setupSearch() {
        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterRecipes(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void filterRecipes(String query) {
        filteredList.clear();
        filteredIds.clear();
        if (query.isEmpty()) {
            filteredList.addAll(recipeList);
            filteredIds.addAll(recipeIds);
        } else {
            String lowerQuery = query.toLowerCase();
            for (int i = 0; i < recipeList.size(); i++) {
                Recipe r = recipeList.get(i);
                if (r.title != null && r.title.toLowerCase().contains(lowerQuery)) {
                    filteredList.add(r);
                    filteredIds.add(recipeIds.get(i));
                }
            }
        }

        // Update adapter with filtered data
        updateAdapter();
    }

    private void setupBottomNav() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_recipes);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                finish();
                return true;
            } else if (id == R.id.nav_recipes) {
                return true;
            } else if (id == R.id.nav_plan) {
                Intent intent = new Intent(RecipesListActivity.this, MealPlanActivity.class);
                startActivity(intent);
                return true;
            } else if (id == R.id.nav_favorite) {
                Intent intent = new Intent(RecipesListActivity.this, FavoritesActivity.class);
                startActivity(intent);
                return true;
            } else if (id == R.id.nav_profile) {
                Intent intent = new Intent(RecipesListActivity.this, ProfileActivity.class);
                startActivity(intent);
                return true;
            }
            return false;
        });
    }

    private void setupCreateButton() {
        if (btnCreateNew == null) {
            Log.e("RecipesListActivity", "btnCreateNew is null in setupCreateButton!");
            return;
        }

        Log.d("RecipesListActivity", "Setting up create button click listener");
        btnCreateNew.setOnClickListener(v -> {
            Log.d("RecipesListActivity", "Create button clicked!");
            try {
                Intent intent = new Intent(RecipesListActivity.this, CreateRecipeActivity.class);
                startActivity(intent);
                Log.d("RecipesListActivity", "Started CreateRecipeActivity");
            } catch (Exception e) {
                Log.e("RecipesListActivity", "Error opening CreateRecipeActivity: " + e.getMessage(), e);
                Toast.makeText(this, "Lỗi mở trang tạo mới: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupFilterButton() {
        btnFilter.setOnClickListener(v -> {
            // TODO: Show filter dialog
            Toast.makeText(this, "Bộ lọc", Toast.LENGTH_SHORT).show();
        });
    }

    private void handleSearchIntent() {
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("search_query")) {
            String searchQuery = intent.getStringExtra("search_query");
            if (searchQuery != null && !searchQuery.isEmpty()) {
                // Store the search query to apply after recipes are loaded
                pendingSearchQuery = searchQuery;
                // Set the search text immediately
                edtSearch.setText(searchQuery);
            }
        }
    }
}
