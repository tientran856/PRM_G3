package com.example.prm_g3.Dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.prm_g3.Entity.MealPlan;

import java.util.List;

@Dao
public interface MealPlanDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(MealPlan plan);

    @Query("SELECT * FROM meal_plans WHERE user_id = :userId")
    List<MealPlan> getByUser(String userId);
}

