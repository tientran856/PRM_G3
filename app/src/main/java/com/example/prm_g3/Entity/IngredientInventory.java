package com.example.prm_g3.Entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "ingredient_inventory",
        foreignKeys = @ForeignKey(
                entity = User.class,
                parentColumns = "id",
                childColumns = "user_id",
                onDelete = ForeignKey.CASCADE
        )
)
public class IngredientInventory {
    @PrimaryKey
    @NonNull
    public String id;
    public String user_id;
    public String name;
    public String quantity;
    public String unit;
    public String updated_at;
    public int sync_status = 0;
}

