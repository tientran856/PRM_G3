package com.example.prm_g3;

import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.prm_g3.Entity.Recipe;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class LandingActivity extends AppCompatActivity {

    private RecyclerView rvRecipes;
    private LinearLayout categoryContainer;
    private RecipeAdapter recipeAdapter;
    private List<Recipe> recipeList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ladning);

        rvRecipes = findViewById(R.id.rvRecipes);
        categoryContainer = findViewById(R.id.categoryContainer);
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);

        setupCategories();
        setupRecipeList();
        setupBottomNav(bottomNav);
    }

    private void setupCategories() {
        String[] categories = {"Món chính", "Món tráng miệng", "Món ăn nhanh", "Đồ uống"};
        for (String c : categories) {
            Button btn = new Button(this);
            btn.setText(c);
            btn.setAllCaps(false);
            btn.setTextSize(14);
            btn.setBackgroundResource(R.drawable.search_bg);
            btn.setPadding(40, 10, 40, 10);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(8, 0, 8, 0);
            btn.setLayoutParams(params);
            categoryContainer.addView(btn);
        }
    }

    private void setupRecipeList() {
        rvRecipes.setLayoutManager(new LinearLayoutManager(this));

        Recipe pho = new Recipe();
        pho.title = "Phở bò Hà Nội";
        pho.rating = 4.8f;
        pho.cook_time = 120;
        pho.difficulty = "Khó";

        Recipe banhmi = new Recipe();
        banhmi.title = "Bánh mì thịt nướng";
        banhmi.rating = 4.6f;
        banhmi.cook_time = 45;
        banhmi.difficulty = "Trung bình";

        Recipe friedRice = new Recipe();
        friedRice.title = "Cơm chiên trứng tỏi";
        friedRice.rating = 4.7f;
        friedRice.cook_time = 30;
        friedRice.difficulty = "Dễ";

        recipeList.add(pho);
        recipeList.add(banhmi);
        recipeList.add(friedRice);

        recipeAdapter = new RecipeAdapter(this, recipeList);
        rvRecipes.setAdapter(recipeAdapter);
    }

    private void setupBottomNav(BottomNavigationView bottomNav) {
        bottomNav.setOnItemSelectedListener(item -> {
            Toast.makeText(this, "Chuyển đến: " + item.getTitle(), Toast.LENGTH_SHORT).show();
            return true;
        });
    }
}
