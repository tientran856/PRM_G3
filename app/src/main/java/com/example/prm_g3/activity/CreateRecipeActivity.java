package com.example.prm_g3.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

import com.example.prm_g3.R;
import com.example.prm_g3.models.Recipe;
import com.example.prm_g3.models.Ingredient;
import com.example.prm_g3.models.Step;
import com.example.prm_g3.UserManager;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.*;

public class CreateRecipeActivity extends AppCompatActivity {

    private ImageView imgRecipe;
    private RelativeLayout containerImageUpload;
    private EditText edtTitle, edtDescription, edtTime, edtServings;
    private Spinner spinnerDifficulty;
    private Button btnAddIngredient, btnAddStep, btnSave;
    private LinearLayout containerIngredients, containerSteps;
    private List<View> ingredientViews;
    private List<View> stepViews;
    private Uri imageUri;

    private static final int PICK_IMAGE_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_recipe);

        // Configure status bar to show light icons (white) on dark background
        setupStatusBar();

        // Handle system window insets for status bar
        View rootView = findViewById(android.R.id.content);
        if (rootView != null) {
            rootView.setOnApplyWindowInsetsListener((v, insets) -> {
                int statusBarHeight = insets.getSystemWindowInsetTop();
                LinearLayout headerLayout = findViewById(R.id.headerLayout);
                if (headerLayout != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    // Adjust padding to account for status bar
                    headerLayout.setPadding(
                            headerLayout.getPaddingLeft(),
                            Math.max(statusBarHeight, (int) (24 * getResources().getDisplayMetrics().density)),
                            headerLayout.getPaddingRight(),
                            headerLayout.getPaddingBottom());
                }
                return insets;
            });
        }

        initViews();
        setupListeners();
        setupDifficultySpinner();
    }

    private void setupStatusBar() {
        // Configure status bar to show light icons (white) on dark background
        // Set status bar background to dark (to match header)
        getWindow().setStatusBarColor(Color.parseColor("#0D0D1A"));

        // Use WindowInsetsControllerCompat for modern approach (API 23+)
        WindowInsetsControllerCompat windowInsetsController = WindowCompat.getInsetsController(getWindow(),
                getWindow().getDecorView());

        if (windowInsetsController != null) {
            // Show light status bar icons (white icons on dark background)
            windowInsetsController.setAppearanceLightStatusBars(false);
        }
        // Fallback for older devices (API 23-29)
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            View decorView = getWindow().getDecorView();
            int flags = decorView.getSystemUiVisibility();
            // Disable light status bar (keep white icons on dark background)
            flags &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            decorView.setSystemUiVisibility(flags);
        }
    }

    private void initViews() {
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
        btnSave = findViewById(R.id.btnSave);

        // Setup back button
        ImageButton btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        ingredientViews = new ArrayList<>();
        stepViews = new ArrayList<>();
    }

    private void setupListeners() {
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
        String[] diffs = { "Dễ", "Trung bình", "Khó" };
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, diffs);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDifficulty.setAdapter(adapter);
    }

    private void addIngredientRow() {
        View row = getLayoutInflater().inflate(R.layout.item_ingredient_input, containerIngredients, false);
        ImageButton btnRemove = row.findViewById(R.id.btnRemoveIngredient);
        btnRemove.setOnClickListener(v -> {
            containerIngredients.removeView(row);
            ingredientViews.remove(row);
        });
        containerIngredients.addView(row);
        ingredientViews.add(row);
    }

    private void addStepRow() {
        View row = getLayoutInflater().inflate(R.layout.item_step_input, containerSteps, false);

        // Calculate step number (current count + 1)
        int stepNumber = stepViews.size() + 1;

        // Update step number TextView
        TextView tvStepNumber = row.findViewById(R.id.tvStepNumber);
        tvStepNumber.setText(String.valueOf(stepNumber));

        // Update step description hint
        EditText edtStepDescription = row.findViewById(R.id.edtStepDescription);
        edtStepDescription.setHint("Bước " + stepNumber);

        ImageButton btnRemove = row.findViewById(R.id.btnRemoveStep);
        btnRemove.setOnClickListener(v -> {
            containerSteps.removeView(row);
            stepViews.remove(row);
            // Update step numbers after removal
            updateStepNumbers();
        });

        containerSteps.addView(row);
        stepViews.add(row);
    }

    private void updateStepNumbers() {
        for (int i = 0; i < stepViews.size(); i++) {
            View stepView = stepViews.get(i);
            TextView tvStepNumber = stepView.findViewById(R.id.tvStepNumber);
            EditText edtStepDescription = stepView.findViewById(R.id.edtStepDescription);

            int stepNumber = i + 1;
            tvStepNumber.setText(String.valueOf(stepNumber));
            edtStepDescription.setHint("Bước " + stepNumber);
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

    // ✅ New save flow
    private void saveRecipe() {
        if (edtTitle.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập tên món ăn", Toast.LENGTH_SHORT).show();
            return;
        }
        if (imageUri == null) {
            Toast.makeText(this, "Vui lòng chọn hình ảnh", Toast.LENGTH_SHORT).show();
            return;
        }

        uploadImageToFirebase(imageUri, downloadUrl -> uploadRecipeWithImage(downloadUrl));
    }

    private void uploadImageToFirebase(Uri uri, OnSuccessListener<String> onSuccess) {
        String fileName = "recipe_images/" + System.currentTimeMillis() + "_" + UUID.randomUUID() + ".jpg";
        StorageReference ref = FirebaseStorage.getInstance().getReference(fileName);
        ref.putFile(uri)
                .addOnSuccessListener(task -> ref.getDownloadUrl().addOnSuccessListener(url -> {
                    onSuccess.onSuccess(url.toString());
                }))
                .addOnFailureListener(
                        e -> Toast.makeText(this, "Upload ảnh thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void uploadRecipeWithImage(String imageUrl) {
        DatabaseReference recipesRef = FirebaseDatabase.getInstance().getReference("recipes");
        String recipeId = recipesRef.push().getKey();

        Recipe recipe = new Recipe();
        recipe.title = edtTitle.getText().toString().trim();
        recipe.description = edtDescription.getText().toString().trim();
        recipe.image_url = imageUrl;
        recipe.difficulty = spinnerDifficulty.getSelectedItem().toString();
        recipe.author_id = UserManager.getInstance().getCurrentUserId();
        recipe.created_at = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).format(new Date());
        recipe.updated_at = recipe.created_at;
        recipe.prep_time = 30;
        recipe.cook_time = 0;
        recipe.servings = 2;

        Map<String, Ingredient> ingMap = new HashMap<>();
        int i = 1;
        for (View view : ingredientViews) {
            EditText n = view.findViewById(R.id.edtIngredientName);
            EditText q = view.findViewById(R.id.edtIngredientQuantity);
            if (!n.getText().toString().isEmpty() && !q.getText().toString().isEmpty()) {
                Ingredient ing = new Ingredient();
                ing.name = n.getText().toString();
                ing.quantity = q.getText().toString();
                ing.sync_status = 0;
                ingMap.put("ing_" + String.format("%03d", i++), ing);
            }
        }
        recipe.ingredients = ingMap;

        Map<String, Step> stepMap = new HashMap<>();
        int s = 1;
        for (View view : stepViews) {
            EditText d = view.findViewById(R.id.edtStepDescription);
            if (!d.getText().toString().isEmpty()) {
                Step step = new Step();
                step.step_number = s;
                step.instruction = d.getText().toString();
                step.image_url = "";
                stepMap.put("step_" + String.format("%03d", s++), step);
            }
        }
        recipe.steps = stepMap;

        recipesRef.child(recipeId).setValue(recipe)
                .addOnSuccessListener(a -> {
                    Toast.makeText(this, "Đã lưu công thức thành công", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
