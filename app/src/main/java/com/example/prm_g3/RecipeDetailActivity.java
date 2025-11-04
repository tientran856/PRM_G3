package com.example.prm_g3;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.prm_g3.adapters.CommentAdapter;
import com.example.prm_g3.models.Comment;
import com.example.prm_g3.models.Recipe;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class RecipeDetailActivity extends AppCompatActivity {

    private ImageView imgRecipe;
    private TextView tvTitle, tvDescription, tvInfo, tvNoComments;
    private LinearLayout containerIngredients, containerSteps;
    private RecyclerView recyclerViewComments;
    private Button btnAddComment;

    private DatabaseReference recipeRef, commentsRef;
    private String recipeId;
    private CommentAdapter commentAdapter;
    private List<Comment> commentList;

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
        recyclerViewComments = findViewById(R.id.recyclerViewComments);
        btnAddComment = findViewById(R.id.btnAddComment);
        tvNoComments = findViewById(R.id.tvNoComments);

        recipeId = getIntent().getStringExtra("recipeId");
        recipeRef = FirebaseDatabase.getInstance().getReference("recipes").child(recipeId);
        commentsRef = FirebaseDatabase.getInstance().getReference("recipes").child(recipeId).child("comments");

        // Setup RecyclerView for comments
        commentList = new ArrayList<>();
        commentAdapter = new CommentAdapter(this, commentList);
        recyclerViewComments.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewComments.setAdapter(commentAdapter);

        // Setup button click listener
        btnAddComment.setOnClickListener(v -> showAddCommentDialog());

        loadRecipeDetail();
        loadComments();
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
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(RecipeDetailActivity.this, "Không tải được dữ liệu", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadComments() {
        commentsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                commentList.clear();

                for (DataSnapshot commentSnapshot : snapshot.getChildren()) {
                    Comment comment = commentSnapshot.getValue(Comment.class);
                    if (comment != null) {
                        comment.id = commentSnapshot.getKey();
                        commentList.add(comment);
                    }
                }

                commentAdapter.updateComments(commentList);

                // Show/hide no comments message
                if (commentList.isEmpty()) {
                    tvNoComments.setVisibility(View.VISIBLE);
                    recyclerViewComments.setVisibility(View.GONE);
                } else {
                    tvNoComments.setVisibility(View.GONE);
                    recyclerViewComments.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(RecipeDetailActivity.this, "Không thể tải bình luận", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAddCommentDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_comment, null);
        builder.setView(dialogView);

        EditText etAuthorName = dialogView.findViewById(R.id.etAuthorName);
        EditText etComment = dialogView.findViewById(R.id.etComment);
        RatingBar ratingBar = dialogView.findViewById(R.id.ratingBar);
        Button btnSubmit = dialogView.findViewById(R.id.btnSubmit);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);

        AlertDialog dialog = builder.create();

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSubmit.setOnClickListener(v -> {
            String authorName = etAuthorName.getText().toString().trim();
            String content = etComment.getText().toString().trim();
            int rating = (int) ratingBar.getRating();

            if (content.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập nội dung bình luận", Toast.LENGTH_SHORT).show();
                return;
            }

            if (authorName.isEmpty()) {
                authorName = "Ẩn danh";
            }

            // Create new comment
            Comment newComment = new Comment(content, authorName, "user_id_placeholder", rating);

            // Add to Firebase
            commentsRef.push().setValue(newComment)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Đã thêm bình luận thành công!", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi khi thêm bình luận: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
        });

        dialog.show();
    }
}
