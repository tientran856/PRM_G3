package com.example.prm_g3;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
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
    private ImageButton btnBack, btnFavorite, btnShare;
    private TextView tvTitle, tvDescription, tvRating;

    private DatabaseReference recipeRef;
    private String recipeId;

    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);
        getWindow().setDecorFitsSystemWindows(false);

        // Set status bar to black
        setStatusBarBlack();

        // Get recipe ID
        recipeId = getIntent().getStringExtra("recipeId");
        if (recipeId == null) {
            Toast.makeText(this, "Không tìm thấy công thức", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupClickListeners();
        loadRecipeDetail();
    }

    private void initViews() {
        imgRecipe = findViewById(R.id.imgRecipe);
        btnBack = findViewById(R.id.btnBack);
        btnFavorite = findViewById(R.id.btnFavorite);
        btnShare = findViewById(R.id.btnShare);
        tvTitle = findViewById(R.id.tvTitle);
        tvDescription = findViewById(R.id.tvDescription);
        tvRating = findViewById(R.id.tvRating);

        recipeRef = FirebaseDatabase.getInstance().getReference("recipes").child(recipeId);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(RecipeDetailActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        btnFavorite.setOnClickListener(v -> {
            // Toggle favorite
            Toast.makeText(this, "Đã thêm vào yêu thích", Toast.LENGTH_SHORT).show();
        });

        btnShare.setOnClickListener(v -> {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, tvTitle.getText().toString());
            startActivity(Intent.createChooser(shareIntent, "Chia sẻ công thức"));
        });
    }

    private void loadRecipeDetail() {
        recipeRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Recipe recipe = snapshot.getValue(Recipe.class);
                if (recipe == null) {
                    Toast.makeText(RecipeDetailActivity.this, "Không tìm thấy công thức", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }

                // Set basic info
                tvTitle.setText(recipe.title);
                tvDescription.setText(recipe.description);

                // Rating
                tvRating.setText(String.format("%.1f (0 đánh giá)", recipe.rating));

                // Load image
                Glide.with(RecipeDetailActivity.this)
                        .load(recipe.image_url)
                        .placeholder(R.drawable.ic_home)
                        .into(imgRecipe);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(RecipeDetailActivity.this, "Không tải được dữ liệu", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setStatusBarBlack() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(0xFF000000);
            int flags = getWindow().getDecorView().getSystemUiVisibility();
            flags &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            getWindow().getDecorView().setSystemUiVisibility(flags);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        setStatusBarBlack();
    }
}
