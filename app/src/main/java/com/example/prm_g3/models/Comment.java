package com.example.prm_g3.models;

public class Comment {
    public String id;
    public String content;
    public String author_name;
    public String author_id;
    public int rating;
    public long timestamp;

    public Comment() {} // Bắt buộc cho Firebase

    public Comment(String content, String author_name, String author_id, int rating) {
        this.content = content;
        this.author_name = author_name;
        this.author_id = author_id;
        this.rating = rating;
        this.timestamp = System.currentTimeMillis();
    }

    public Comment(String id, String content, String author_name, String author_id, int rating, long timestamp) {
        this.id = id;
        this.content = content;
        this.author_name = author_name;
        this.author_id = author_id;
        this.rating = rating;
        this.timestamp = timestamp;
    }
}
