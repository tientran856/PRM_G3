package com.example.prm_g3;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.prm_g3.adapters.RecipeAdapter;
import com.example.prm_g3.models.Recipe;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView rvRecipes;
    private RecipeAdapter adapter;
    private List<Recipe> recipeList;
    private DatabaseReference recipesRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rvRecipes = findViewById(R.id.rvRecipes);
        rvRecipes.setLayoutManager(new LinearLayoutManager(this));
        recipeList = new ArrayList<>();
        adapter = new RecipeAdapter(this, recipeList);
        rvRecipes.setAdapter(adapter);

        // 🔥 Kết nối tới Realtime Database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        recipesRef = database.getReference("recipes");

        loadRecipes();
    }

    private void loadRecipes() {
        recipesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                recipeList.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    Recipe recipe = data.getValue(Recipe.class);
                    recipeList.add(recipe);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Lỗi đọc dữ liệu", error.toException());
                Toast.makeText(MainActivity.this, "Không thể tải dữ liệu!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
