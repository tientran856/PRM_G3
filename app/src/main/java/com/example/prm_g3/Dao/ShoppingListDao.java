package com.example.prm_g3.Dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.prm_g3.Entity.ShoppingList;

import java.util.List;

@Dao
public interface ShoppingListDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(ShoppingList list);

    @Query("SELECT * FROM shopping_lists WHERE user_id = :userId")
    List<ShoppingList> getByUser(String userId);
}

