package com.example.prm_g3.Entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "users")
public class User {
    @PrimaryKey
    @NonNull
    public String id; // UUID – Firebase UID
    public String name;
    public String email;
    public String avatar_url;
    public String bio;
    public String joined_at;
    public int sync_status = 0;
}
