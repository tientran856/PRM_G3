package com.example.prm_g3;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.prm_g3.models.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class EditProfileActivity extends AppCompatActivity {

    private ImageView btnBack;
    private EditText etName, etBio;
    private Button btnSave;

    private DatabaseReference usersRef;
    private String currentUserId;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        initViews();
        setupAuth();
        loadCurrentUserData();
        setupListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        etName = findViewById(R.id.etName);
        etBio = findViewById(R.id.etBio);
        btnSave = findViewById(R.id.btnSave);
    }

    private void setupAuth() {
        if (!UserManager.getInstance().isLoggedIn()) {
            finish();
            return;
        }

        currentUserId = UserManager.getInstance().getCurrentUserId();
        usersRef = FirebaseDatabase.getInstance().getReference("users");
    }

    private void loadCurrentUserData() {
        usersRef.child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    currentUser = snapshot.getValue(User.class);
                    if (currentUser != null) {
                        populateFields();
                    }
                } else {
                    Toast.makeText(EditProfileActivity.this, "Không tìm thấy thông tin người dùng", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(EditProfileActivity.this, "Lỗi tải thông tin người dùng", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void populateFields() {
        if (currentUser.getName() != null) {
            etName.setText(currentUser.getName());
        }

        if (currentUser.getBio() != null) {
            etBio.setText(currentUser.getBio());
        }
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnSave.setOnClickListener(v -> saveProfile());
    }

    private void saveProfile() {
        String name = etName.getText().toString().trim();
        String bio = etBio.getText().toString().trim();

        // Validate input
        if (TextUtils.isEmpty(name)) {
            etName.setError("Tên không được để trống");
            etName.requestFocus();
            return;
        }

        if (name.length() < 2) {
            etName.setError("Tên phải có ít nhất 2 ký tự");
            etName.requestFocus();
            return;
        }

        if (name.length() > 50) {
            etName.setError("Tên không được vượt quá 50 ký tự");
            etName.requestFocus();
            return;
        }

        if (bio.length() > 200) {
            etBio.setError("Giới thiệu không được vượt quá 200 ký tự");
            etBio.requestFocus();
            return;
        }

        // Disable save button to prevent multiple clicks
        btnSave.setEnabled(false);

        // Update user data
        currentUser.setName(name);
        currentUser.setBio(bio);

        // Save to Firebase
        usersRef.child(currentUserId).setValue(currentUser)
            .addOnSuccessListener(aVoid -> {
                Toast.makeText(EditProfileActivity.this, "Cập nhật hồ sơ thành công", Toast.LENGTH_SHORT).show();

                // Return to ProfileActivity
                Intent intent = new Intent();
                intent.putExtra("updated", true);
                setResult(RESULT_OK, intent);
                finish();
            })
            .addOnFailureListener(e -> {
                Toast.makeText(EditProfileActivity.this, "Lỗi cập nhật hồ sơ: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                btnSave.setEnabled(true);
            });
    }
}
