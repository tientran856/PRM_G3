package com.example.prm_g3.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.prm_g3.R;
import com.example.prm_g3.RecipeDetailActivity;
import com.example.prm_g3.models.Recipe;
import com.example.prm_g3.FavoritesManager;
import java.util.ArrayList;
import java.util.List;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.ViewHolder> {
    private Context context;
    private List<Recipe> recipes;
    private List<String> recipeIds;
    private FavoritesManager favoritesManager;

    public RecipeAdapter(Context context, List<Recipe> recipes) {
        this.context = context;
        this.recipes = recipes;
        this.recipeIds = new ArrayList<>();
        this.favoritesManager = new FavoritesManager(context);

        // Generate default IDs if none provided
        for (int i = 0; i < recipes.size(); i++) {
            this.recipeIds.add("recipe_00" + (i + 1));
        }
    }

    public RecipeAdapter(Context context, List<Recipe> recipes, List<String> recipeIds) {
        this.context = context;
        this.recipes = recipes;
        this.recipeIds = recipeIds != null ? recipeIds : new ArrayList<>();
        this.favoritesManager = new FavoritesManager(context);
    }

    public void refreshFavorites() {
        favoritesManager.refreshForCurrentUser();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_recipe, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public int getItemCount() {
        return recipes.size();
    }

    private void updateFavoriteButton(ImageView btnFavorite, String recipeId) {
        if (favoritesManager.isFavorite(recipeId)) {
            btnFavorite.setImageResource(R.drawable.ic_heart_filled);
            btnFavorite.setColorFilter(0xFFFF6B6B); // Red color for favorited
        } else {
            btnFavorite.setImageResource(R.drawable.ic_heart_outline);
            btnFavorite.setColorFilter(0xFF666666); // Gray color for not favorited
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgRecipe, btnFavorite;
        TextView tvTitle, tvTime, tvDifficulty, tvRating;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgRecipe = itemView.findViewById(R.id.imgRecipe);
            btnFavorite = itemView.findViewById(R.id.btnFavorite);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvDifficulty = itemView.findViewById(R.id.tvDifficulty);
            tvRating = itemView.findViewById(R.id.tvRating);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Recipe r = recipes.get(position);
        holder.tvTitle.setText(r.title);

        // Format time
        int totalTime = r.prep_time + r.cook_time;
        if (totalTime >= 60) {
            int hours = totalTime / 60;
            int minutes = totalTime % 60;
            if (minutes > 0) {
                holder.tvTime.setText(hours + " giờ " + minutes + " phút");
            } else {
                holder.tvTime.setText(hours + " giờ");
            }
        } else {
            holder.tvTime.setText(totalTime + " phút");
        }

        holder.tvDifficulty.setText(r.difficulty);
        holder.tvRating.setText(String.format("%.1f", r.rating));

        Glide.with(context)
                .load(r.image_url)
                .placeholder(R.drawable.ic_home)
                .into(holder.imgRecipe);

        // Set up favorite button
        String recipeId = recipeIds.get(position);
        updateFavoriteButton(holder.btnFavorite, recipeId);

        // Set favorite button click listener
        holder.btnFavorite.setOnClickListener(v -> {
            if (favoritesManager.isFavorite(recipeId)) {
                favoritesManager.removeFromFavorites(recipeId);
            } else {
                favoritesManager.addToFavorites(recipeId);
            }
            updateFavoriteButton(holder.btnFavorite, recipeId);
        });

        // 👇 Khi click, mở RecipeDetailActivity
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, RecipeDetailActivity.class);
            intent.putExtra("recipeId", recipeId);
            context.startActivity(intent);
        });
    }

}
