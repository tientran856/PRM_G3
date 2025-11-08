package com.example.prm_g3.utils;

import com.example.prm_g3.models.User;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FirebaseDataInitializer {

    public static void initializeSampleUsers() {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");

        // Sample user 1
        User user1 = new User();
        user1.setId("user_001");
        user1.setName("Chef Hưng");
        user1.setEmail("hung@example.com");
        user1.setAvatar_url("https://example.com/avatar.jpg");
        user1.setBio("Đam mê ẩm thực Việt Nam và chia sẻ công thức truyền thống.");
        user1.setJoined_at("2025-11-03T12:00:00Z");

        // Sample user 2
        User user2 = new User();
        user2.setId("user_002");
        user2.setName("Foodie Lan");
        user2.setEmail("lanfoodie@example.com");
        user2.setAvatar_url("https://example.com/avatar2.jpg");
        user2.setBio("Yêu thích nấu ăn gia đình và món chay.");
        user2.setJoined_at("2025-11-03T12:10:00Z");

        // Add to Firebase
        usersRef.child("user_001").setValue(user1);
        usersRef.child("user_002").setValue(user2);
    }
}
