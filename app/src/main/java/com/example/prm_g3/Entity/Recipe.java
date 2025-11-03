package com.example.prm_g3.Entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "recipes",
        foreignKeys = @ForeignKey(
                entity = User.class,
                parentColumns = "id",
                childColumns = "author_id",
                onDelete = ForeignKey.CASCADE
        )
)
public class Recipe {
    @PrimaryKey
    @NonNull
    public String id;
    public String title;
    public String description;
    public String category;
    public String tags;
    public String image_url;
    public String video_url;
    public String author_id;
    public int prep_time;
    public int cook_time;
    public int servings;
    public String difficulty;
    public float rating;
    public int total_reviews;
    public String created_at;
    public String updated_at;
    public int sync_status = 0;
}
