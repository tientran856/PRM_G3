package com.example.prm_g3;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.prm_g3.activity.AuthActivity;
import com.example.prm_g3.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProfileActivity extends AppCompatActivity {

    private ImageView imgAvatar, btnBack;
    private TextView tvUserName, tvUserEmail, tvUserBio, tvJoinedDate;
    private Button btnLogout, btnEditProfile;

    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        initViews();
        setupAuth();
        loadUserProfile();
        setupListeners();
    }

    private void initViews() {
        imgAvatar = findViewById(R.id.imgAvatar);
        btnBack = findViewById(R.id.btnBack);
        tvUserName = findViewById(R.id.tvUserName);
        tvUserEmail = findViewById(R.id.tvUserEmail);
        tvUserBio = findViewById(R.id.tvUserBio);
        tvJoinedDate = findViewById(R.id.tvJoinedDate);
        btnLogout = findViewById(R.id.btnLogout);
        btnEditProfile = findViewById(R.id.btnEditProfile);
    }

    private void setupAuth() {
        mAuth = FirebaseAuth.getInstance();

        if (!UserManager.getInstance().isLoggedIn()) {
            startActivity(new Intent(this, AuthActivity.class));
            finish();
            return;
        }

        currentUserId = UserManager.getInstance().getCurrentUserId();
        usersRef = FirebaseDatabase.getInstance().getReference("users");
    }

    private void loadUserProfile() {
        usersRef.child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    User user = snapshot.getValue(User.class);
                    if (user != null) {
                        displayUserInfo(user);
                    }
                } else {
                    // Nếu chưa có thông tin user trong database, tạo mới từ Firebase Auth
                    createUserProfile();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ProfileActivity.this, "Lỗi tải thông tin người dùng", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createUserProfile() {
        if (UserManager.getInstance().isLoggedIn()) {
            User newUser = new User(
                currentUserId,
                UserManager.getInstance().getCurrentUserDisplayName() != null ?
                    UserManager.getInstance().getCurrentUserDisplayName() : "Người dùng",
                UserManager.getInstance().getCurrentUserEmail() != null ?
                    UserManager.getInstance().getCurrentUserEmail() : ""
            );

            usersRef.child(currentUserId).setValue(newUser)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Tạo hồ sơ người dùng thành công", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi tạo hồ sơ người dùng", Toast.LENGTH_SHORT).show();
                });
        }
    }

    private void displayUserInfo(User user) {
        tvUserName.setText(user.getName());
        tvUserEmail.setText(user.getEmail());

        if (user.getBio() != null && !user.getBio().isEmpty()) {
            tvUserBio.setText(user.getBio());
        } else {
            tvUserBio.setText("Chưa có thông tin giới thiệu");
        }

        if (user.getJoined_at() != null) {
            tvJoinedDate.setText("Tham gia: " + formatJoinedDate(user.getJoined_at()));
        } else {
            tvJoinedDate.setText("Tham gia: Không rõ");
        }

        // TODO: Load avatar image using Glide or Picasso if avatar_url is available
        if (user.getAvatar_url() != null && !user.getAvatar_url().isEmpty()) {
            // Load avatar image here
        }
    }

    private String formatJoinedDate(String timestamp) {
        try {
            // Nếu timestamp là ISO format
            if (timestamp.contains("T")) {
                return timestamp.substring(0, 10); // Lấy phần ngày yyyy-MM-dd
            }
            // Nếu timestamp là milliseconds
            long millis = Long.parseLong(timestamp);
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy");
            return sdf.format(new java.util.Date(millis));
        } catch (Exception e) {
            return timestamp;
        }
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnLogout.setOnClickListener(v -> showLogoutDialog());

        btnEditProfile.setOnClickListener(v -> {
            // TODO: Implement edit profile functionality
            Toast.makeText(this, "Tính năng chỉnh sửa hồ sơ sẽ được cập nhật", Toast.LENGTH_SHORT).show();
        });
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
            .setTitle("Đăng xuất")
            .setMessage("Bạn có chắc chắn muốn đăng xuất?")
            .setPositiveButton("Đăng xuất", (dialog, which) -> performLogout())
            .setNegativeButton("Hủy", null)
            .show();
    }

    private void performLogout() {
        mAuth.signOut();
        UserManager.getInstance().clearUser();
        Intent intent = new Intent(this, AuthActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
