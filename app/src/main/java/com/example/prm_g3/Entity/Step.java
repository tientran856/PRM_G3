package com.example.prm_g3.Entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "steps",
        foreignKeys = @ForeignKey(
                entity = Recipe.class,
                parentColumns = "id",
                childColumns = "recipe_id",
                onDelete = ForeignKey.CASCADE
        )
)
public class Step {
    @PrimaryKey
    @NonNull
    public String id;
    public String recipe_id;
    public int step_number;
    public String instruction;
    public String image_url;
    public int sync_status = 0;
}

