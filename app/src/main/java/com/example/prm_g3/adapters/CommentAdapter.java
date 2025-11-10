package com.example.prm_g3.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.prm_g3.R;
import com.example.prm_g3.UserManager;
import com.example.prm_g3.models.Comment;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {

    private Context context;
    private List<Comment> comments;
    private List<String> commentIds;
    private String recipeId;
    private OnCommentUpdateListener listener;

    public interface OnCommentUpdateListener {
        void onCommentUpdated();
    }

    public CommentAdapter(Context context, List<Comment> comments, List<String> commentIds, String recipeId) {
        this.context = context;
        this.comments = comments;
        this.commentIds = commentIds;
        this.recipeId = recipeId;
    }

    public void setOnCommentUpdateListener(OnCommentUpdateListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_comment, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        Comment comment = comments.get(position);
        String commentId = commentIds.get(position);

        // Set user name
        holder.tvUserName.setText(comment.author_name != null ? comment.author_name : "Người dùng");

        // Set comment content
        holder.tvContent.setText(comment.content);

        // Set rating stars
        holder.llRating.removeAllViews();
        for (int i = 0; i < 5; i++) {
            ImageView star = new ImageView(context);
            if (i < comment.rating) {
                star.setImageResource(android.R.drawable.star_big_on);
                star.setColorFilter(0xFFFFD700);
            } else {
                star.setImageResource(android.R.drawable.star_big_off);
                star.setColorFilter(0xFFCCCCCC);
            }
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(32, 32);
            params.setMargins(0, 0, 4, 0);
            star.setLayoutParams(params);
            holder.llRating.addView(star);
        }

        // Set time
        holder.tvTime.setText(formatTimeAgo(comment.timestamp));

        // Check if this comment belongs to current user
        String currentUserId = UserManager.getInstance().getCurrentUserId();
        if (currentUserId == null) {
            currentUserId = "user_002"; // Default user
        }

        boolean isMyComment = currentUserId.equals(comment.author_id);
        holder.btnMenu.setVisibility(isMyComment ? View.VISIBLE : View.GONE);

        // Set menu click listener for user's own comments
        if (isMyComment) {
            holder.btnMenu.setOnClickListener(v -> showPopupMenu(v, comment, commentId, position));
        }
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    private void showPopupMenu(View view, Comment comment, String commentId, int position) {
        PopupMenu popup = new PopupMenu(context, view);
        popup.getMenuInflater().inflate(R.menu.comment_menu, popup.getMenu());

        popup.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.action_edit) {
                showEditDialog(comment, commentId, position);
                return true;
            } else if (itemId == R.id.action_delete) {
                showDeleteDialog(commentId, position);
                return true;
            }
            return false;
        });

        popup.show();
    }

    private void showEditDialog(Comment comment, String commentId, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_edit_comment, null);

        EditText etContent = dialogView.findViewById(R.id.etContent);
        LinearLayout llRatingStars = dialogView.findViewById(R.id.llRatingStars);

        // Pre-fill current values
        etContent.setText(comment.content);

        // Setup rating stars
        ImageView[] stars = new ImageView[5];
        final int[] selectedRating = {comment.rating};

        for (int i = 0; i < 5; i++) {
            stars[i] = new ImageView(context);
            stars[i].setLayoutParams(new LinearLayout.LayoutParams(60, 60));
            stars[i].setPadding(8, 8, 8, 8);
            updateStarAppearance(stars[i], i < selectedRating[0]);

            final int starIndex = i;
            stars[i].setOnClickListener(v -> {
                selectedRating[0] = starIndex + 1;
                updateAllStars(stars, selectedRating[0]);
            });

            llRatingStars.addView(stars[i]);
        }

        builder.setView(dialogView)
               .setTitle("Chỉnh sửa bình luận")
               .setPositiveButton("Cập nhật", (dialog, which) -> {
                   String newContent = etContent.getText().toString().trim();
                   if (newContent.isEmpty()) {
                       Toast.makeText(context, "Nội dung không được để trống", Toast.LENGTH_SHORT).show();
                       return;
                   }

                   updateComment(commentId, newContent, selectedRating[0], position);
               })
               .setNegativeButton("Hủy", null)
               .show();
    }

    private void updateStarAppearance(ImageView star, boolean selected) {
        if (selected) {
            star.setImageResource(android.R.drawable.star_big_on);
            star.setColorFilter(0xFFFFD700);
        } else {
            star.setImageResource(android.R.drawable.star_big_off);
            star.setColorFilter(0xFFCCCCCC);
        }
    }

    private void updateAllStars(ImageView[] stars, int rating) {
        for (int i = 0; i < stars.length; i++) {
            updateStarAppearance(stars[i], i < rating);
        }
    }

    private void showDeleteDialog(String commentId, int position) {
        new AlertDialog.Builder(context)
                .setTitle("Xóa bình luận")
                .setMessage("Bạn có chắc chắn muốn xóa bình luận này?")
                .setPositiveButton("Xóa", (dialog, which) -> deleteComment(commentId, position))
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void updateComment(String commentId, String newContent, int newRating, int position) {
        DatabaseReference commentRef = FirebaseDatabase.getInstance()
                .getReference("recipes")
                .child(recipeId)
                .child("comments")
                .child(commentId);

        Map<String, Object> updates = new HashMap<>();
        updates.put("content", newContent);
        updates.put("rating", newRating);
        updates.put("created_at", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).format(new Date()));

        commentRef.updateChildren(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(context, "Đã cập nhật bình luận", Toast.LENGTH_SHORT).show();

                    // Update local data
                    Comment comment = comments.get(position);
                    comment.content = newContent;
                    comment.rating = newRating;
                    comment.timestamp = System.currentTimeMillis();
                    notifyItemChanged(position);

                    if (listener != null) {
                        listener.onCommentUpdated();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Lỗi cập nhật bình luận: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void deleteComment(String commentId, int position) {
        DatabaseReference commentRef = FirebaseDatabase.getInstance()
                .getReference("recipes")
                .child(recipeId)
                .child("comments")
                .child(commentId);

        commentRef.removeValue()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(context, "Đã xóa bình luận", Toast.LENGTH_SHORT).show();

                    // Remove from local data
                    comments.remove(position);
                    commentIds.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, comments.size());

                    if (listener != null) {
                        listener.onCommentUpdated();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Lỗi xóa bình luận: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private String formatTimeAgo(long timestamp) {
        long now = System.currentTimeMillis();
        long diff = now - timestamp;

        if (diff < 60000) { // less than 1 minute
            return "Vừa xong";
        } else if (diff < 3600000) { // less than 1 hour
            return (diff / 60000) + " phút trước";
        } else if (diff < 86400000) { // less than 1 day
            return (diff / 3600000) + " giờ trước";
        } else {
            return (diff / 86400000) + " ngày trước";
        }
    }

    public static class CommentViewHolder extends RecyclerView.ViewHolder {
        TextView tvUserName, tvContent, tvTime;
        LinearLayout llRating;
        ImageView btnMenu;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvContent = itemView.findViewById(R.id.tvContent);
            tvTime = itemView.findViewById(R.id.tvTime);
            llRating = itemView.findViewById(R.id.llRating);
            btnMenu = itemView.findViewById(R.id.btnMenu);
        }
    }
}
