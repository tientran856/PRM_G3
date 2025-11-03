package com.example.prm_g3.Dao;


import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import com.example.prm_g3.Entity.Ingredient;
import java.util.List;

@Dao
public interface IngredientDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Ingredient ingredient);

    @Query("SELECT * FROM ingredients WHERE recipe_id = :recipeId")
    List<Ingredient> getByRecipe(String recipeId);

    @Delete
    void delete(Ingredient ingredient);
}