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
import java.util.List;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.ViewHolder> {
    private Context context;
    private List<Recipe> recipes;

    public RecipeAdapter(Context context, List<Recipe> recipes) {
        this.context = context;
        this.recipes = recipes;
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
        ImageView imgRecipe;
        TextView tvTitle, tvTime, tvDifficulty, tvRating;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgRecipe = itemView.findViewById(R.id.imgRecipe);
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

        // 👇 Khi click, mở RecipeDetailActivity
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, RecipeDetailActivity.class);
            intent.putExtra("recipeId", "recipe_00" + (position + 1)); // id từ Firebase
            context.startActivity(intent);
        });
    }

}
