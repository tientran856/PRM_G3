package com.example.prm_g3.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.prm_g3.R;
import com.example.prm_g3.models.MealCategory;
import com.example.prm_g3.models.Recipe;

import java.util.List;

public class MealCategoryAdapter extends RecyclerView.Adapter<MealCategoryAdapter.ViewHolder> {
    private Context context;
    private List<MealCategory> categories;
    private OnAddRecipeClickListener onAddRecipeClickListener;
    private OnRemoveRecipeClickListener onRemoveRecipeClickListener;

    public interface OnAddRecipeClickListener {
        void onAddRecipeClick(String mealCategoryName);
    }

    public interface OnRemoveRecipeClickListener {
        void onRemoveRecipeClick(String mealCategoryName, int recipeIndex);
    }

    public MealCategoryAdapter(Context context, List<MealCategory> categories) {
        this.context = context;
        this.categories = categories;
    }

    public void setOnAddRecipeClickListener(OnAddRecipeClickListener listener) {
        this.onAddRecipeClickListener = listener;
    }

    public void setOnRemoveRecipeClickListener(OnRemoveRecipeClickListener listener) {
        this.onRemoveRecipeClickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_meal_category, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvMealName;
        Button btnAddDish;
        LinearLayout containerDishes;
        TextView tvEmptyMessage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMealName = itemView.findViewById(R.id.tvMealName);
            btnAddDish = itemView.findViewById(R.id.btnAddDish);
            containerDishes = itemView.findViewById(R.id.containerDishes);
            tvEmptyMessage = itemView.findViewById(R.id.tvEmptyMessage);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MealCategory category = categories.get(position);
        holder.tvMealName.setText(category.name);

        // 清除之前的视图
        holder.containerDishes.removeAllViews();

        if (category.recipes == null || category.recipes.isEmpty()) {
            holder.tvEmptyMessage.setVisibility(View.VISIBLE);
            holder.containerDishes.setVisibility(View.GONE);
        } else {
            holder.tvEmptyMessage.setVisibility(View.GONE);
            holder.containerDishes.setVisibility(View.VISIBLE);
            
            // 添加所有食谱项
            for (int i = 0; i < category.recipes.size(); i++) {
                Recipe recipe = category.recipes.get(i);
                View recipeView = createRecipeView(recipe, category.name, i);
                holder.containerDishes.addView(recipeView);
            }
        }

        holder.btnAddDish.setOnClickListener(v -> {
            if (onAddRecipeClickListener != null) {
                onAddRecipeClickListener.onAddRecipeClick(category.name);
            }
        });
    }

    private View createRecipeView(Recipe recipe, String mealCategoryName, int recipeIndex) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_meal_recipe, null);
        
        ImageView imgRecipe = view.findViewById(R.id.imgRecipe);
        TextView tvRecipeTitle = view.findViewById(R.id.tvRecipeTitle);
        TextView tvTime = view.findViewById(R.id.tvTime);
        TextView tvRating = view.findViewById(R.id.tvRating);
        ImageButton btnRemove = view.findViewById(R.id.btnRemove);

        // 设置食谱信息
        tvRecipeTitle.setText(recipe.title != null ? recipe.title : "Không có tên");
        
        // 设置时间
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

        // 设置评分
        String ratingStr = String.format("%.1f", recipe.rating);
        tvRating.setText(ratingStr);

        // 加载图片
        if (recipe.image_url != null && !recipe.image_url.isEmpty()) {
            Glide.with(context)
                    .load(recipe.image_url)
                    .placeholder(R.drawable.ic_home)
                    .into(imgRecipe);
        }

        // 设置删除按钮
        btnRemove.setOnClickListener(v -> {
            if (onRemoveRecipeClickListener != null) {
                onRemoveRecipeClickListener.onRemoveRecipeClick(mealCategoryName, recipeIndex);
            }
        });

        return view;
    }
}
