package com.example.prm_g3;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import com.example.prm_g3.Entity.*;
import com.example.prm_g3.Dao.*;

@Database(
        entities = {
                User.class, Recipe.class, Ingredient.class, Step.class,
                Comment.class, Favorite.class, MealPlan.class, MealPlanDay.class,
                ShoppingList.class, ShoppingItem.class, IngredientInventory.class
        },
        version = 1
)
public abstract class AppDatabase extends RoomDatabase {
    public abstract UserDao userDao();
    public abstract RecipeDao recipeDao();
    public abstract IngredientDao ingredientDao();
    public abstract StepDao stepDao();
    public abstract CommentDao commentDao();
    public abstract FavoriteDao favoriteDao();
    public abstract MealPlanDao mealPlanDao();
    public abstract MealPlanDayDao mealPlanDayDao();
    public abstract ShoppingListDao shoppingListDao();
    public abstract ShoppingItemDao shoppingItemDao();
    public abstract IngredientInventoryDao ingredientInventoryDao();
}

