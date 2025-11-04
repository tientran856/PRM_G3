package com.example.prm_g3;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import com.example.prm_g3.adapters.RecipeAdapter;
import com.example.prm_g3.adapters.RecipeGridAdapter;
import com.example.prm_g3.models.Recipe;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView rvRecipes, rvPopularRecipes;
    private RecipeAdapter adapter;
    private RecipeGridAdapter popularAdapter;
    private List<Recipe> recipeList;
    private List<Recipe> popularRecipeList;
    private EditText edtSearch;
    private DatabaseReference recipesRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rvRecipes = findViewById(R.id.rvRecipes);
        rvPopularRecipes = findViewById(R.id.rvPopularRecipes);
        edtSearch = findViewById(R.id.edtSearch);
        recipeList = new ArrayList<>();
        popularRecipeList = new ArrayList<>();

        // Featured recipes - Linear layout
        adapter = new RecipeAdapter(this, recipeList);
        rvRecipes.setLayoutManager(new LinearLayoutManager(this));
        rvRecipes.setAdapter(adapter);

        // Popular recipes - Grid layout 2 columns
        popularAdapter = new RecipeGridAdapter(this, popularRecipeList);
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

                List<Recipe> allRecipes = new ArrayList<>();
                for (DataSnapshot data : snapshot.getChildren()) {
                    Recipe r = data.getValue(Recipe.class);
                    if (r != null) {
                        allRecipes.add(r);
                    }
                }

                if (allRecipes.isEmpty()) {
                    return;
                }

                // Featured recipes: top 3 highest rated
                allRecipes.sort((a, b) -> Double.compare(b.rating, a.rating));
                int featuredCount = Math.min(3, allRecipes.size());
                recipeList.addAll(allRecipes.subList(0, featuredCount));

                // Popular recipes: all remaining recipes (or all if less than 3)
                if (allRecipes.size() > featuredCount) {
                    popularRecipeList.addAll(allRecipes.subList(featuredCount, allRecipes.size()));
                } else {
                    // If we have less than 3 recipes, show all in popular
                    popularRecipeList.addAll(allRecipes);
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

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                Toast.makeText(this, "Trang chủ", Toast.LENGTH_SHORT).show();
                return true;
            } else if (id == R.id.nav_recipes) {
                Toast.makeText(this, "Công thức", Toast.LENGTH_SHORT).show();
                return true;
            } else if (id == R.id.nav_plan) {
                Toast.makeText(this, "Kế hoạch", Toast.LENGTH_SHORT).show();
                return true;
            } else if (id == R.id.nav_favorite) {
                Toast.makeText(this, "Yêu thích", Toast.LENGTH_SHORT).show();
                return true;
            } else if (id == R.id.nav_profile) {
                Toast.makeText(this, "Cá nhân", Toast.LENGTH_SHORT).show();
                return true;
            }
            return false;
        });
    }
}
