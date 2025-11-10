package com.example.prm_g3.models;

import java.util.Map;

public class Recipe {
    public String title;
    public String description;
    public String category;
    public String tags;
    public String image_url;
    public String video_url;
    public String difficulty;
    public double rating;
    public int total_reviews;
    public String author_id;
    public int prep_time;
    public int cook_time;
    public int servings;
    public String created_at;
    public String updated_at;
    public int sync_status = 0;
    
    // Nested objects for Firebase
    public Map<String, Ingredient> ingredients;
    public Map<String, Step> steps;
    public Map<String, Object> comments; // Comments are handled separately

    public Recipe() {} // Bắt buộc cho Firebase

    public Recipe(String title, String description, String image_url) {
        this.title = title;
        this.description = description;
        this.image_url = image_url;
    }
}


