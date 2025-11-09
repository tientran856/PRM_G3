package com.example.prm_g3;

import androidx.annotation.NonNull;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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

import com.example.prm_g3.models.Recipe;
import com.example.prm_g3.UserManager;
import com.example.prm_g3.RecipeLinkManager;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.List;

public class CreateRecipeActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private Button btnSave;
    private ImageView imgRecipe;
    private ImageView imgQRCode;
    private RelativeLayout containerImageUpload;
    private RelativeLayout containerQRCodeUpload;
    private EditText edtTitle, edtDescription, edtTime, edtServings, edtShareLink;
    private Spinner spinnerDifficulty;
    private Button btnAddIngredient, btnAddStep;
    private LinearLayout containerIngredients, containerSteps;

    private List<View> ingredientViews;
    private List<View> stepViews;
    private Uri imageUri;
    private Uri qrCodeUri;  // URI của mã QR code đã chọn
    
    // Activity Result Launchers (thay thế cho onActivityResult deprecated)
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private ActivityResultLauncher<Intent> qrCodePickerLauncher;
    
    // Mode: CREATE hoặc EDIT
    private String mode = "CREATE";
    private String editRecipeId = null;  // Recipe ID khi chỉnh sửa
    private Recipe existingRecipe = null;  // Recipe hiện tại khi chỉnh sửa

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_create_recipe);
            
            // Kiểm tra mode: CREATE hoặc EDIT
            editRecipeId = getIntent().getStringExtra("recipeId");
            if (editRecipeId != null && !editRecipeId.isEmpty()) {
                mode = "EDIT";
            }
            
            initViews();
            
            // Đảm bảo containers có thể nhận click events
            if (containerImageUpload != null) {
                containerImageUpload.setClickable(true);
                containerImageUpload.setFocusable(true);
                android.util.Log.d("CreateRecipeActivity", "containerImageUpload initialized: " + (containerImageUpload != null));
            } else {
                android.util.Log.e("CreateRecipeActivity", "containerImageUpload is NULL!");
            }
            
            if (containerQRCodeUpload != null) {
                containerQRCodeUpload.setClickable(true);
                containerQRCodeUpload.setFocusable(true);
                android.util.Log.d("CreateRecipeActivity", "containerQRCodeUpload initialized: " + (containerQRCodeUpload != null));
            } else {
                android.util.Log.e("CreateRecipeActivity", "containerQRCodeUpload is NULL!");
            }
            
            // Khởi tạo Activity Result Launchers
            setupActivityResultLaunchers();
            
            setupClickListeners();
            setupDifficultySpinner();
            
            if (mode.equals("EDIT")) {
                updateUIForEditMode();
                loadRecipeForEdit();
            }
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
        imgQRCode = findViewById(R.id.imgQRCode);
        containerImageUpload = findViewById(R.id.containerImageUpload);
        containerQRCodeUpload = findViewById(R.id.containerQRCodeUpload);
        edtTitle = findViewById(R.id.edtTitle);
        edtDescription = findViewById(R.id.edtDescription);
        edtTime = findViewById(R.id.edtTime);
        edtServings = findViewById(R.id.edtServings);
        edtShareLink = findViewById(R.id.edtShareLink);
        spinnerDifficulty = findViewById(R.id.spinnerDifficulty);
        btnAddIngredient = findViewById(R.id.btnAddIngredient);
        btnAddStep = findViewById(R.id.btnAddStep);
        containerIngredients = findViewById(R.id.containerIngredients);
        containerSteps = findViewById(R.id.containerSteps);

        ingredientViews = new ArrayList<>();
        stepViews = new ArrayList<>();
    }
    
    private void updateUIForEditMode() {
        TextView tvTitle = findViewById(R.id.tvTitle);
        if (tvTitle != null) {
            tvTitle.setText("Chỉnh sửa công thức");
        }
        Button btnSave = findViewById(R.id.btnSave);
        if (btnSave != null) {
            btnSave.setText("Cập nhật");
        }
    }

    private void setupActivityResultLaunchers() {
        // Launcher cho chọn hình ảnh món ăn
        imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri selectedUri = result.getData().getData();
                    if (selectedUri != null) {
                        android.util.Log.d("CreateRecipeActivity", "Image selected: " + selectedUri);
                        imageUri = selectedUri;
                        try {
                            // Ẩn placeholder
                            View placeholder = findViewById(R.id.placeholderImageUpload);
                            if (placeholder != null) {
                                placeholder.setVisibility(View.GONE);
                            }
                            // Hiển thị ảnh
                            imgRecipe.setImageURI(imageUri);
                            imgRecipe.setVisibility(View.VISIBLE);
                            Toast.makeText(this, "Đã chọn hình ảnh", Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            android.util.Log.e("CreateRecipeActivity", "Error setting image: " + e.getMessage(), e);
                            Toast.makeText(this, "Lỗi hiển thị hình ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    android.util.Log.d("CreateRecipeActivity", "Image picker cancelled");
                }
            }
        );
        
        // Launcher cho chọn mã QR code
        qrCodePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri selectedUri = result.getData().getData();
                    if (selectedUri != null) {
                        android.util.Log.d("CreateRecipeActivity", "QR Code selected: " + selectedUri);
                        qrCodeUri = selectedUri;
                        try {
                            // Ẩn placeholder
                            View placeholder = findViewById(R.id.placeholderQRCodeUpload);
                            if (placeholder != null) {
                                placeholder.setVisibility(View.GONE);
                            }
                            // Hiển thị mã QR
                            imgQRCode.setImageURI(qrCodeUri);
                            imgQRCode.setVisibility(View.VISIBLE);
                            Toast.makeText(this, "Đã chọn mã QR", Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            android.util.Log.e("CreateRecipeActivity", "Error setting QR code: " + e.getMessage(), e);
                            Toast.makeText(this, "Lỗi hiển thị mã QR: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    android.util.Log.d("CreateRecipeActivity", "QR Code picker cancelled");
                }
            }
        );
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());

        containerImageUpload.setOnClickListener(v -> {
            android.util.Log.d("CreateRecipeActivity", "Image upload container clicked");
            try {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                imagePickerLauncher.launch(intent);
            } catch (Exception e) {
                android.util.Log.e("CreateRecipeActivity", "Error opening image picker: " + e.getMessage(), e);
                Toast.makeText(this, "Lỗi mở chọn ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        containerQRCodeUpload.setOnClickListener(v -> {
            android.util.Log.d("CreateRecipeActivity", "QR code upload container clicked");
            try {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                qrCodePickerLauncher.launch(intent);
            } catch (Exception e) {
                android.util.Log.e("CreateRecipeActivity", "Error opening QR code picker: " + e.getMessage(), e);
                Toast.makeText(this, "Lỗi mở chọn mã QR: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        btnAddIngredient.setOnClickListener(v -> addIngredientRow());

        btnAddStep.setOnClickListener(v -> addStepRow());

        btnSave.setOnClickListener(v -> saveRecipe());
        
        // Tự động điền link khi người dùng nhập tên công thức (nếu EditText link đang trống)
        edtTitle.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                // Khi người dùng rời khỏi trường tên công thức
                String title = edtTitle.getText().toString().trim();
                String currentLink = edtShareLink.getText().toString().trim();
                
                // Chỉ tự động điền nếu EditText link đang trống và có link hardcode cho công thức này
                if ((currentLink == null || currentLink.isEmpty()) && title != null && !title.isEmpty()) {
                    String hardcodedLink = RecipeLinkManager.getShareLinkForRecipe(title);
                    if (hardcodedLink != null) {
                        edtShareLink.setText(hardcodedLink);
                        android.util.Log.d("CreateRecipeActivity", "Tự động điền link hardcode: " + title + " -> " + hardcodedLink);
                    }
                }
            }
        });
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

        // Get ingredients - Lưu đúng cấu trúc Firebase
        java.util.Map<String, Object> ingredientsMap = new java.util.HashMap<>();
        int ingredientIndex = 0;
        for (View view : ingredientViews) {
            EditText edtName = view.findViewById(R.id.edtIngredientName);
            EditText edtQuantity = view.findViewById(R.id.edtIngredientQuantity);
            String name = edtName.getText().toString().trim();
            String quantity = edtQuantity.getText().toString().trim();
            if (!name.isEmpty() && !quantity.isEmpty()) {
                java.util.Map<String, Object> ingredient = new java.util.HashMap<>();
                ingredient.put("name", name);
                ingredient.put("quantity", quantity);
                ingredientsMap.put(String.valueOf(ingredientIndex), ingredient);
                ingredientIndex++;
            }
        }

        if (ingredientsMap.isEmpty()) {
            Toast.makeText(this, "Vui lòng thêm ít nhất một nguyên liệu", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get steps - Lưu đúng cấu trúc Firebase
        java.util.Map<String, Object> stepsMap = new java.util.HashMap<>();
        int stepIndex = 0;
        for (View view : stepViews) {
            EditText edtStep = view.findViewById(R.id.edtStepDescription);
            String step = edtStep.getText().toString().trim();
            if (!step.isEmpty()) {
                java.util.Map<String, Object> stepObj = new java.util.HashMap<>();
                stepObj.put("step_number", stepIndex + 1);
                stepObj.put("instruction", step);
                stepsMap.put(String.valueOf(stepIndex), stepObj);
                stepIndex++;
            }
        }

        if (stepsMap.isEmpty()) {
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

        // Create recipe object
        Recipe recipe = new Recipe();
        recipe.title = edtTitle.getText().toString().trim();
        recipe.description = edtDescription.getText().toString().trim();
        recipe.prep_time = prepTime;
        recipe.cook_time = 0;
        recipe.difficulty = spinnerDifficulty.getSelectedItem().toString();
        if (mode.equals("EDIT") && existingRecipe != null) {
            recipe.rating = existingRecipe.rating;  // Giữ nguyên rating khi chỉnh sửa
            recipe.qr_code_url = existingRecipe.qr_code_url;  // Giữ nguyên QR code URL nếu không thay đổi
            recipe.author_id = existingRecipe.author_id;  // Giữ nguyên author_id
        } else {
            recipe.rating = 0.0;
            // Lưu author_id khi tạo mới
            recipe.author_id = UserManager.getInstance().getCurrentUserId();
        }
        recipe.image_url = imageUri.toString(); // In production, upload to Firebase Storage first

        // Save to Firebase
        DatabaseReference recipesRef = FirebaseDatabase.getInstance().getReference("recipes");
        String recipeId;
        
        if (mode.equals("EDIT") && editRecipeId != null) {
            recipeId = editRecipeId;
        } else {
            recipeId = recipesRef.push().getKey();
        }
        
        // Lấy share_link từ EditText
        String shareLink = edtShareLink.getText().toString().trim();
        
        // Nếu người dùng chưa nhập link, kiểm tra xem có link hardcode cho công thức này không
        if ((shareLink == null || shareLink.isEmpty()) && recipe.title != null) {
            String hardcodedLink = RecipeLinkManager.getShareLinkForRecipe(recipe.title);
            if (hardcodedLink != null) {
                shareLink = hardcodedLink;
                android.util.Log.d("CreateRecipeActivity", "Sử dụng link hardcode cho công thức: " + recipe.title + " -> " + shareLink);
            }
        }
        
        // Lưu ingredients và steps vào recipe
        java.util.Map<String, Object> recipeMap = new java.util.HashMap<>();
        recipeMap.put("title", recipe.title);
        recipeMap.put("description", recipe.description);
        recipeMap.put("prep_time", recipe.prep_time);
        recipeMap.put("cook_time", recipe.cook_time);
        recipeMap.put("difficulty", recipe.difficulty);
        recipeMap.put("rating", recipe.rating);
        recipeMap.put("image_url", recipe.image_url);
        recipeMap.put("ingredients", ingredientsMap);
        recipeMap.put("steps", stepsMap);
        if (recipe.author_id != null) {
            recipeMap.put("author_id", recipe.author_id);
        }
        if (recipe.qr_code_url != null) {
            recipeMap.put("qr_code_url", recipe.qr_code_url);
        }
        if (shareLink != null && !shareLink.isEmpty()) {
            recipeMap.put("share_link", shareLink);
        }
        
        // Upload QR code nếu có (và là QR code mới hoặc đang chỉnh sửa)
        if (qrCodeUri != null) {
            uploadQRCode(recipeId, recipeMap, recipesRef);
        } else {
            // Lưu công thức không có QR code mới
            recipesRef.child(recipeId).updateChildren(recipeMap)
                    .addOnSuccessListener(aVoid -> {
                        String message = mode.equals("EDIT") ? "Đã cập nhật công thức thành công" : "Đã lưu công thức thành công";
                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Lỗi lưu công thức: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }
    
    private void uploadQRCode(String recipeId, java.util.Map<String, Object> recipeMap, DatabaseReference recipesRef) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference qrCodeRef = storage.getReference().child("qr_codes").child(recipeId + ".jpg");
        
        UploadTask uploadTask = qrCodeRef.putFile(qrCodeUri);
        uploadTask.addOnSuccessListener(taskSnapshot -> {
            // Lấy download URL
            qrCodeRef.getDownloadUrl().addOnSuccessListener(uri -> {
                recipeMap.put("qr_code_url", uri.toString());
                // Lưu công thức với QR code URL
                recipesRef.child(recipeId).updateChildren(recipeMap)
                        .addOnSuccessListener(aVoid -> {
                            String message = mode.equals("EDIT") ? "Đã cập nhật công thức và mã QR thành công" : "Đã lưu công thức và mã QR thành công";
                            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                            setResult(RESULT_OK);
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "Lỗi lưu công thức: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            }).addOnFailureListener(e -> {
                Toast.makeText(this, "Lỗi lấy URL mã QR: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Lỗi upload mã QR: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            // Vẫn lưu công thức dù upload QR code thất bại
            recipesRef.child(recipeId).updateChildren(recipeMap)
                    .addOnSuccessListener(aVoid -> {
                        String message = mode.equals("EDIT") ? "Đã cập nhật công thức (không có mã QR)" : "Đã lưu công thức (không có mã QR)";
                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    });
        });
    }
    
    private void loadRecipeForEdit() {
        if (editRecipeId == null || editRecipeId.isEmpty()) {
            return;
        }
        
        DatabaseReference recipeRef = FirebaseDatabase.getInstance().getReference("recipes").child(editRecipeId);
        recipeRef.addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@NonNull com.google.firebase.database.DataSnapshot snapshot) {
                existingRecipe = snapshot.getValue(Recipe.class);
                if (existingRecipe == null) {
                    Toast.makeText(CreateRecipeActivity.this, "Không tìm thấy công thức để chỉnh sửa", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
                
                // Điền thông tin vào form
                edtTitle.setText(existingRecipe.title);
                edtDescription.setText(existingRecipe.description);
                edtTime.setText(existingRecipe.prep_time + " phút");
                
                // Load share_link: ưu tiên link đã lưu, sau đó là link hardcode
                if (existingRecipe.share_link != null && !existingRecipe.share_link.isEmpty()) {
                    edtShareLink.setText(existingRecipe.share_link);
                } else {
                    // Nếu chưa có link, kiểm tra link hardcode
                    String hardcodedLink = RecipeLinkManager.getShareLinkForRecipe(existingRecipe.title);
                    if (hardcodedLink != null) {
                        edtShareLink.setText(hardcodedLink);
                        android.util.Log.d("CreateRecipeActivity", "Điền link hardcode cho công thức: " + existingRecipe.title + " -> " + hardcodedLink);
                    }
                }
                
                // Set difficulty spinner
                String[] difficulties = { "Dễ", "Trung bình", "Khó" };
                for (int i = 0; i < difficulties.length; i++) {
                    if (difficulties[i].equals(existingRecipe.difficulty)) {
                        spinnerDifficulty.setSelection(i);
                        break;
                    }
                }
                
                // Load image
                if (existingRecipe.image_url != null && !existingRecipe.image_url.isEmpty()) {
                    try {
                        imageUri = android.net.Uri.parse(existingRecipe.image_url);
                        imgRecipe.setImageURI(imageUri);
                        imgRecipe.setVisibility(View.VISIBLE);
                        // Ẩn placeholder
                        View placeholder = findViewById(R.id.placeholderImageUpload);
                        if (placeholder != null) {
                            placeholder.setVisibility(View.GONE);
                        }
                    } catch (Exception e) {
                        android.util.Log.e("CreateRecipeActivity", "Error loading image: " + e.getMessage());
                    }
                }
                
                // Load QR code
                if (existingRecipe.qr_code_url != null && !existingRecipe.qr_code_url.isEmpty()) {
                    com.bumptech.glide.Glide.with(CreateRecipeActivity.this)
                            .load(existingRecipe.qr_code_url)
                            .into(imgQRCode);
                    imgQRCode.setVisibility(View.VISIBLE);
                    // Ẩn placeholder
                    View placeholder = findViewById(R.id.placeholderQRCodeUpload);
                    if (placeholder != null) {
                        placeholder.setVisibility(View.GONE);
                    }
                }
                
                // Load ingredients
                com.google.firebase.database.DataSnapshot ingredientsSnap = snapshot.child("ingredients");
                for (com.google.firebase.database.DataSnapshot ingredient : ingredientsSnap.getChildren()) {
                    String name = ingredient.child("name").getValue(String.class);
                    String quantity = ingredient.child("quantity").getValue(String.class);
                    if (name != null && quantity != null) {
                        addIngredientRow();
                        View lastView = ingredientViews.get(ingredientViews.size() - 1);
                        EditText edtName = lastView.findViewById(R.id.edtIngredientName);
                        EditText edtQuantity = lastView.findViewById(R.id.edtIngredientQuantity);
                        edtName.setText(name);
                        edtQuantity.setText(quantity);
                    }
                }
                
                // Load steps
                com.google.firebase.database.DataSnapshot stepsSnap = snapshot.child("steps");
                for (com.google.firebase.database.DataSnapshot step : stepsSnap.getChildren()) {
                    String instruction = step.child("instruction").getValue(String.class);
                    if (instruction != null) {
                        addStepRow();
                        View lastView = stepViews.get(stepViews.size() - 1);
                        EditText edtStep = lastView.findViewById(R.id.edtStepDescription);
                        edtStep.setText(instruction);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull com.google.firebase.database.DatabaseError error) {
                Toast.makeText(CreateRecipeActivity.this, "Lỗi tải công thức: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }
}
