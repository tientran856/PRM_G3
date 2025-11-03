package com.example.prm_g3.Entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "shopping_lists",
        foreignKeys = {
                @ForeignKey(entity = User.class, parentColumns = "id", childColumns = "user_id", onDelete = ForeignKey.CASCADE),
                @ForeignKey(entity = MealPlan.class, parentColumns = "id", childColumns = "related_plan_id", onDelete = ForeignKey.SET_NULL)
        }
)
public class ShoppingList {
    @PrimaryKey
    @NonNull
    public String id;
    public String user_id;
    public String title;
    public String related_plan_id;
    public String status; // Active / Completed
    public String created_at;
    public int sync_status = 0;
}

