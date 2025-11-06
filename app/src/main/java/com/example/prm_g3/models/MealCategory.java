package com.example.prm_g3.models;

import java.util.List;

public class MealCategory {
    public String name;
    public List<Recipe> recipes;

    public MealCategory() {
    }

    public MealCategory(String name, List<Recipe> recipes) {
        this.name = name;
        this.recipes = recipes;
    }
}
