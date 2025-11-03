package com.example.prm_g3.Dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.prm_g3.Entity.IngredientInventory;

import java.util.List;

@Dao
public interface IngredientInventoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(IngredientInventory inv);

    @Query("SELECT * FROM ingredient_inventory WHERE user_id = :userId")
    List<IngredientInventory> getByUser(String userId);

    @Query("DELETE FROM ingredient_inventory WHERE id = :id")
    void deleteById(String id);
}

