package com.example.prm_g3.Dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.prm_g3.Entity.MealPlanDay;

import java.util.List;

@Dao
public interface MealPlanDayDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(MealPlanDay day);

    @Query("SELECT * FROM meal_plan_days WHERE meal_plan_id = :planId ORDER BY date ASC")
    List<MealPlanDay> getByPlan(String planId);
}
