package com.example.prm_g3.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.prm_g3.R;
import com.example.prm_g3.activity.RecipeDetailActivity;
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

    public void refreshFavorites() {
        if (favoritesManager != null) {
            favoritesManager.refreshForCurrentUser();
            notifyDataSetChanged();
        }
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Recipe r = recipes.get(position);
        holder.tvTitle.setText(r.title);

        int totalTime = r.prep_time + r.cook_time;
        holder.tvTime.setText(totalTime + " phút");
        holder.tvDifficulty.setText(r.difficulty);
        holder.tvRating.setText(String.format("%.1f", r.rating));

        // ✅ Fix load ảnh: hỗ trợ cả content:// (local) và https:// (Firebase)
        if (r.image_url != null && !r.image_url.isEmpty()) {
            if (r.image_url.startsWith("content://") || r.image_url.startsWith("file://")) {
                Glide.with(context)
                        .load(Uri.parse(r.image_url))
                        .placeholder(R.drawable.placeholder)
                        .error(R.drawable.placeholder)
                        .into(holder.imgRecipe);
            } else {
                Glide.with(context)
                        .load(r.image_url)
                        .placeholder(R.drawable.placeholder)
                        .error(R.drawable.placeholder)
                        .into(holder.imgRecipe);
            }
        } else {
            holder.imgRecipe.setImageResource(R.drawable.placeholder);
        }


        String recipeId = recipeIds.get(position);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, RecipeDetailActivity.class);
            intent.putExtra("recipeId", recipeId);
            context.startActivity(intent);
        });
    }
}
