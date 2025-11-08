package com.example.prm_g3;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
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
    private TextView tvTitle, tvDescription, tvRating, tvTime, tvServings, tvDifficulty;
    private TextView tabIngredients, tabSteps, tabComments;
    private LinearLayout containerIngredients, containerSteps, containerComments, commentsList;
    private LinearLayout containerTags;
    private EditText edtComment;
    private Button btnSubmitReview;
    private ImageButton star1, star2, star3, star4, star5;
    private int selectedRating = 0;

    private DatabaseReference recipeRef;
    private String recipeId;
    private FavoritesManager favoritesManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);

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
        tvTime = findViewById(R.id.tvTime);
        tvServings = findViewById(R.id.tvServings);
        tvDifficulty = findViewById(R.id.tvDifficulty);
        tabIngredients = findViewById(R.id.tabIngredients);
        tabSteps = findViewById(R.id.tabSteps);
        tabComments = findViewById(R.id.tabComments);
        containerIngredients = findViewById(R.id.containerIngredients);
        containerSteps = findViewById(R.id.containerSteps);
        containerComments = findViewById(R.id.containerComments);
        commentsList = findViewById(R.id.commentsList);
        containerTags = findViewById(R.id.containerTags);
        edtComment = findViewById(R.id.edtComment);
        btnSubmitReview = findViewById(R.id.btnSubmitReview);
        star1 = findViewById(R.id.star1);
        star2 = findViewById(R.id.star2);
        star3 = findViewById(R.id.star3);
        star4 = findViewById(R.id.star4);
        star5 = findViewById(R.id.star5);

        recipeRef = FirebaseDatabase.getInstance().getReference("recipes").child(recipeId);
        favoritesManager = new FavoritesManager(this);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(RecipeDetailActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        btnFavorite.setOnClickListener(v -> {
            favoritesManager.toggleFavorite(recipeId);
            updateFavoriteButton();
            String message = favoritesManager.isFavorite(recipeId) ?
                "Đã thêm vào yêu thích" : "Đã xóa khỏi yêu thích";
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        });

        btnShare.setOnClickListener(v -> {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, tvTitle.getText().toString());
            startActivity(Intent.createChooser(shareIntent, "Chia sẻ công thức"));
        });

        // Tab navigation
        tabIngredients.setOnClickListener(v -> switchTab(0));
        tabSteps.setOnClickListener(v -> switchTab(1));
        tabComments.setOnClickListener(v -> switchTab(2));

        // Star rating
        star1.setOnClickListener(v -> setRating(1));
        star2.setOnClickListener(v -> setRating(2));
        star3.setOnClickListener(v -> setRating(3));
        star4.setOnClickListener(v -> setRating(4));
        star5.setOnClickListener(v -> setRating(5));

        // Submit review
        btnSubmitReview.setOnClickListener(v -> {
            String comment = edtComment.getText().toString().trim();
            if (selectedRating == 0) {
                Toast.makeText(this, "Vui lòng chọn đánh giá", Toast.LENGTH_SHORT).show();
                return;
            }
            if (comment.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập bình luận", Toast.LENGTH_SHORT).show();
                return;
            }

            // Save comment to Firebase
            submitComment(comment, selectedRating);
        });
    }

    private void setRating(int rating) {
        selectedRating = rating;

    }

    private void switchTab(int index) {
        // Reset all tabs
        tabIngredients.setBackgroundResource(R.drawable.tab_unselected_background);
        tabIngredients.setTextColor(0xFF666666);
        tabSteps.setBackgroundResource(R.drawable.tab_unselected_background);
        tabSteps.setTextColor(0xFF666666);
        tabComments.setBackgroundResource(R.drawable.tab_unselected_background);
        tabComments.setTextColor(0xFF666666);

        containerIngredients.setVisibility(View.GONE);
        containerSteps.setVisibility(View.GONE);
        containerComments.setVisibility(View.GONE);

        // Set selected tab
        switch (index) {
            case 0:
                tabIngredients.setBackgroundResource(R.drawable.tab_selected_background);
                tabIngredients.setTextColor(0xFF111111);
                containerIngredients.setVisibility(View.VISIBLE);
                break;
            case 1:
                tabSteps.setBackgroundResource(R.drawable.tab_selected_background);
                tabSteps.setTextColor(0xFF111111);
                containerSteps.setVisibility(View.VISIBLE);
                break;
            case 2:
                tabComments.setBackgroundResource(R.drawable.tab_selected_background);
                tabComments.setTextColor(0xFF111111);
                containerComments.setVisibility(View.VISIBLE);
                break;
        }
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
                tvDifficulty.setText(recipe.difficulty);

                // Update favorite button state
                updateFavoriteButton();

                // Format time
                int totalTime = recipe.prep_time + recipe.cook_time;
                if (totalTime >= 60) {
                    int hours = totalTime / 60;
                    int minutes = totalTime % 60;
                    if (minutes > 0) {
                        tvTime.setText(hours + " giờ " + minutes + " phút");
                    } else {
                        tvTime.setText(hours + " giờ");
                    }
                } else {
                    tvTime.setText(totalTime + " phút");
                }

                // Rating
                tvRating.setText(String.format("%.1f (0 đánh giá)", recipe.rating));

                // Servings (default if not in database)
                tvServings.setText("4 người");

                // Load image
                Glide.with(RecipeDetailActivity.this)
                        .load(recipe.image_url)
                        .placeholder(R.drawable.ic_home)
                        .into(imgRecipe);

                // Category tag
                if (recipe.category != null && !recipe.category.isEmpty()) {
                    TextView tagView = new TextView(RecipeDetailActivity.this);
                    tagView.setText(recipe.category);
                    tagView.setBackgroundResource(R.drawable.tag_background);
                    tagView.setTextColor(0xFF666666);
                    tagView.setTextSize(12);
                    tagView.setPadding(24, 12, 24, 12);
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT);
                    params.setMargins(0, 0, 16, 0);
                    tagView.setLayoutParams(params);
                    containerTags.addView(tagView);
                }

                // Load ingredients
                DataSnapshot ingredientsSnap = snapshot.child("ingredients");
                containerIngredients.removeAllViews();
                for (DataSnapshot item : ingredientsSnap.getChildren()) {
                    String name = item.child("name").getValue(String.class);
                    String quantity = item.child("quantity").getValue(String.class);
                    if (name != null && quantity != null) {
                        LinearLayout row = new LinearLayout(RecipeDetailActivity.this);
                        row.setOrientation(LinearLayout.HORIZONTAL);
                        row.setPadding(0, 24, 0, 24);

                        TextView tvName = new TextView(RecipeDetailActivity.this);
                        tvName.setText(name);
                        tvName.setTextSize(15);
                        tvName.setTextColor(0xFF111111);
                        tvName.setLayoutParams(new LinearLayout.LayoutParams(
                                0,
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                1.0f));
                        row.addView(tvName);

                        TextView tvQty = new TextView(RecipeDetailActivity.this);
                        tvQty.setText(quantity);
                        tvQty.setTextSize(15);
                        tvQty.setTextColor(0xFF666666);
                        row.addView(tvQty);

                        containerIngredients.addView(row);
                    }
                }

                // Load steps
                DataSnapshot stepsSnap = snapshot.child("steps");
                containerSteps.removeAllViews();
                for (DataSnapshot step : stepsSnap.getChildren()) {
                    Integer num = step.child("step_number").getValue(Integer.class);
                    String instruction = step.child("instruction").getValue(String.class);
                    if (num != null && instruction != null) {
                        // Create row layout
                        LinearLayout row = new LinearLayout(RecipeDetailActivity.this);
                        row.setOrientation(LinearLayout.HORIZONTAL);
                        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT);
                        rowParams.setMargins(0, 0, 0, 12);
                        row.setLayoutParams(rowParams);
                        row.setBackgroundResource(R.drawable.step_item_background);
                        row.setPadding(16, 16, 16, 16);

                        // Step number circle
                        TextView tvNum = new TextView(RecipeDetailActivity.this);
                        tvNum.setText(String.valueOf(num));
                        tvNum.setTextSize(16);
                        tvNum.setTextColor(0xFFFFFFFF);
                        tvNum.setTypeface(null, android.graphics.Typeface.BOLD);
                        tvNum.setBackgroundResource(R.drawable.step_number_bg);
                        tvNum.setGravity(android.view.Gravity.CENTER);
                        tvNum.setPadding(0, 0, 0, 0);
                        LinearLayout.LayoutParams numParams = new LinearLayout.LayoutParams(48, 48);
                        numParams.setMargins(0, 0, 16, 0);
                        tvNum.setLayoutParams(numParams);
                        row.addView(tvNum);

                        // Step instruction
                        TextView tvInstruction = new TextView(RecipeDetailActivity.this);
                        tvInstruction.setText(instruction);
                        tvInstruction.setTextSize(15);
                        tvInstruction.setTextColor(0xFF111111);
                        tvInstruction.setLayoutParams(new LinearLayout.LayoutParams(
                                0,
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                1.0f));
                        row.addView(tvInstruction);

                        containerSteps.addView(row);
                    }
                }

                // Load comments
                DataSnapshot commentsSnap = snapshot.child("comments");
                commentsList.removeAllViews();
                int commentCount = 0;
                for (DataSnapshot cmt : commentsSnap.getChildren()) {
                    String userName = cmt.child("user_name").getValue(String.class);
                    String content = cmt.child("content").getValue(String.class);
                    Long rating = cmt.child("rating").getValue(Long.class);
                    String createdAt = cmt.child("created_at").getValue(String.class);
                    String timeAgo = formatTimeAgo(createdAt);

                    if (content != null && rating != null) {
                        // Main comment container
                        LinearLayout commentContainer = new LinearLayout(RecipeDetailActivity.this);
                        commentContainer.setOrientation(LinearLayout.VERTICAL);
                        commentContainer.setPadding(0, 16, 0, 16);

                        // Top row: Avatar + Name + Rating, Time on right
                        RelativeLayout topRow = new RelativeLayout(RecipeDetailActivity.this);
                        topRow.setPadding(0, 0, 0, 8);

                        // Avatar
                        ImageView avatar = new ImageView(RecipeDetailActivity.this);
                        avatar.setId(View.generateViewId());
                        avatar.setImageResource(android.R.drawable.ic_menu_gallery);
                        avatar.setBackgroundResource(android.R.drawable.ic_menu_gallery);
                        avatar.setScaleType(ImageView.ScaleType.CENTER_CROP);
                        avatar.setPadding(2, 2, 2, 2);
                        RelativeLayout.LayoutParams avatarParams = new RelativeLayout.LayoutParams(40, 40);
                        avatarParams.addRule(RelativeLayout.ALIGN_PARENT_START);
                        avatarParams.addRule(RelativeLayout.CENTER_VERTICAL);
                        avatarParams.setMargins(0, 0, 12, 0);
                        avatar.setLayoutParams(avatarParams);
                        topRow.addView(avatar);

                        // Name and info column
                        LinearLayout infoColumn = new LinearLayout(RecipeDetailActivity.this);
                        infoColumn.setId(View.generateViewId());
                        infoColumn.setOrientation(LinearLayout.VERTICAL);
                        RelativeLayout.LayoutParams infoParams = new RelativeLayout.LayoutParams(
                                RelativeLayout.LayoutParams.WRAP_CONTENT,
                                RelativeLayout.LayoutParams.WRAP_CONTENT);
                        infoParams.addRule(RelativeLayout.RIGHT_OF, avatar.getId());
                        infoParams.addRule(RelativeLayout.CENTER_VERTICAL);
                        infoColumn.setLayoutParams(infoParams);

                        // Name
                        TextView tvName = new TextView(RecipeDetailActivity.this);
                        tvName.setText(userName != null ? userName : "Người dùng");
                        tvName.setTextSize(15);
                        tvName.setTypeface(null, android.graphics.Typeface.BOLD);
                        tvName.setTextColor(0xFF111111);
                        infoColumn.addView(tvName);

                        // Rating stars
                        LinearLayout ratingRow = new LinearLayout(RecipeDetailActivity.this);
                        ratingRow.setOrientation(LinearLayout.HORIZONTAL);
                        for (int i = 0; i < 5; i++) {
                            ImageView star = new ImageView(RecipeDetailActivity.this);
                            if (i < rating) {
                                star.setImageResource(android.R.drawable.star_big_on);
                                star.setColorFilter(0xFFFFD700);
                            } else {
                                star.setImageResource(android.R.drawable.star_big_off);
                                star.setColorFilter(0xFFCCCCCC);
                            }
                            star.setLayoutParams(new LinearLayout.LayoutParams(16, 16));
                            ratingRow.addView(star);
                        }
                        infoColumn.addView(ratingRow);
                        topRow.addView(infoColumn);

                        // Time on right
                        TextView tvTime = new TextView(RecipeDetailActivity.this);
                        tvTime.setText(timeAgo != null ? timeAgo : "");
                        tvTime.setTextSize(12);
                        tvTime.setTextColor(0xFF999999);
                        RelativeLayout.LayoutParams timeParams = new RelativeLayout.LayoutParams(
                                RelativeLayout.LayoutParams.WRAP_CONTENT,
                                RelativeLayout.LayoutParams.WRAP_CONTENT);
                        timeParams.addRule(RelativeLayout.ALIGN_PARENT_END);
                        timeParams.addRule(RelativeLayout.CENTER_VERTICAL);
                        tvTime.setLayoutParams(timeParams);
                        topRow.addView(tvTime);

                        commentContainer.addView(topRow);

                        // Comment text
                        TextView tvComment = new TextView(RecipeDetailActivity.this);
                        tvComment.setText(content);
                        tvComment.setTextSize(14);
                        tvComment.setTextColor(0xFF333333);
                        tvComment.setPadding(52, 0, 0, 0);
                        commentContainer.addView(tvComment);

                        // Add separator line
                        View separator = new View(RecipeDetailActivity.this);
                        separator.setBackgroundColor(0xFFEEEEEE);
                        LinearLayout.LayoutParams sepParams = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT, 1);
                        sepParams.setMargins(0, 16, 0, 0);
                        separator.setLayoutParams(sepParams);
                        commentContainer.addView(separator);

                        commentsList.addView(commentContainer);
                        commentCount++;
                    }
                }

                // Update rating text with comment count
                tvRating.setText(String.format("%.1f (%d đánh giá)", recipe.rating, commentCount));

                // Switch to ingredients tab by default
                switchTab(0);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(RecipeDetailActivity.this, "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void submitComment(String comment, int rating) {
        // Get current user ID
        String currentUserId = UserManager.getInstance().getCurrentUserId();
        if (currentUserId == null) {
            currentUserId = "user_002"; // Fallback to default user
        }

        // Create comment data
        java.util.Map<String, Object> commentData = new java.util.HashMap<>();
        commentData.put("user_id", currentUserId);
        commentData.put("content", comment);
        commentData.put("rating", rating);
        commentData.put("created_at", new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.getDefault()).format(new java.util.Date()));
        commentData.put("sync_status", 1);

        // Get user name from users table
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users").child(currentUserId);
        usersRef.child("name").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String userName = snapshot.getValue(String.class);
                if (userName == null) userName = "Người dùng";

                commentData.put("user_name", userName);

                // Save comment to Firebase
                String commentId = recipeRef.child("comments").push().getKey();
                if (commentId != null) {
                    recipeRef.child("comments").child(commentId).setValue(commentData)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(RecipeDetailActivity.this, "Đã gửi đánh giá", Toast.LENGTH_SHORT).show();
                                edtComment.setText("");
                                setRating(0);
                                // Reload recipe to show new comment
                                loadRecipeDetail();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(RecipeDetailActivity.this, "Lỗi gửi đánh giá: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Use default name if can't get from database
                commentData.put("user_name", "Người dùng");

                String commentId = recipeRef.child("comments").push().getKey();
                if (commentId != null) {
                    recipeRef.child("comments").child(commentId).setValue(commentData)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(RecipeDetailActivity.this, "Đã gửi đánh giá", Toast.LENGTH_SHORT).show();
                                edtComment.setText("");
                                setRating(0);
                                loadRecipeDetail();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(RecipeDetailActivity.this, "Lỗi gửi đánh giá: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                }
            }
        });
    }

    private void updateFavoriteButton() {
        if (favoritesManager.isFavorite(recipeId)) {
            btnFavorite.setImageResource(R.drawable.ic_heart_filled);
            btnFavorite.setColorFilter(0xFFFF6B6B); // Red color for favorited
        } else {
            btnFavorite.setImageResource(R.drawable.ic_heart_outline);
            btnFavorite.setColorFilter(0xFF666666); // Gray color for not favorited
        }
    }

    private String formatTimeAgo(String createdAt) {
        if (createdAt == null || createdAt.isEmpty()) {
            return "Vừa xong";
        }

        try {
            // Parse ISO format time (2025-11-03T15:30:00Z)
            java.text.SimpleDateFormat iso = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.getDefault());
            java.util.Date commentDate = iso.parse(createdAt);
            java.util.Date now = new java.util.Date();

            long diffInMillis = now.getTime() - commentDate.getTime();
            long diffInMinutes = diffInMillis / (60 * 1000);
            long diffInHours = diffInMillis / (60 * 60 * 1000);
            long diffInDays = diffInMillis / (24 * 60 * 60 * 1000);

            if (diffInMinutes < 1) {
                return "Vừa xong";
            } else if (diffInMinutes < 60) {
                return diffInMinutes + " phút trước";
            } else if (diffInHours < 24) {
                return diffInHours + " giờ trước";
            } else if (diffInDays < 7) {
                return diffInDays + " ngày trước";
            } else {
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault());
                return sdf.format(commentDate);
            }
        } catch (Exception e) {
            return "Vừa xong";
        }
    }
}
