package com.example.prm_g3.models;

import java.util.Map;

public class Recipe {
    public String title;
    public String description;
    public String category;
    public String image_url;
    public String qr_code_url;  // URL của mã QR code đã upload
    public String share_link;  // Link chia sẻ từ get-qr.com hoặc link tùy chỉnh
    public String difficulty;
    public double rating;
    public String author_id;
    public int prep_time;
    public int cook_time;

    public Recipe() {} // Bắt buộc cho Firebase

    public Recipe(String title, String description, String image_url) {
        this.title = title;
        this.description = description;
        this.image_url = image_url;
    }
}


