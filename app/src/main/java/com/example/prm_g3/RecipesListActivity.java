package com.example.prm_g3;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.graphics.Color;
import android.os.Build;
import androidx.appcompat.app.AlertDialog;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.example.prm_g3.activity.CreateRecipeActivity;
import com.example.prm_g3.activity.FavoritesActivity;
import com.example.prm_g3.activity.MainActivity;
import com.example.prm_g3.activity.MealPlanActivity;
import com.example.prm_g3.activity.ProfileActivity;
import com.example.prm_g3.adapters.RecipeGridAdapter;
import com.example.prm_g3.models.Recipe;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Collections;

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

    // Filter states
    private Set<String> selectedDifficulties = new HashSet<>();
    private Set<String> selectedCategories = new HashSet<>();
    private List<String> availableCategories = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipes_list);

        // Configure status bar to show light icons on dark background
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
        setupRecyclerView();

        // Handle search query from Intent first (before loading recipes)
        handleSearchIntent();

        loadRecipes();
        setupSearch();
        setupBottomNav();
        setupCreateButton();
        setupFilterButton();
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

                // Extract unique categories from recipes
                Set<String> categoriesSet = new HashSet<>();
                for (Recipe recipe : recipeList) {
                    if (recipe.category != null && !recipe.category.trim().isEmpty()) {
                        categoriesSet.add(recipe.category.trim());
                    }
                }
                availableCategories.clear();
                availableCategories.addAll(categoriesSet);
                Collections.sort(availableCategories);

                // Apply filters
                applyFilters();
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
                // Apply all filters including search query
                applyFilters();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void applyFilters() {
        filteredList.clear();
        filteredIds.clear();

        String searchQuery = edtSearch.getText().toString().trim().toLowerCase();

        for (int i = 0; i < recipeList.size(); i++) {
            Recipe recipe = recipeList.get(i);
            boolean matches = true;

            // Filter by search query
            if (!searchQuery.isEmpty()) {
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

    private void setupBottomNav() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_recipes);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                Intent intent = new Intent(RecipesListActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
                return true;
            } else if (id == R.id.nav_recipes) {
                return true;
            } else if (id == R.id.nav_plan) {
                Intent intent = new Intent(RecipesListActivity.this, MealPlanActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
                return true;
            } else if (id == R.id.nav_favorite) {
                Intent intent = new Intent(RecipesListActivity.this, FavoritesActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
                return true;
            } else if (id == R.id.nav_profile) {
                Intent intent = new Intent(RecipesListActivity.this, ProfileActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
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
            showFilterDialog();
        });
    }

    private void showFilterDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_filter, null);
        builder.setView(dialogView);

        Dialog dialog = builder.create();

        // Get views
        CheckBox cbEasy = dialogView.findViewById(R.id.cbEasy);
        CheckBox cbMedium = dialogView.findViewById(R.id.cbMedium);
        CheckBox cbHard = dialogView.findViewById(R.id.cbHard);
        LinearLayout containerCategories = dialogView.findViewById(R.id.containerCategories);
        Button btnApply = dialogView.findViewById(R.id.btnApply);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);
        Button btnReset = dialogView.findViewById(R.id.btnReset);

        // Set current filter states
        cbEasy.setChecked(selectedDifficulties.contains("Dễ"));
        cbMedium.setChecked(selectedDifficulties.contains("Trung bình"));
        cbHard.setChecked(selectedDifficulties.contains("Khó"));

        // Clear and populate categories
        containerCategories.removeAllViews();
        List<CheckBox> categoryCheckboxes = new ArrayList<>();
        if (availableCategories.isEmpty()) {
            // Show message if no categories available
            android.widget.TextView tvNoCategories = new android.widget.TextView(this);
            tvNoCategories.setText("Chưa có loại món ăn nào");
            tvNoCategories.setTextSize(14);
            tvNoCategories.setTextColor(Color.parseColor("#999999"));
            tvNoCategories.setPadding(0, (int) (8 * getResources().getDisplayMetrics().density), 0,
                    (int) (8 * getResources().getDisplayMetrics().density));
            containerCategories.addView(tvNoCategories);
        } else {
            for (String category : availableCategories) {
                CheckBox checkBox = new CheckBox(this);
                checkBox.setText(category);
                checkBox.setTextSize(14);
                checkBox.setTextColor(Color.parseColor("#333333"));
                checkBox.setPadding(0, (int) (8 * getResources().getDisplayMetrics().density), 0,
                        (int) (8 * getResources().getDisplayMetrics().density));
                checkBox.setChecked(selectedCategories.contains(category));
                containerCategories.addView(checkBox);
                categoryCheckboxes.add(checkBox);
            }
        }

        // Apply button
        btnApply.setOnClickListener(v -> {
            // Update selected difficulties
            selectedDifficulties.clear();
            if (cbEasy.isChecked())
                selectedDifficulties.add("Dễ");
            if (cbMedium.isChecked())
                selectedDifficulties.add("Trung bình");
            if (cbHard.isChecked())
                selectedDifficulties.add("Khó");

            // Update selected categories
            selectedCategories.clear();
            for (CheckBox checkBox : categoryCheckboxes) {
                if (checkBox.isChecked()) {
                    selectedCategories.add(checkBox.getText().toString());
                }
            }

            // Apply filters
            applyFilters();
            dialog.dismiss();
        });

        // Cancel button
        btnCancel.setOnClickListener(v -> dialog.dismiss());

        // Reset button
        btnReset.setOnClickListener(v -> {
            cbEasy.setChecked(false);
            cbMedium.setChecked(false);
            cbHard.setChecked(false);
            for (CheckBox checkBox : categoryCheckboxes) {
                checkBox.setChecked(false);
            }
            selectedDifficulties.clear();
            selectedCategories.clear();
            applyFilters();
            dialog.dismiss();
        });

        dialog.show();
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
