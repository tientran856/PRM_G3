package com.example.prm_g3.models;

public class Notification {
    public String id;
    public String userId;
    public String recipeId;
    public String recipeTitle;
    public String commenterName;
    public String commentContent;
    public String type; // "comment", "like", etc.
    public long timestamp;
    public boolean isRead;

    public Notification() {
    } // Bắt buộc cho Firebase

    public Notification(String userId, String recipeId, String recipeTitle,
            String commenterName, String commentContent, String type) {
        this.userId = userId;
        this.recipeId = recipeId;
        this.recipeTitle = recipeTitle;
        this.commenterName = commenterName;
        this.commentContent = commentContent;
        this.type = type;
        this.timestamp = System.currentTimeMillis();
        this.isRead = false;
    }
}
