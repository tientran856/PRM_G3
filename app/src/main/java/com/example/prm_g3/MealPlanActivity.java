package com.example.prm_g3;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.prm_g3.adapters.DayAdapter;
import com.example.prm_g3.adapters.MealCategoryAdapter;
import com.example.prm_g3.models.MealCategory;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

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
    private MealCategoryAdapter adapter;
    private DayAdapter dayAdapter;
    private List<MealCategory> mealCategories;
    private List<Calendar> daysOfWeek;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meal_plan);

        currentDate = Calendar.getInstance();
        // monthYearFormat không cần dùng nữa vì format thủ công

        initViews();
        setupDateSelector();
        setupMealCategories();
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
            // TODO: Load meal plan for selected day
            Toast.makeText(this, "Đã chọn ngày " + day.get(Calendar.DAY_OF_MONTH), Toast.LENGTH_SHORT).show();
        });

        androidx.recyclerview.widget.LinearLayoutManager layoutManager = new androidx.recyclerview.widget.LinearLayoutManager(
                this,
                androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL, false);
        rvDays.setLayoutManager(layoutManager);
        rvDays.setAdapter(dayAdapter);
    }

    private void setupMealCategories() {
        mealCategories = new ArrayList<>();
        mealCategories.add(new MealCategory("Bữa sáng", new ArrayList<>()));
        mealCategories.add(new MealCategory("Bữa trưa", new ArrayList<>()));
        mealCategories.add(new MealCategory("Bữa tối", new ArrayList<>()));
        mealCategories.add(new MealCategory("Bữa phụ", new ArrayList<>()));

        adapter = new MealCategoryAdapter(this, mealCategories);
        rvMealCategories.setLayoutManager(new LinearLayoutManager(this));
        rvMealCategories.setAdapter(adapter);
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
