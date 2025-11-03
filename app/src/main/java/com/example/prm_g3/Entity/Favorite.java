package com.example.prm_g3.Entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "favorites",
        foreignKeys = {
                @ForeignKey(entity = User.class, parentColumns = "id", childColumns = "user_id", onDelete = ForeignKey.CASCADE),
                @ForeignKey(entity = Recipe.class, parentColumns = "id", childColumns = "recipe_id", onDelete = ForeignKey.CASCADE)
        },
        indices = {@Index(value = {"user_id", "recipe_id"}, unique = true)}
)
public class Favorite {
    @PrimaryKey
    @NonNull
    public String id;
    public String user_id;
    public String recipe_id;
    public String created_at;
    public int sync_status = 0;
}

