package com.example.prm_g3.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;  // ĐÃ THÊM
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.prm_g3.databinding.ActivityAuthBinding;
import com.example.prm_g3.models.User;
import com.example.prm_g3.R;  // ĐÃ THÊM
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class AuthActivity extends AppCompatActivity {

    private ActivityAuthBinding binding;
    private FirebaseAuth mAuth;
    private boolean isLoginMode = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAuthBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() != null) {
            goToMain();
            return;
        }

        setupUI();
    }

    private void setupUI() {
        updateMode();

        binding.btnAuth.setOnClickListener(v -> {
            if (isLoginMode) login();
            else register();
        });

        binding.tvToggle.setOnClickListener(v -> {
            isLoginMode = !isLoginMode;
            updateMode();
        });
    }

    private void updateMode() {
        if (isLoginMode) {
            binding.tilName.setVisibility(View.GONE);
            binding.btnAuth.setText(R.string.btn_login);
            binding.tvToggle.setText(R.string.tv_no_account);
        } else {
            binding.tilName.setVisibility(View.VISIBLE);
            binding.btnAuth.setText(R.string.btn_register);
            binding.tvToggle.setText(R.string.tv_has_account);
        }
    }

    private void login() {
        String email = getText(binding.etEmail);
        String pass = getText(binding.etPassword);
        if (!validate(email, pass, null)) return;

        mAuth.signInWithEmailAndPassword(email, pass)
                .addOnSuccessListener(a -> goToMain())
                .addOnFailureListener(e -> toast("Sai email hoặc mật khẩu"));
    }

    private void register() {
        String email = getText(binding.etEmail);
        String pass = getText(binding.etPassword);
        String name = getText(binding.etName);
        if (!validate(email, pass, name)) return;

        mAuth.createUserWithEmailAndPassword(email, pass)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser fUser = authResult.getUser();
                    if (fUser != null) {
                        saveUserToFirestore(fUser, name);
                        toast("Đăng ký thành công!");
                        goToMain();
                    }
                })
                .addOnFailureListener(e -> toast(e.getMessage()));
    }

    private void saveUserToFirestore(FirebaseUser fUser, String name) {
        User user = new User(fUser.getUid(), name, fUser.getEmail());
        FirebaseFirestore.getInstance()
                .collection("users")
                .document(fUser.getUid())
                .set(user)
                .addOnFailureListener(e -> android.util.Log.e("Auth", "Lưu user thất bại", e));
    }

    private boolean validate(String email, String pass, String name) {
        if (TextUtils.isEmpty(email)) { binding.etEmail.setError("Nhập email"); return false; }
        if (TextUtils.isEmpty(pass)) { binding.etPassword.setError("Nhập mật khẩu"); return false; }
        if (!isLoginMode && TextUtils.isEmpty(name)) { binding.etName.setError("Nhập tên"); return false; }
        if (pass.length() < 6) { toast("Mật khẩu ≥ 6 ký tự"); return false; }
        return true;
    }

    private String getText(com.google.android.material.textfield.TextInputEditText et) {
        return et.getText() != null ? et.getText().toString().trim() : "";
    }

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    private void goToMain() {
        startActivity(new Intent(this, com.example.prm_g3.MainActivity.class));
        finish();
    }
}