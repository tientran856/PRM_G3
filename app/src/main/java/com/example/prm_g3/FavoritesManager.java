package com.example.prm_g3;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.HashSet;
import java.util.Set;

public class FavoritesManager {
    private static final String PREF_NAME = "favorites";
    private static final String KEY_FAVORITE_RECIPES = "favorite_recipes";

    private SharedPreferences sharedPreferences;

    public FavoritesManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public boolean isFavorite(String recipeId) {
        Set<String> favorites = getFavoriteRecipes();
        return favorites.contains(recipeId);
    }

    public void addToFavorites(String recipeId) {
        Set<String> favorites = getFavoriteRecipes();
        favorites.add(recipeId);
        saveFavoriteRecipes(favorites);
    }

    public void removeFromFavorites(String recipeId) {
        Set<String> favorites = getFavoriteRecipes();
        favorites.remove(recipeId);
        saveFavoriteRecipes(favorites);
    }

    public void toggleFavorite(String recipeId) {
        if (isFavorite(recipeId)) {
            removeFromFavorites(recipeId);
        } else {
            addToFavorites(recipeId);
        }
    }

    public Set<String> getFavoriteRecipes() {
        return new HashSet<>(sharedPreferences.getStringSet(KEY_FAVORITE_RECIPES, new HashSet<>()));
    }

    private void saveFavoriteRecipes(Set<String> favorites) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putStringSet(KEY_FAVORITE_RECIPES, favorites);
        editor.apply();
    }
}
