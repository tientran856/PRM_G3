package com.example.prm_g3.Entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "comments",
        foreignKeys = {
                @ForeignKey(entity = Recipe.class, parentColumns = "id", childColumns = "recipe_id", onDelete = ForeignKey.CASCADE),
                @ForeignKey(entity = User.class, parentColumns = "id", childColumns = "user_id", onDelete = ForeignKey.CASCADE)
        }
)
public class Comment {
    @PrimaryKey
    @NonNull
    public String id;
    public String recipe_id;
    public String user_id;
    public String content;
    public int rating;
    public String created_at;
    public int sync_status = 0;
}

