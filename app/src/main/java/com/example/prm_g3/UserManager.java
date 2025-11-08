package com.example.prm_g3;

import com.example.prm_g3.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class UserManager {
    private static UserManager instance;
    private User currentUser;
    private FirebaseAuth mAuth;

    private UserManager() {
        mAuth = FirebaseAuth.getInstance();
    }

    public static UserManager getInstance() {
        if (instance == null) {
            instance = new UserManager();
        }
        return instance;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    public String getCurrentUserId() {
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        return firebaseUser != null ? firebaseUser.getUid() : null;
    }

    public boolean isLoggedIn() {
        return mAuth.getCurrentUser() != null;
    }

    public String getCurrentUserEmail() {
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        return firebaseUser != null ? firebaseUser.getEmail() : null;
    }

    public String getCurrentUserDisplayName() {
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        return firebaseUser != null ? firebaseUser.getDisplayName() : null;
    }

    public void clearUser() {
        currentUser = null;
    }
}
