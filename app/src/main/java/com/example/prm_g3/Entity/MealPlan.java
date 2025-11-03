package com.example.prm_g3.Entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "meal_plans",
        foreignKeys = @ForeignKey(
                entity = User.class,
                parentColumns = "id",
                childColumns = "user_id",
                onDelete = ForeignKey.CASCADE
        )
)
public class MealPlan {
    @PrimaryKey
    @NonNull
    public String id;
    public String user_id;
    public String title;
    public String start_date;
    public String end_date;
    public String created_at;
    public int sync_status = 0;
}

