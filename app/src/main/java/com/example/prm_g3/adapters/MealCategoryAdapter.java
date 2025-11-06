package com.example.prm_g3.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.prm_g3.R;
import com.example.prm_g3.models.MealCategory;

import java.util.List;

public class MealCategoryAdapter extends RecyclerView.Adapter<MealCategoryAdapter.ViewHolder> {
    private Context context;
    private List<MealCategory> categories;

    public MealCategoryAdapter(Context context, List<MealCategory> categories) {
        this.context = context;
        this.categories = categories;
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

        if (category.recipes == null || category.recipes.isEmpty()) {
            holder.tvEmptyMessage.setVisibility(View.VISIBLE);
            holder.containerDishes.setVisibility(View.GONE);
        } else {
            holder.tvEmptyMessage.setVisibility(View.GONE);
            holder.containerDishes.setVisibility(View.VISIBLE);
            // TODO: Add recipe items to containerDishes
        }

        holder.btnAddDish.setOnClickListener(v -> {
            // TODO: Show dialog to add recipe to this meal
            android.widget.Toast.makeText(context, "Thêm món vào " + category.name, android.widget.Toast.LENGTH_SHORT)
                    .show();
        });
    }
}
