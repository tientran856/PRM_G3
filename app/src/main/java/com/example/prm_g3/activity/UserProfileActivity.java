package com.example.prm_g3.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.bumptech.glide.Glide;
import com.example.prm_g3.R;
import com.example.prm_g3.RecipesListActivity;
import com.example.prm_g3.adapters.RecipeGridAdapter;
import com.example.prm_g3.models.Recipe;
import com.example.prm_g3.models.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class UserProfileActivity extends AppCompatActivity {

    private ImageView btnBack, imgAvatar;
    private TextView tvUserName, tvUserEmail, tvUserBio, tvJoinedDate;
    private RecyclerView rvRecipes;
    private RecipeGridAdapter adapter;
    private List<Recipe> recipeList;
    private List<String> recipeIds;
    private TextView tvNoRecipes;

    private DatabaseReference usersRef;
    private DatabaseReference recipesRef;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        userId = getIntent().getStringExtra("userId");
        if (userId == null || userId.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy thông tin người dùng", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupStatusBar();
        initViews();
        loadUserProfile();
        loadUserRecipes();
    }

    private void setupStatusBar() {
        getWindow().setStatusBarColor(Color.parseColor("#0D0D1A"));
        WindowInsetsControllerCompat windowInsetsController = WindowCompat.getInsetsController(getWindow(),
                getWindow().getDecorView());
        if (windowInsetsController != null) {
            windowInsetsController.setAppearanceLightStatusBars(false);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            View decorView = getWindow().getDecorView();
            int flags = decorView.getSystemUiVisibility();
            flags &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            decorView.setSystemUiVisibility(flags);
        }
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        imgAvatar = findViewById(R.id.imgAvatar);
        tvUserName = findViewById(R.id.tvUserName);
        tvUserEmail = findViewById(R.id.tvUserEmail);
        tvUserBio = findViewById(R.id.tvUserBio);
        tvJoinedDate = findViewById(R.id.tvJoinedDate);
        rvRecipes = findViewById(R.id.rvRecipes);
        tvNoRecipes = findViewById(R.id.tvNoRecipes);

        btnBack.setOnClickListener(v -> finish());

        recipeList = new ArrayList<>();
        recipeIds = new ArrayList<>();
        adapter = new RecipeGridAdapter(this, recipeList, recipeIds);
        adapter.setOnRecipeClickListener((recipe, recipeId) -> {
            Intent intent = new Intent(UserProfileActivity.this, RecipeDetailActivity.class);
            intent.putExtra("recipeId", recipeId);
            startActivity(intent);
        });

        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        rvRecipes.setLayoutManager(layoutManager);
        rvRecipes.setAdapter(adapter);

        usersRef = FirebaseDatabase.getInstance().getReference("users");
        recipesRef = FirebaseDatabase.getInstance().getReference("recipes");
    }

    private void loadUserProfile() {
        usersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    User user = snapshot.getValue(User.class);
                    if (user != null) {
                        displayUserInfo(user);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UserProfileActivity.this, "Lỗi tải thông tin người dùng", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayUserInfo(User user) {
        tvUserName.setText(user.getName());
        tvUserEmail.setText(user.getEmail());

        if (user.getBio() != null && !user.getBio().isEmpty()) {
            tvUserBio.setText(user.getBio());
        } else {
            tvUserBio.setText("Chưa có thông tin giới thiệu");
        }

        if (user.getJoined_at() != null) {
            tvJoinedDate.setText("Tham gia: " + formatJoinedDate(user.getJoined_at()));
        } else {
            tvJoinedDate.setText("Tham gia: Không rõ");
        }

        if (user.getAvatar_url() != null && !user.getAvatar_url().isEmpty()) {
            Glide.with(this)
                    .load(user.getAvatar_url())
                    .placeholder(R.drawable.ic_user)
                    .error(R.drawable.ic_user)
                    .into(imgAvatar);
        } else {
            imgAvatar.setImageResource(R.drawable.ic_user);
        }
    }

    private String formatJoinedDate(String timestamp) {
        try {
            if (timestamp.contains("T")) {
                return timestamp.substring(0, 10);
            }
            long millis = Long.parseLong(timestamp);
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy");
            return sdf.format(new java.util.Date(millis));
        } catch (Exception e) {
            return timestamp;
        }
    }

    private void loadUserRecipes() {
        recipesRef.orderByChild("author_id").equalTo(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        recipeList.clear();
                        recipeIds.clear();

                        for (DataSnapshot data : snapshot.getChildren()) {
                            try {
                                Recipe recipe = data.getValue(Recipe.class);
                                if (recipe != null) {
                                    recipeList.add(recipe);
                                    recipeIds.add(data.getKey());
                                }
                            } catch (Exception e) {
                                android.util.Log.e("UserProfileActivity", "Error parsing recipe: " + data.getKey(), e);
                            }
                        }

                        if (recipeList.isEmpty()) {
                            tvNoRecipes.setVisibility(View.VISIBLE);
                            rvRecipes.setVisibility(View.GONE);
                        } else {
                            tvNoRecipes.setVisibility(View.GONE);
                            rvRecipes.setVisibility(View.VISIBLE);
                            adapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        android.util.Log.e("UserProfileActivity", "Error loading recipes: " + error.getMessage());
                        tvNoRecipes.setVisibility(View.VISIBLE);
                        rvRecipes.setVisibility(View.GONE);
                    }
                });
    }
}
