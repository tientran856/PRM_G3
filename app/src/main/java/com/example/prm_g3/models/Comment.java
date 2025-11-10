package com.example.prm_g3.models;

public class Comment {
    public String id;
    public String content;
    public String author_name;
    public String author_id;
    public String user_id;
    public String user_name;
    public int rating;
    public long timestamp;
    public String created_at;
    public int sync_status;

    public Comment() {} // Bắt buộc cho Firebase

    public Comment(String content, String author_name, String author_id, int rating) {
        this.content = content;
        this.author_name = author_name;
        this.author_id = author_id;
        this.user_id = author_id;
        this.user_name = author_name;
        this.rating = rating;
        this.timestamp = System.currentTimeMillis();
        this.sync_status = 1;
    }

    public Comment(String id, String content, String author_name, String author_id, int rating, long timestamp) {
        this.id = id;
        this.content = content;
        this.author_name = author_name;
        this.author_id = author_id;
        this.user_id = author_id;
        this.user_name = author_name;
        this.rating = rating;
        this.timestamp = timestamp;
        this.sync_status = 1;
    }

    // Constructor từ Firebase data
    public Comment(String id, String content, String user_name, String user_id, int rating, String created_at, int sync_status) {
        this.id = id;
        this.content = content;
        this.user_name = user_name;
        this.user_id = user_id;
        this.author_name = user_name;
        this.author_id = user_id;
        this.rating = rating;
        this.created_at = created_at;
        this.sync_status = sync_status;
        // Convert created_at to timestamp if needed
        this.timestamp = System.currentTimeMillis();
    }
}
