package com.example.prm_g3;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import com.example.prm_g3.activity.AuthActivity;
import com.example.prm_g3.adapters.RecipeAdapter;
import com.example.prm_g3.models.Recipe;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;          // THÊM DÒNG NÀY
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView rvRecipes;
    private RecipeAdapter adapter;
    private List<Recipe> recipeList;
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
        edtSearch = findViewById(R.id.edtSearch);
        recipeList = new ArrayList<>();

        adapter = new RecipeAdapter(this, recipeList);
        rvRecipes.setLayoutManager(new LinearLayoutManager(this));
        rvRecipes.setAdapter(adapter);

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
                for (DataSnapshot data : snapshot.getChildren()) {
                    Recipe r = data.getValue(Recipe.class);
                    recipeList.add(r);
                }
                adapter.notifyDataSetChanged();
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
