package com.example.prm_g3.Dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.prm_g3.Entity.ShoppingItem;

import java.util.List;

@Dao
public interface ShoppingItemDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(ShoppingItem item);

    @Query("SELECT * FROM shopping_items WHERE list_id = :listId")
    List<ShoppingItem> getByList(String listId);

    @Query("UPDATE shopping_items SET is_bought = 1 WHERE id = :itemId")
    void markAsBought(String itemId);
}

