package com.example.prm_g3;

import android.content.Context;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.prm_g3.R;
import com.example.prm_g3.Entity.Recipe;
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
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Recipe r = recipes.get(position);
        holder.tvRecipeTitle.setText(r.title);
        holder.tvRating.setText("⭐ " + r.rating);
        holder.tvInfo.setText("⏱ " + r.cook_time + " phút  •  " + r.difficulty);

        // Tạm thời ảnh tĩnh, có thể sau này load bằng Glide/Picasso
        holder.imgRecipe.setImageResource(R.drawable.pho_bo);
    }

    @Override
    public int getItemCount() {
        return recipes.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgRecipe;
        TextView tvRecipeTitle, tvRating, tvInfo;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgRecipe = itemView.findViewById(R.id.imgRecipe);
            tvRecipeTitle = itemView.findViewById(R.id.tvRecipeTitle);
            tvRating = itemView.findViewById(R.id.tvRating);
            tvInfo = itemView.findViewById(R.id.tvInfo);
        }
    }
}

