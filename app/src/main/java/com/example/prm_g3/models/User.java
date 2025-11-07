package com.example.prm_g3.models;

public class User {
    private String id, name, email, avatar_url, bio, joined_at;

    public User() {}

    public User(String id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.joined_at = String.valueOf(System.currentTimeMillis());
    }

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getAvatar_url() { return avatar_url; }
    public String getBio() { return bio; }
    public String getJoined_at() { return joined_at; }
}