package com.example.prm_g3.Entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "meal_plan_days",
        foreignKeys = {
                @ForeignKey(entity = MealPlan.class, parentColumns = "id", childColumns = "meal_plan_id", onDelete = ForeignKey.CASCADE),
                @ForeignKey(entity = Recipe.class, parentColumns = "id", childColumns = "recipe_id", onDelete = ForeignKey.CASCADE)
        }
)
public class MealPlanDay {
    @PrimaryKey
    @NonNull
    public String id;
    public String meal_plan_id;
    public String date;
    public String meal_type;
    public String recipe_id;
    public int sync_status = 0;
}
