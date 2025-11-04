package com.example.prm_g3;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.prm_g3.models.Recipe;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class RecipeDetailActivity extends AppCompatActivity {

    private ImageView imgRecipe;
    private TextView tvTitle, tvDescription, tvInfo;
    private LinearLayout containerIngredients, containerSteps, containerComments;

    private DatabaseReference recipeRef;
    private String recipeId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);

        imgRecipe = findViewById(R.id.imgRecipe);
        tvTitle = findViewById(R.id.tvTitle);
        tvDescription = findViewById(R.id.tvDescription);
        tvInfo = findViewById(R.id.tvInfo);
        containerIngredients = findViewById(R.id.containerIngredients);
        containerSteps = findViewById(R.id.containerSteps);
        containerComments = findViewById(R.id.containerComments);

        recipeId = getIntent().getStringExtra("recipeId");
        recipeRef = FirebaseDatabase.getInstance().getReference("recipes").child(recipeId);

        loadRecipeDetail();
    }

    private void loadRecipeDetail() {
        recipeRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Recipe recipe = snapshot.getValue(Recipe.class);
                if (recipe == null) return;

                tvTitle.setText(recipe.title);
                tvDescription.setText(recipe.description);
                tvInfo.setText("⏱ " + recipe.cook_time + " phút • " + recipe.difficulty);

                Glide.with(RecipeDetailActivity.this)
                        .load(recipe.image_url)
                        .placeholder(R.drawable.ic_home)
                        .into(imgRecipe);

                // 🧂 Load nguyên liệu
                DataSnapshot ingredientsSnap = snapshot.child("ingredients");
                for (DataSnapshot item : ingredientsSnap.getChildren()) {
                    String name = item.child("name").getValue(String.class);
                    String quantity = item.child("quantity").getValue(String.class);
                    TextView tv = new TextView(RecipeDetailActivity.this);
                    tv.setText("- " + name + " (" + quantity + ")");
                    tv.setTextSize(15);
                    tv.setPadding(12, 4, 0, 4);
                    containerIngredients.addView(tv);
                }

                // 📋 Load bước
                DataSnapshot stepsSnap = snapshot.child("steps");
                for (DataSnapshot step : stepsSnap.getChildren()) {
                    int num = step.child("step_number").getValue(Integer.class);
                    String instruction = step.child("instruction").getValue(String.class);
                    TextView tv = new TextView(RecipeDetailActivity.this);
                    tv.setText("Bước " + num + ": " + instruction);
                    tv.setPadding(12, 4, 0, 4);
                    containerSteps.addView(tv);
                }

                // 💬 Load bình luận
                DataSnapshot commentsSnap = snapshot.child("comments");
                for (DataSnapshot cmt : commentsSnap.getChildren()) {
                    String content = cmt.child("content").getValue(String.class);
                    Long rating = cmt.child("rating").getValue(Long.class);
                    TextView tv = new TextView(RecipeDetailActivity.this);
                    tv.setText("⭐ " + rating + " - " + content);
                    tv.setPadding(12, 4, 0, 4);
                    containerComments.addView(tv);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(RecipeDetailActivity.this, "Không tải được dữ liệu", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
