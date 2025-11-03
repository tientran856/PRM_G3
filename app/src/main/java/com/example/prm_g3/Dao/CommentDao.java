package com.example.prm_g3.Dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import com.example.prm_g3.Entity.Comment;
import java.util.List;

@Dao
public interface CommentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Comment comment);

    @Query("SELECT * FROM comments WHERE recipe_id = :recipeId ORDER BY created_at DESC")
    List<Comment> getByRecipe(String recipeId);
}
