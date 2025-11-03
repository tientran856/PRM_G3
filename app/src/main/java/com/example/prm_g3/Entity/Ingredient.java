package com.example.prm_g3.Entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "ingredients",
        foreignKeys = @ForeignKey(
                entity = Recipe.class,
                parentColumns = "id",
                childColumns = "recipe_id",
                onDelete = ForeignKey.CASCADE
        )
)
public class Ingredient {
    @PrimaryKey
    @NonNull
    public String id;
    public String recipe_id;
    public String name;
    public String quantity;
    public int sync_status = 0;
}
