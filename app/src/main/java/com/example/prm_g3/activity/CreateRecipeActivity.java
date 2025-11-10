package com.example.prm_g3.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.prm_g3.R;
import com.example.prm_g3.models.Recipe;
import com.example.prm_g3.models.Ingredient;
import com.example.prm_g3.models.Step;
import com.example.prm_g3.UserManager;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CreateRecipeActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private Button btnSave;
    private ImageView imgRecipe;
    private RelativeLayout containerImageUpload;
    private EditText edtTitle, edtDescription, edtTime, edtServings;
    private Spinner spinnerDifficulty;
    private Button btnAddIngredient, btnAddStep;
    private LinearLayout containerIngredients, containerSteps;

    private List<View> ingredientViews;
    private List<View> stepViews;
    private Uri imageUri;
    private static final int PICK_IMAGE_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_create_recipe);
            initViews();
            setupClickListeners();
            setupDifficultySpinner();
        } catch (Exception e) {
            android.util.Log.e("CreateRecipeActivity", "Error in onCreate: " + e.getMessage(), e);
            Toast.makeText(this, "Lỗi khởi tạo: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnSave = findViewById(R.id.btnSave);
        imgRecipe = findViewById(R.id.imgRecipe);
        containerImageUpload = findViewById(R.id.containerImageUpload);
        edtTitle = findViewById(R.id.edtTitle);
        edtDescription = findViewById(R.id.edtDescription);
        edtTime = findViewById(R.id.edtTime);
        edtServings = findViewById(R.id.edtServings);
        spinnerDifficulty = findViewById(R.id.spinnerDifficulty);
        btnAddIngredient = findViewById(R.id.btnAddIngredient);
        btnAddStep = findViewById(R.id.btnAddStep);
        containerIngredients = findViewById(R.id.containerIngredients);
        containerSteps = findViewById(R.id.containerSteps);

        ingredientViews = new ArrayList<>();
        stepViews = new ArrayList<>();
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());

        containerImageUpload.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
        });

        btnAddIngredient.setOnClickListener(v -> addIngredientRow());

        btnAddStep.setOnClickListener(v -> addStepRow());

        btnSave.setOnClickListener(v -> saveRecipe());
    }

    private void setupDifficultySpinner() {
        String[] difficulties = { "Dễ", "Trung bình", "Khó" };
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, difficulties);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDifficulty.setAdapter(adapter);
    }

    private void addIngredientRow() {
        View rowView = getLayoutInflater().inflate(R.layout.item_ingredient_input, containerIngredients, false);

        EditText edtIngredientName = rowView.findViewById(R.id.edtIngredientName);
        EditText edtIngredientQuantity = rowView.findViewById(R.id.edtIngredientQuantity);
        ImageButton btnRemove = rowView.findViewById(R.id.btnRemoveIngredient);

        btnRemove.setOnClickListener(v -> {
            containerIngredients.removeView(rowView);
            ingredientViews.remove(rowView);
        });

        containerIngredients.addView(rowView);
        ingredientViews.add(rowView);
    }

    private void addStepRow() {
        View rowView = getLayoutInflater().inflate(R.layout.item_step_input, containerSteps, false);

        TextView tvStepNumber = rowView.findViewById(R.id.tvStepNumber);
        EditText edtStepDescription = rowView.findViewById(R.id.edtStepDescription);
        ImageButton btnRemove = rowView.findViewById(R.id.btnRemoveStep);

        int stepNumber = stepViews.size() + 1;
        tvStepNumber.setText(String.valueOf(stepNumber));

        btnRemove.setOnClickListener(v -> {
            containerSteps.removeView(rowView);
            stepViews.remove(rowView);
            updateStepNumbers();
        });

        containerSteps.addView(rowView);
        stepViews.add(rowView);
    }

    private void updateStepNumbers() {
        for (int i = 0; i < stepViews.size(); i++) {
            View stepView = stepViews.get(i);
            TextView tvStepNumber = stepView.findViewById(R.id.tvStepNumber);
            tvStepNumber.setText(String.valueOf(i + 1));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            imgRecipe.setImageURI(imageUri);
            imgRecipe.setVisibility(View.VISIBLE);
        }
    }

    private void saveRecipe() {
        // Validate inputs
        if (edtTitle.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập tên món ăn", Toast.LENGTH_SHORT).show();
            return;
        }

        if (imageUri == null) {
            Toast.makeText(this, "Vui lòng chọn hình ảnh", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get ingredients
        List<String> ingredients = new ArrayList<>();
        for (View view : ingredientViews) {
            EditText edtName = view.findViewById(R.id.edtIngredientName);
            EditText edtQuantity = view.findViewById(R.id.edtIngredientQuantity);
            String name = edtName.getText().toString().trim();
            String quantity = edtQuantity.getText().toString().trim();
            if (!name.isEmpty() && !quantity.isEmpty()) {
                ingredients.add(name + ": " + quantity);
            }
        }

        if (ingredients.isEmpty()) {
            Toast.makeText(this, "Vui lòng thêm ít nhất một nguyên liệu", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get steps
        List<String> steps = new ArrayList<>();
        for (View view : stepViews) {
            EditText edtStep = view.findViewById(R.id.edtStepDescription);
            String step = edtStep.getText().toString().trim();
            if (!step.isEmpty()) {
                steps.add(step);
            }
        }

        if (steps.isEmpty()) {
            Toast.makeText(this, "Vui lòng thêm ít nhất một bước thực hiện", Toast.LENGTH_SHORT).show();
            return;
        }

        // Parse time
        String timeStr = edtTime.getText().toString().trim();
        int prepTime = 0;
        int cookTime = 0;
        if (timeStr.contains("phút")) {
            try {
                prepTime = Integer.parseInt(timeStr.replace(" phút", "").trim());
            } catch (NumberFormatException e) {
                prepTime = 30;
            }
        }

        // Parse servings
        int servings = 1;
        try {
            String servingsStr = edtServings.getText().toString().trim();
            if (!servingsStr.isEmpty()) {
                servings = Integer.parseInt(servingsStr);
            }
        } catch (NumberFormatException e) {
            servings = 1;
        }

        // Generate recipe ID - use Firebase push key format
        DatabaseReference recipesRef = FirebaseDatabase.getInstance().getReference("recipes");
        String recipeId = recipesRef.push().getKey();
        String currentTime = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).format(new Date());
        String authorId = UserManager.getInstance().getCurrentUserId();

        // Create Recipe object with nested ingredients and steps
        Recipe recipe = new Recipe();
        recipe.title = edtTitle.getText().toString().trim();
        recipe.description = edtDescription.getText().toString().trim();
        recipe.category = ""; // Can be added later with a category selector
        recipe.tags = ""; // Can be added later with tags input
        recipe.prep_time = prepTime;
        recipe.cook_time = cookTime;
        recipe.servings = servings;
        recipe.difficulty = spinnerDifficulty.getSelectedItem().toString();
        recipe.rating = 0.0;
        recipe.total_reviews = 0;
        recipe.image_url = imageUri != null ? imageUri.toString() : "";
        recipe.video_url = "";
        recipe.author_id = authorId != null ? authorId : "";
        recipe.created_at = currentTime;
        recipe.updated_at = currentTime;
        recipe.sync_status = 0;

        // Create nested ingredients map
        Map<String, Ingredient> ingredientsMap = new HashMap<>();
        int ingIndex = 1;
        for (View view : ingredientViews) {
            EditText edtName = view.findViewById(R.id.edtIngredientName);
            EditText edtQuantity = view.findViewById(R.id.edtIngredientQuantity);
            String name = edtName.getText().toString().trim();
            String quantity = edtQuantity.getText().toString().trim();
            if (!name.isEmpty() && !quantity.isEmpty()) {
                Ingredient ingredient = new Ingredient();
                ingredient.name = name;
                ingredient.quantity = quantity;
                ingredient.sync_status = 0;
                // Use key format: ing_001, ing_002, etc.
                String ingKey = "ing_" + String.format("%03d", ingIndex++);
                ingredientsMap.put(ingKey, ingredient);
            }
        }
        recipe.ingredients = ingredientsMap;

        // Create nested steps map
        Map<String, Step> stepsMap = new HashMap<>();
        int stepIndex = 1;
        for (int i = 0; i < stepViews.size(); i++) {
            View view = stepViews.get(i);
            EditText edtStep = view.findViewById(R.id.edtStepDescription);
            String instruction = edtStep.getText().toString().trim();
            if (!instruction.isEmpty()) {
                Step step = new Step();
                step.step_number = stepIndex;
                step.instruction = instruction;
                step.image_url = "";
                step.sync_status = 0;
                // Use key format: step_001, step_002, etc.
                String stepKey = "step_" + String.format("%03d", stepIndex++);
                stepsMap.put(stepKey, step);
            }
        }
        recipe.steps = stepsMap;

        // Save to Firebase with nested structure
        recipesRef.child(recipeId).setValue(recipe)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Đã lưu công thức thành công", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi lưu công thức: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    android.util.Log.e("CreateRecipeActivity", "Error saving recipe: " + e.getMessage(), e);
                });
    }

}
