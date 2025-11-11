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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.bumptech.glide.Glide;
import android.util.Log;
import java.io.InputStream;

import java.text.SimpleDateFormat;
import java.util.*;

public class CreateRecipeActivity extends AppCompatActivity {

    private ImageView imgRecipe;
    private RelativeLayout containerImageUpload;
    private EditText edtTitle, edtDescription, edtTime, edtServings;
    private EditText edtImageUrl;
    private Spinner spinnerDifficulty, spinnerCategory;
    private Button btnAddIngredient, btnAddStep, btnSave, btnUseUrl;
    private LinearLayout containerIngredients, containerSteps;
    private List<View> ingredientViews;
    private List<View> stepViews;
    private Uri imageUri;
    private String imageUrlFromInput = null;
    private boolean useUrlMode = false;

    // Edit mode variables
    private boolean isEditMode = false;
    private String editRecipeId = null;

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
        setupCategorySpinner();

        // Check if in edit mode
        Intent intent = getIntent();
        if (intent != null && intent.getBooleanExtra("editMode", false)) {
            isEditMode = true;
            editRecipeId = intent.getStringExtra("recipeId");
            if (editRecipeId != null && !editRecipeId.isEmpty()) {
                loadRecipeForEdit(editRecipeId);
            } else {
                Toast.makeText(this, "Không tìm thấy ID công thức để chỉnh sửa", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
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
        edtImageUrl = findViewById(R.id.edtImageUrl);
        spinnerDifficulty = findViewById(R.id.spinnerDifficulty);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        btnAddIngredient = findViewById(R.id.btnAddIngredient);
        btnAddStep = findViewById(R.id.btnAddStep);
        btnUseUrl = findViewById(R.id.btnUseUrl);
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
            if (!useUrlMode) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, PICK_IMAGE_REQUEST);
            }
        });

        btnUseUrl.setOnClickListener(v -> toggleImageInputMode());

        // Listen to URL input changes
        edtImageUrl.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String url = s.toString().trim();
                if (!url.isEmpty() && android.util.Patterns.WEB_URL.matcher(url).matches()) {
                    // Load image from URL to preview
                    loadImageFromUrl(url);
                    imageUrlFromInput = url;
                } else {
                    imageUrlFromInput = null;
                }
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {
            }
        });

        btnAddIngredient.setOnClickListener(v -> addIngredientRow());
        btnAddStep.setOnClickListener(v -> addStepRow());
        btnSave.setOnClickListener(v -> saveRecipe());
    }

    private void toggleImageInputMode() {
        useUrlMode = !useUrlMode;
        if (useUrlMode) {
            // Switch to URL mode
            btnUseUrl.setText("Chọn ảnh");
            edtImageUrl.setVisibility(View.VISIBLE);
            containerImageUpload.setClickable(false);
            containerImageUpload.setAlpha(0.5f);
            imageUri = null; // Clear local image
            imgRecipe.setVisibility(View.GONE);
        } else {
            // Switch to gallery mode
            btnUseUrl.setText("Dùng URL");
            edtImageUrl.setVisibility(View.GONE);
            edtImageUrl.setText("");
            containerImageUpload.setClickable(true);
            containerImageUpload.setAlpha(1.0f);
            imageUrlFromInput = null; // Clear URL
            imgRecipe.setVisibility(View.GONE);
        }
    }

    private void loadImageFromUrl(String url) {
        try {
            com.bumptech.glide.Glide.with(this)
                    .load(url)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_menu_report_image)
                    .into(imgRecipe);
            imgRecipe.setVisibility(View.VISIBLE);
        } catch (Exception e) {
            Log.e("CreateRecipeActivity", "Error loading image from URL: " + e.getMessage(), e);
            Toast.makeText(this, "Không thể tải ảnh từ URL này", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupDifficultySpinner() {
        String[] diffs = { "Dễ", "Trung bình", "Khó" };
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, diffs);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDifficulty.setAdapter(adapter);
    }

    private void setupCategorySpinner() {
        String[] categories = {
                "Món Á",
                "Món chính",
                "Món phụ",
                "Món khai vị",
                "Món tráng miệng",
                "Món chay",
                "Món nhanh",
                "Truyền thống",
                "Món Âu",
                "Món Nhật",
                "Món Hàn",
                "Món Trung",
                "Món Thái",
                "Món Việt",
                "Đồ uống",
                "Bánh ngọt",
                "Khác"
        };
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);
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

        // Check if using URL or local image
        if (useUrlMode) {
            String url = edtImageUrl.getText().toString().trim();
            if (url.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập URL ảnh hoặc chọn ảnh từ thư viện", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!android.util.Patterns.WEB_URL.matcher(url).matches()) {
                Toast.makeText(this,
                        "URL không hợp lệ. Vui lòng nhập URL đúng định dạng (VD: https://example.com/image.jpg)",
                        Toast.LENGTH_LONG).show();
                return;
            }
            // Use URL directly, no upload needed
            btnSave.setEnabled(false);
            btnSave.setText(isEditMode ? "Đang cập nhật..." : "Đang lưu...");
            uploadRecipeWithImage(url);
            // Note: Button will be re-enabled in success/failure callbacks
            return;
        }

        // Using local image - need to upload
        if (imageUri == null) {
            Toast.makeText(this, "Vui lòng chọn hình ảnh hoặc nhập URL ảnh", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show loading indicator
        btnSave.setEnabled(false);
        btnSave.setText(isEditMode ? "Đang cập nhật..." : "Đang tải lên...");

        uploadImageToFirebase(imageUri,
                downloadUrl -> {
                    uploadRecipeWithImage(downloadUrl);
                    btnSave.setEnabled(true);
                    btnSave.setText(isEditMode ? "Cập nhật" : "Lưu");
                },
                error -> {
                    String errorMessage = error.getMessage();
                    if (errorMessage == null) {
                        errorMessage = "Lỗi không xác định";
                    }
                    // Offer to use URL instead
                    new android.app.AlertDialog.Builder(this)
                            .setTitle("Upload ảnh thất bại")
                            .setMessage("Không thể upload ảnh lên Firebase Storage.\n\n" +
                                    "Bạn có muốn:\n" +
                                    "1. Thử lại upload\n" +
                                    "2. Sử dụng URL ảnh từ internet thay thế?")
                            .setPositiveButton("Dùng URL", (dialog, which) -> {
                                toggleImageInputMode();
                                btnSave.setEnabled(true);
                                btnSave.setText(isEditMode ? "Cập nhật" : "Lưu");
                            })
                            .setNeutralButton("Thử lại", (dialog, which) -> {
                                saveRecipe(); // Retry
                            })
                            .setNegativeButton("Hủy", (dialog, which) -> {
                                btnSave.setEnabled(true);
                                btnSave.setText(isEditMode ? "Cập nhật" : "Lưu");
                            })
                            .show();
                });
    }

    private void uploadImageToFirebase(Uri uri, OnSuccessListener<String> onSuccess, OnFailureListener onFailure) {
        try {
            // Validate URI
            if (uri == null) {
                onFailure.onFailure(new Exception("URI không hợp lệ"));
                return;
            }

            // Create unique file name
            String fileName = "recipe_images/" + System.currentTimeMillis() + "_" + UUID.randomUUID() + ".jpg";

            // Get Firebase Storage reference
            // Use bucket name from google-services.json: "prm-g3.firebasestorage.app"
            FirebaseStorage storage;
            try {
                // Try to get instance with specific bucket name
                String bucketName = "prm-g3.firebasestorage.app";
                storage = FirebaseStorage.getInstance("gs://" + bucketName);
                Log.d("CreateRecipeActivity", "Using bucket: " + bucketName);
            } catch (Exception e) {
                // Fallback to default instance
                Log.w("CreateRecipeActivity", "Failed to use specific bucket, using default: " + e.getMessage());
                try {
                    storage = FirebaseStorage.getInstance();
                } catch (Exception e2) {
                    Log.e("CreateRecipeActivity", "Firebase Storage not initialized: " + e2.getMessage(), e2);
                    onFailure.onFailure(new Exception(
                            "Firebase Storage chưa được kích hoạt. Vui lòng vào Firebase Console > Storage để kích hoạt."));
                    return;
                }
            }

            StorageReference storageRef = storage.getReference();
            StorageReference imageRef = storageRef.child(fileName);

            Log.d("CreateRecipeActivity",
                    "Storage bucket: " + (storage.getApp() != null ? storage.getApp().getName() : "default"));
            Log.d("CreateRecipeActivity", "Storage reference path: " + imageRef.getPath());
            Log.d("CreateRecipeActivity", "Full path: gs://" + imageRef.getBucket() + "/" + imageRef.getPath());

            Log.d("CreateRecipeActivity", "Uploading image to: " + fileName);
            Log.d("CreateRecipeActivity", "URI: " + uri.toString());
            Log.d("CreateRecipeActivity", "URI Scheme: " + uri.getScheme());

            // Upload file
            UploadTask uploadTask = imageRef.putFile(uri);

            uploadTask.addOnSuccessListener(taskSnapshot -> {
                Log.d("CreateRecipeActivity", "Upload successful");
                // Get download URL
                imageRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                    String downloadUrl = downloadUri.toString();
                    Log.d("CreateRecipeActivity", "Download URL: " + downloadUrl);
                    onSuccess.onSuccess(downloadUrl);
                }).addOnFailureListener(e -> {
                    Log.e("CreateRecipeActivity", "Error getting download URL: " + e.getMessage(), e);
                    onFailure.onFailure(e);
                });
            }).addOnFailureListener(e -> {
                Log.e("CreateRecipeActivity", "Upload failed: " + e.getMessage(), e);
                String errorMessage = e.getMessage();
                if (errorMessage != null) {
                    if (errorMessage.contains("Object does not exist") || errorMessage.contains("404")
                            || errorMessage.contains("Not Found")) {
                        errorMessage = "Firebase Storage chưa được kích hoạt.\n\n" +
                                "Cách khắc phục:\n" +
                                "1. Vào Firebase Console (https://console.firebase.google.com)\n" +
                                "2. Chọn project của bạn\n" +
                                "3. Vào mục Storage\n" +
                                "4. Nhấn 'Get started' để kích hoạt Storage\n" +
                                "5. Chọn 'Start in test mode' hoặc cấu hình rules\n" +
                                "6. Chọn location cho bucket\n" +
                                "7. Thử lại upload ảnh";
                    } else if (errorMessage.contains("Permission denied") || errorMessage.contains("403")) {
                        errorMessage = "Không có quyền upload ảnh.\n\n" +
                                "Cách khắc phục:\n" +
                                "1. Vào Firebase Console > Storage > Rules\n" +
                                "2. Đảm bảo rules cho phép upload:\n" +
                                "   allow write: if request.auth != null;\n" +
                                "3. Lưu rules và thử lại";
                    }
                } else {
                    errorMessage = "Lỗi không xác định khi upload ảnh";
                }
                onFailure.onFailure(new Exception(errorMessage));
            }).addOnProgressListener(taskSnapshot -> {
                // Show upload progress if needed
                double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                Log.d("CreateRecipeActivity", "Upload progress: " + progress + "%");
            });
        } catch (Exception e) {
            Log.e("CreateRecipeActivity", "Exception in uploadImageToFirebase: " + e.getMessage(), e);
            onFailure.onFailure(e);
        }
    }

    private void uploadRecipeWithImage(String imageUrl) {
        DatabaseReference recipesRef = FirebaseDatabase.getInstance().getReference("recipes");
        String recipeId;

        if (isEditMode && editRecipeId != null) {
            // Update existing recipe
            recipeId = editRecipeId;
        } else {
            // Create new recipe
            recipeId = recipesRef.push().getKey();
        }

        Recipe recipe = new Recipe();
        recipe.title = edtTitle.getText().toString().trim();
        recipe.description = edtDescription.getText().toString().trim();
        recipe.image_url = imageUrl;
        recipe.difficulty = spinnerDifficulty.getSelectedItem().toString();
        recipe.category = spinnerCategory.getSelectedItem().toString();

        if (isEditMode) {
            // Keep original author_id and created_at when editing
            DatabaseReference recipeRef = recipesRef.child(recipeId);
            recipeRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        Recipe existingRecipe = snapshot.getValue(Recipe.class);
                        if (existingRecipe != null) {
                            recipe.author_id = existingRecipe.author_id;
                            recipe.created_at = existingRecipe.created_at;
                        } else {
                            recipe.author_id = UserManager.getInstance().getCurrentUserId();
                            recipe.created_at = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
                                    .format(new Date());
                        }
                    } else {
                        recipe.author_id = UserManager.getInstance().getCurrentUserId();
                        recipe.created_at = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
                                .format(new Date());
                    }
                    recipe.updated_at = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
                            .format(new Date());

                    // Parse time and servings from input
                    try {
                        String timeStr = edtTime.getText().toString().trim();
                        if (!timeStr.isEmpty()) {
                            int totalTime = Integer.parseInt(timeStr);
                            recipe.prep_time = totalTime;
                            recipe.cook_time = 0;
                        } else {
                            recipe.prep_time = 30;
                            recipe.cook_time = 0;
                        }
                    } catch (NumberFormatException e) {
                        recipe.prep_time = 30;
                        recipe.cook_time = 0;
                    }

                    try {
                        String servingsStr = edtServings.getText().toString().trim();
                        if (!servingsStr.isEmpty()) {
                            recipe.servings = Integer.parseInt(servingsStr);
                        } else {
                            recipe.servings = 2;
                        }
                    } catch (NumberFormatException e) {
                        recipe.servings = 2;
                    }

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
                                Toast.makeText(CreateRecipeActivity.this,
                                        isEditMode ? "Đã cập nhật công thức thành công" : "Đã lưu công thức thành công",
                                        Toast.LENGTH_SHORT).show();
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                btnSave.setEnabled(true);
                                btnSave.setText(isEditMode ? "Cập nhật" : "Lưu");
                                Toast.makeText(CreateRecipeActivity.this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT)
                                        .show();
                            });
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    btnSave.setEnabled(true);
                    btnSave.setText(isEditMode ? "Cập nhật" : "Lưu");
                    Toast.makeText(CreateRecipeActivity.this, "Lỗi tải dữ liệu: " + error.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // Create new recipe
            recipe.author_id = UserManager.getInstance().getCurrentUserId();
            recipe.created_at = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
                    .format(new Date());
            recipe.updated_at = recipe.created_at;

            // Parse time and servings from input
            try {
                String timeStr = edtTime.getText().toString().trim();
                if (!timeStr.isEmpty()) {
                    int totalTime = Integer.parseInt(timeStr);
                    recipe.prep_time = totalTime;
                    recipe.cook_time = 0;
                } else {
                    recipe.prep_time = 30;
                    recipe.cook_time = 0;
                }
            } catch (NumberFormatException e) {
                recipe.prep_time = 30;
                recipe.cook_time = 0;
            }

            try {
                String servingsStr = edtServings.getText().toString().trim();
                if (!servingsStr.isEmpty()) {
                    recipe.servings = Integer.parseInt(servingsStr);
                } else {
                    recipe.servings = 2;
                }
            } catch (NumberFormatException e) {
                recipe.servings = 2;
            }

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
                    .addOnFailureListener(e -> {
                        btnSave.setEnabled(true);
                        btnSave.setText("Lưu");
                        Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void loadRecipeForEdit(String recipeId) {
        DatabaseReference recipeRef = FirebaseDatabase.getInstance().getReference("recipes").child(recipeId);
        recipeRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    Toast.makeText(CreateRecipeActivity.this, "Không tìm thấy công thức", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }

                Recipe recipe = snapshot.getValue(Recipe.class);
                if (recipe == null) {
                    Toast.makeText(CreateRecipeActivity.this, "Lỗi đọc dữ liệu công thức", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }

                // Populate title and description
                edtTitle.setText(recipe.title);
                edtDescription.setText(recipe.description);

                // Populate time and servings
                int totalTime = recipe.prep_time + recipe.cook_time;
                edtTime.setText(String.valueOf(totalTime));
                edtServings.setText(String.valueOf(recipe.servings));

                // Set difficulty spinner
                String[] difficulties = { "Dễ", "Trung bình", "Khó" };
                for (int i = 0; i < difficulties.length; i++) {
                    if (difficulties[i].equals(recipe.difficulty)) {
                        spinnerDifficulty.setSelection(i);
                        break;
                    }
                }

                // Set category spinner
                String[] categories = {
                        "Món Á", "Món chính", "Món phụ", "Món khai vị", "Món tráng miệng",
                        "Món chay", "Món nhanh", "Truyền thống", "Món Âu", "Món Nhật",
                        "Món Hàn", "Món Trung", "Món Thái", "Món Việt", "Đồ uống",
                        "Bánh ngọt", "Khác"
                };
                for (int i = 0; i < categories.length; i++) {
                    if (categories[i].equals(recipe.category)) {
                        spinnerCategory.setSelection(i);
                        break;
                    }
                }

                // Load image
                if (recipe.image_url != null && !recipe.image_url.isEmpty()) {
                    useUrlMode = true;
                    btnUseUrl.setText("Chọn ảnh");
                    edtImageUrl.setVisibility(View.VISIBLE);
                    edtImageUrl.setText(recipe.image_url);
                    containerImageUpload.setClickable(false);
                    containerImageUpload.setAlpha(0.5f);
                    imageUrlFromInput = recipe.image_url;
                    Glide.with(CreateRecipeActivity.this)
                            .load(recipe.image_url)
                            .placeholder(android.R.drawable.ic_menu_gallery)
                            .error(android.R.drawable.ic_menu_report_image)
                            .into(imgRecipe);
                    imgRecipe.setVisibility(View.VISIBLE);
                }

                // Load ingredients
                containerIngredients.removeAllViews();
                ingredientViews.clear();
                DataSnapshot ingredientsSnap = snapshot.child("ingredients");
                for (DataSnapshot item : ingredientsSnap.getChildren()) {
                    String name = item.child("name").getValue(String.class);
                    String quantity = item.child("quantity").getValue(String.class);
                    if (name != null && quantity != null) {
                        View row = getLayoutInflater().inflate(R.layout.item_ingredient_input, containerIngredients,
                                false);
                        EditText edtName = row.findViewById(R.id.edtIngredientName);
                        EditText edtQty = row.findViewById(R.id.edtIngredientQuantity);
                        edtName.setText(name);
                        edtQty.setText(quantity);

                        ImageButton btnRemove = row.findViewById(R.id.btnRemoveIngredient);
                        btnRemove.setOnClickListener(v -> {
                            containerIngredients.removeView(row);
                            ingredientViews.remove(row);
                        });

                        containerIngredients.addView(row);
                        ingredientViews.add(row);
                    }
                }

                // Load steps
                containerSteps.removeAllViews();
                stepViews.clear();
                DataSnapshot stepsSnap = snapshot.child("steps");
                List<Step> stepsList = new ArrayList<>();
                for (DataSnapshot stepSnap : stepsSnap.getChildren()) {
                    Step step = stepSnap.getValue(Step.class);
                    if (step != null) {
                        stepsList.add(step);
                    }
                }

                // Sort steps by step_number
                stepsList.sort((s1, s2) -> Integer.compare(s1.step_number, s2.step_number));

                for (Step step : stepsList) {
                    View row = getLayoutInflater().inflate(R.layout.item_step_input, containerSteps, false);
                    TextView tvStepNumber = row.findViewById(R.id.tvStepNumber);
                    EditText edtStepDescription = row.findViewById(R.id.edtStepDescription);

                    tvStepNumber.setText(String.valueOf(step.step_number));
                    edtStepDescription.setText(step.instruction);
                    edtStepDescription.setHint("Bước " + step.step_number);

                    ImageButton btnRemove = row.findViewById(R.id.btnRemoveStep);
                    btnRemove.setOnClickListener(v -> {
                        containerSteps.removeView(row);
                        stepViews.remove(row);
                        updateStepNumbers();
                    });

                    containerSteps.addView(row);
                    stepViews.add(row);
                }

                // Update button text
                btnSave.setText("Cập nhật");
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(CreateRecipeActivity.this, "Lỗi tải dữ liệu: " + error.getMessage(), Toast.LENGTH_SHORT)
                        .show();
                finish();
            }
        });
    }
}
