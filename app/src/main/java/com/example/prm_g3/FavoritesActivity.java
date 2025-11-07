package com.example.prm_g3;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
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
    private TextView tvTitle, tvEmptyMessage;
    private RecyclerView recyclerViewFavorites;
    private RecipeGridAdapter adapter;
    private List<Recipe> favoriteRecipesList;
    private List<String> favoriteRecipeIds;

    private FavoritesManager favoritesManager;
    private DatabaseReference recipesRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        initViews();
        setupRecyclerView();
        loadFavoriteRecipes();
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
    }

    private void setupRecyclerView() {
        adapter = new RecipeGridAdapter(this, favoriteRecipesList, favoriteRecipeIds);
        adapter.setOnRecipeClickListener(this);

        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        recyclerViewFavorites.setLayoutManager(layoutManager);
        recyclerViewFavorites.setAdapter(adapter);
    }

    private void loadFavoriteRecipes() {
        Set<String> favoriteIds = favoritesManager.getFavoriteRecipes();

        if (favoriteIds.isEmpty()) {
            showEmptyState();
            return;
        }

        favoriteRecipesList.clear();
        favoriteRecipeIds.clear();

        for (String recipeId : favoriteIds) {
            recipesRef.child(recipeId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Recipe recipe = snapshot.getValue(Recipe.class);
                    if (recipe != null) {
                        favoriteRecipesList.add(recipe);
                        favoriteRecipeIds.add(recipeId);
                        adapter.notifyDataSetChanged();
                        hideEmptyState();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(FavoritesActivity.this, "Lỗi tải dữ liệu: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
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
        android.content.Intent intent = new android.content.Intent(this, RecipeDetailActivity.class);
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
