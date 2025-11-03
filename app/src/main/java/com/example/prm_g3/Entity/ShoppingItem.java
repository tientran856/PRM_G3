package com.example.prm_g3.Entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "shopping_items",
        foreignKeys = @ForeignKey(
                entity = ShoppingList.class,
                parentColumns = "id",
                childColumns = "list_id",
                onDelete = ForeignKey.CASCADE
        )
)
public class ShoppingItem {
    @PrimaryKey
    @NonNull
    public String id;
    public String list_id;
    public String name;
    public String quantity;
    public int is_bought;
    public int sync_status = 0;
}

