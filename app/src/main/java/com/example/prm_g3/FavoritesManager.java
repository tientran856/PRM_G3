package com.example.prm_g3;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import com.example.prm_g3.UserManager;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class FavoritesManager {
    private static final String TAG = "FavoritesManager";

    private DatabaseReference favoritesRef;
    private Set<String> cachedFavorites;
    private String currentUserId;
    private ValueEventListener currentListener;

    public FavoritesManager(Context context) {
        favoritesRef = FirebaseDatabase.getInstance().getReference("favorites");
        cachedFavorites = new HashSet<>();
        currentUserId = UserManager.getInstance().getCurrentUserId();
        if (currentUserId != null) {
            loadFavoritesFromFirebase();
        }
    }

    private void loadFavoritesFromFirebase() {
        if (currentUserId == null) {
            Log.w(TAG, "No current user ID, cannot load favorites");
            return;
        }

        try {
            // Remove previous listener if exists
            if (currentListener != null) {
                favoritesRef.orderByChild("user_id").equalTo(currentUserId)
                        .removeEventListener(currentListener);
            }

            currentListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    try {
                        cachedFavorites.clear();
                        for (DataSnapshot child : snapshot.getChildren()) {
                            String recipeId = child.child("recipe_id").getValue(String.class);
                            if (recipeId != null) {
                                cachedFavorites.add(recipeId);
                            }
                        }
                        Log.d(TAG, "Loaded " + cachedFavorites.size() + " favorites from Firebase");

                        // Notify listener of changes
                        if (favoritesChangedListener != null) {
                            favoritesChangedListener.onFavoritesChanged();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error processing favorites data: ", e);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e(TAG, "Failed to load favorites: " + error.getMessage());
                }
            };

            favoritesRef.orderByChild("user_id").equalTo(currentUserId)
                    .addValueEventListener(currentListener);
        } catch (Exception e) {
            Log.e(TAG, "Error setting up Firebase listener: ", e);
        }
    }

    public boolean isFavorite(String recipeId) {
        return currentUserId != null && cachedFavorites.contains(recipeId);
    }

    public void addToFavorites(String recipeId) {
        Log.d(TAG, "Adding to favorites: " + recipeId);

        if (currentUserId == null) {
            Log.w(TAG, "No current user ID, cannot add to favorites");
            return;
        }

        // Check if already favorite to avoid duplicates
        if (isFavorite(recipeId)) {
            Log.d(TAG, "Recipe already in favorites: " + recipeId);
            return;
        }

        // Create unique key for favorite record
        String favoriteId = favoritesRef.push().getKey();

        if (favoriteId != null && currentUserId != null) {
            Map<String, Object> favoriteData = new HashMap<>();
            favoriteData.put("user_id", currentUserId);
            favoriteData.put("recipe_id", recipeId);
            favoriteData.put("created_at", new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.getDefault()).format(new java.util.Date()));
            favoriteData.put("sync_status", 1);

            favoritesRef.child(favoriteId).setValue(favoriteData)
                    .addOnSuccessListener(aVoid -> {
                        cachedFavorites.add(recipeId);
                        Log.d(TAG, "Successfully added to favorites: " + recipeId);
                    })
                    .addOnFailureListener(e -> Log.e(TAG, "Failed to add to favorites: " + e.getMessage()));
        }
    }

    public void removeFromFavorites(String recipeId) {
        Log.d(TAG, "Removing from favorites: " + recipeId);

        if (currentUserId == null) {
            Log.w(TAG, "No current user ID, cannot remove favorite");
            return;
        }

        // Find the favorite record to remove
        favoritesRef.orderByChild("user_id").equalTo(currentUserId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot child : snapshot.getChildren()) {
                    String childRecipeId = child.child("recipe_id").getValue(String.class);
                    if (recipeId.equals(childRecipeId)) {
                        child.getRef().removeValue()
                                .addOnSuccessListener(aVoid -> {
                                    cachedFavorites.remove(recipeId);
                                    Log.d(TAG, "Successfully removed from favorites: " + recipeId);
                                })
                                .addOnFailureListener(e ->
                                    Log.e(TAG, "Failed to remove from favorites: " + e.getMessage()));
                        break;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to query favorites for removal: " + error.getMessage());
            }
        });
    }

    public void toggleFavorite(String recipeId) {
        Log.d(TAG, "Toggling favorite for recipe: " + recipeId);
        if (isFavorite(recipeId)) {
            removeFromFavorites(recipeId);
        } else {
            addToFavorites(recipeId);
        }
    }

    public Set<String> getFavoriteRecipes() {
        Log.d(TAG, "Getting favorites: " + cachedFavorites.size() + " items");
        return new HashSet<>(cachedFavorites);
    }

    // Interface for listening to favorites changes
    public interface OnFavoritesChangedListener {
        void onFavoritesChanged();
    }

    private OnFavoritesChangedListener favoritesChangedListener;

    public void setOnFavoritesChangedListener(OnFavoritesChangedListener listener) {
        this.favoritesChangedListener = listener;
    }

    // Method to refresh favorites when user changes
    public void refreshForCurrentUser() {
        String newUserId = UserManager.getInstance().getCurrentUserId();
        if (!Objects.equals(currentUserId, newUserId)) {
            currentUserId = newUserId;
            cachedFavorites.clear();
            if (currentUserId != null) {
                loadFavoritesFromFirebase();
            } else {
                // User logged out, notify listeners
                if (favoritesChangedListener != null) {
                    favoritesChangedListener.onFavoritesChanged();
                }
            }
        }
    }

    // Method to cleanup listeners
    public void cleanup() {
        if (currentListener != null && currentUserId != null) {
            favoritesRef.orderByChild("user_id").equalTo(currentUserId)
                    .removeEventListener(currentListener);
            currentListener = null;
        }
    }

}
