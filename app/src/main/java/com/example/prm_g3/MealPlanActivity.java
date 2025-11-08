package com.example.prm_g3;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.prm_g3.adapters.DayAdapter;
import com.example.prm_g3.adapters.MealCategoryAdapter;
import com.example.prm_g3.adapters.RecipeGridAdapter;
import com.example.prm_g3.models.MealCategory;
import com.example.prm_g3.models.Recipe;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MealPlanActivity extends AppCompatActivity {

    private TextView tvMonthYear;
    private RecyclerView rvDays;
    private RecyclerView rvMealCategories;
    private ImageButton btnPrevMonth, btnNextMonth;
    private ExtendedFloatingActionButton fabShoppingList;

    private Calendar currentDate;
    private Calendar selectedDate;
    private MealCategoryAdapter adapter;
    private DayAdapter dayAdapter;
    private List<MealCategory> mealCategories;
    private List<Calendar> daysOfWeek;
    private MealPlanManager mealPlanManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meal_plan);

        currentDate = Calendar.getInstance();
        selectedDate = (Calendar) currentDate.clone();
        mealPlanManager = new MealPlanManager(this);

        initViews();
        setupDateSelector();
        loadMealPlanForSelectedDate();
        setupBottomNav();
        setupFAB();
    }

    private void initViews() {
        tvMonthYear = findViewById(R.id.tvMonthYear);
        btnPrevMonth = findViewById(R.id.btnPrevMonth);
        btnNextMonth = findViewById(R.id.btnNextMonth);
        rvDays = findViewById(R.id.rvDays);
        rvMealCategories = findViewById(R.id.rvMealCategories);
        fabShoppingList = findViewById(R.id.fabShoppingList);
    }

    private void setupDateSelector() {
        updateMonthYearDisplay();

        btnPrevMonth.setOnClickListener(v -> {
            // Lùi 1 tuần (7 ngày)
            currentDate.add(Calendar.DAY_OF_MONTH, -7);
            updateMonthYearDisplay();
            updateDaysList();
        });

        btnNextMonth.setOnClickListener(v -> {
            // Tiến 1 tuần (7 ngày)
            currentDate.add(Calendar.DAY_OF_MONTH, 7);
            updateMonthYearDisplay();
            updateDaysList();
        });

        updateDaysList();
    }

    private void updateMonthYearDisplay() {
        // Format: "Tháng 11, 2025"
        int month = currentDate.get(Calendar.MONTH) + 1; // Calendar.MONTH is 0-based
        int year = currentDate.get(Calendar.YEAR);
        tvMonthYear.setText("Tháng " + month + ", " + year);
    }

    private void updateDaysList() {
        daysOfWeek = new ArrayList<>();

        // Get first day of current week (Sunday = 1)
        Calendar weekStart = (Calendar) currentDate.clone();
        int currentDayOfWeek = weekStart.get(Calendar.DAY_OF_WEEK);
        // Go back to Sunday
        weekStart.add(Calendar.DAY_OF_MONTH, -(currentDayOfWeek - 1));

        // Get 7 days of the week starting from Sunday
        for (int i = 0; i < 7; i++) {
            Calendar day = (Calendar) weekStart.clone();
            day.add(Calendar.DAY_OF_MONTH, i);
            daysOfWeek.add(day);
        }

        dayAdapter = new DayAdapter(this, daysOfWeek);
        dayAdapter.setOnDayClickListener((position, day) -> {
            selectedDate = (Calendar) day.clone();
            loadMealPlanForSelectedDate();
        });

        androidx.recyclerview.widget.LinearLayoutManager layoutManager = new androidx.recyclerview.widget.LinearLayoutManager(
                this,
                androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL, false);
        rvDays.setLayoutManager(layoutManager);
        rvDays.setAdapter(dayAdapter);
    }

    private void loadMealPlanForSelectedDate() {
        mealCategories = mealPlanManager.loadMealPlan(selectedDate);
        setupMealCategories();
    }

    private void setupMealCategories() {
        adapter = new MealCategoryAdapter(this, mealCategories);
        
        // 设置添加食谱监听器
        adapter.setOnAddRecipeClickListener(mealCategoryName -> {
            showSelectRecipeDialog(mealCategoryName);
        });
        
        // 设置删除食谱监听器
        adapter.setOnRemoveRecipeClickListener((mealCategoryName, recipeIndex) -> {
            mealPlanManager.removeRecipeFromMeal(selectedDate, mealCategoryName, recipeIndex);
            loadMealPlanForSelectedDate();
            Toast.makeText(this, "Đã xóa món ăn", Toast.LENGTH_SHORT).show();
        });
        
        rvMealCategories.setLayoutManager(new LinearLayoutManager(this));
        rvMealCategories.setAdapter(adapter);
    }

    private void showSelectRecipeDialog(String mealCategoryName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_select_recipe, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        dialog.show();

        EditText edtSearch = dialogView.findViewById(R.id.edtSearchRecipe);
        RecyclerView rvRecipes = dialogView.findViewById(R.id.rvRecipes);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);
        Button btnConfirm = dialogView.findViewById(R.id.btnConfirm);

        List<Recipe> allRecipes = new ArrayList<>();
        List<String> recipeIds = new ArrayList<>();
        List<Recipe> filteredRecipes = new ArrayList<>();
        List<String> filteredIds = new ArrayList<>();
        final String[] selectedRecipeId = {null};
        final Recipe[] selectedRecipe = {null};

        RecipeGridAdapter recipeAdapter = new RecipeGridAdapter(this, filteredRecipes, filteredIds);
        recipeAdapter.setOnRecipeClickListener((recipe, recipeId) -> {
            selectedRecipeId[0] = recipeId;
            selectedRecipe[0] = recipe;
            Toast.makeText(MealPlanActivity.this, "Đã chọn: " + recipe.title, Toast.LENGTH_SHORT).show();
        });

        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        rvRecipes.setLayoutManager(layoutManager);
        rvRecipes.setAdapter(recipeAdapter);

        // 加载所有食谱
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference recipesRef = database.getReference("recipes");
        
        recipesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(com.google.firebase.database.DataSnapshot snapshot) {
                allRecipes.clear();
                recipeIds.clear();
                
                for (com.google.firebase.database.DataSnapshot data : snapshot.getChildren()) {
                    Recipe r = data.getValue(Recipe.class);
                    if (r != null) {
                        allRecipes.add(r);
                        recipeIds.add(data.getKey());
                    }
                }
                
                filteredRecipes.clear();
                filteredIds.clear();
                filteredRecipes.addAll(allRecipes);
                filteredIds.addAll(recipeIds);
                recipeAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(MealPlanActivity.this, "Lỗi tải dữ liệu: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // 搜索功能
        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().toLowerCase();
                filteredRecipes.clear();
                filteredIds.clear();
                
                if (query.isEmpty()) {
                    filteredRecipes.addAll(allRecipes);
                    filteredIds.addAll(recipeIds);
                } else {
                    for (int i = 0; i < allRecipes.size(); i++) {
                        Recipe r = allRecipes.get(i);
                        if (r.title != null && r.title.toLowerCase().contains(query)) {
                            filteredRecipes.add(r);
                            filteredIds.add(recipeIds.get(i));
                        }
                    }
                }
                recipeAdapter.notifyDataSetChanged();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnConfirm.setOnClickListener(v -> {
            if (selectedRecipeId[0] != null && selectedRecipe[0] != null) {
                mealPlanManager.addRecipeToMeal(selectedDate, mealCategoryName, selectedRecipe[0]);
                loadMealPlanForSelectedDate();
                Toast.makeText(this, "Đã thêm món ăn vào " + mealCategoryName, Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            } else {
                Toast.makeText(this, "Vui lòng chọn một món ăn", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupBottomNav() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_plan);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                finish();
                return true;
            } else if (id == R.id.nav_recipes) {
                finish();
                // TODO: Navigate to RecipesListActivity
                return true;
            } else if (id == R.id.nav_plan) {
                return true;
            } else if (id == R.id.nav_favorite) {
                Intent intent = new Intent(MealPlanActivity.this, FavoritesActivity.class);
                startActivity(intent);
                return true;
            } else if (id == R.id.nav_profile) {
                Toast.makeText(this, "Cá nhân", Toast.LENGTH_SHORT).show();
                return true;
            }
            return false;
        });
    }

    private void setupFAB() {
        fabShoppingList.setOnClickListener(v -> {
            // TODO: Navigate to shopping list activity
            Toast.makeText(this, "Danh sách mua sắm", Toast.LENGTH_SHORT).show();
        });
    }
}
