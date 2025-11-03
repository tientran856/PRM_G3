package com.example.prm_g3.Dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.prm_g3.Entity.Favorite;

import java.util.List;

@Dao
public interface FavoriteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Favorite favorite);

    @Query("SELECT * FROM favorites WHERE user_id = :userId")
    List<Favorite> getByUser(String userId);

    @Query("DELETE FROM favorites WHERE user_id = :userId AND recipe_id = :recipeId")
    void removeFavorite(String userId, String recipeId);
}

