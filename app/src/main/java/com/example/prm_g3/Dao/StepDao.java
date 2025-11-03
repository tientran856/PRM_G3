package com.example.prm_g3.Dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import com.example.prm_g3.Entity.Step;
import java.util.List;

@Dao
public interface StepDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Step step);

    @Query("SELECT * FROM steps WHERE recipe_id = :recipeId ORDER BY step_number ASC")
    List<Step> getByRecipe(String recipeId);
}

