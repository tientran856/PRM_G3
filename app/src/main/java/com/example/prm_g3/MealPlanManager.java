package com.example.prm_g3;

import android.content.Context;
import android.content.SharedPreferences;
import com.example.prm_g3.models.MealCategory;
import com.example.prm_g3.models.Recipe;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MealPlanManager {
    private static final String PREFS_NAME = "meal_plan_prefs";
    private static final String KEY_MEAL_PLANS = "meal_plans";
    private SharedPreferences prefs;
    private Gson gson;

    public MealPlanManager(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    // 获取日期键 (格式: yyyy-MM-dd)
    private String getDateKey(Calendar date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(date.getTime());
    }

    // 保存某一天的膳食计划
    public void saveMealPlan(Calendar date, List<MealCategory> mealCategories) {
        String dateKey = getDateKey(date);
        String json = gson.toJson(mealCategories);
        prefs.edit().putString(dateKey, json).apply();
    }

    // 加载某一天的膳食计划
    public List<MealCategory> loadMealPlan(Calendar date) {
        String dateKey = getDateKey(date);
        String json = prefs.getString(dateKey, null);
        
        if (json == null) {
            // 如果没有保存的数据，返回默认的空餐段
            return createDefaultMealCategories();
        }

        try {
            Type type = new TypeToken<List<MealCategory>>(){}.getType();
            List<MealCategory> mealCategories = gson.fromJson(json, type);
            
            // 确保所有餐段都存在
            if (mealCategories == null || mealCategories.isEmpty()) {
                return createDefaultMealCategories();
            }
            
            return mealCategories;
        } catch (Exception e) {
            e.printStackTrace();
            return createDefaultMealCategories();
        }
    }

    // 添加食谱到指定餐段
    public void addRecipeToMeal(Calendar date, String mealCategoryName, Recipe recipe) {
        List<MealCategory> mealCategories = loadMealPlan(date);
        
        for (MealCategory category : mealCategories) {
            if (category.name.equals(mealCategoryName)) {
                if (category.recipes == null) {
                    category.recipes = new ArrayList<>();
                }
                // 检查是否已存在（避免重复添加）
                boolean exists = false;
                for (Recipe r : category.recipes) {
                    if (r.title != null && r.title.equals(recipe.title)) {
                        exists = true;
                        break;
                    }
                }
                if (!exists) {
                    category.recipes.add(recipe);
                }
                break;
            }
        }
        
        saveMealPlan(date, mealCategories);
    }

    // 从指定餐段移除食谱
    public void removeRecipeFromMeal(Calendar date, String mealCategoryName, int recipeIndex) {
        List<MealCategory> mealCategories = loadMealPlan(date);
        
        for (MealCategory category : mealCategories) {
            if (category.name.equals(mealCategoryName)) {
                if (category.recipes != null && recipeIndex >= 0 && recipeIndex < category.recipes.size()) {
                    category.recipes.remove(recipeIndex);
                }
                break;
            }
        }
        
        saveMealPlan(date, mealCategories);
    }

    // 创建默认的餐段列表
    private List<MealCategory> createDefaultMealCategories() {
        List<MealCategory> categories = new ArrayList<>();
        categories.add(new MealCategory("Bữa sáng", new ArrayList<>()));
        categories.add(new MealCategory("Bữa trưa", new ArrayList<>()));
        categories.add(new MealCategory("Bữa tối", new ArrayList<>()));
        categories.add(new MealCategory("Bữa phụ", new ArrayList<>()));
        return categories;
    }

    // 获取购物清单（所有已添加食谱的食材）
    public List<String> getShoppingList(Calendar startDate, Calendar endDate) {
        List<String> shoppingList = new ArrayList<>();
        // TODO: 实现购物清单逻辑（需要从食谱中提取食材）
        return shoppingList;
    }
}

