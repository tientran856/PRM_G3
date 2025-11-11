package com.example.prm_g3.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.prm_g3.R;
import com.example.prm_g3.RecipesListActivity;
import com.example.prm_g3.UserManager;
import com.example.prm_g3.adapters.NotificationAdapter;
import com.example.prm_g3.models.Notification;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NotificationsActivity extends AppCompatActivity
        implements NotificationAdapter.OnNotificationClickListener {

    private ImageButton btnBack;
    private TextView tvTitle;
    private LinearLayout emptyMessageLayout;
    private RecyclerView recyclerViewNotifications;
    private NotificationAdapter adapter;
    private List<Notification> notificationList;
    private DatabaseReference notificationsRef;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        setupStatusBar();
        initViews();
        setupRecyclerView();
        setupBottomNav();

        currentUserId = UserManager.getInstance().getCurrentUserId();
        if (currentUserId == null) {
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadNotifications();
    }

    private void setupStatusBar() {
        getWindow().setStatusBarColor(Color.parseColor("#0D0D1A"));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    decorView.getSystemUiVisibility() | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            getWindow().getInsetsController().setSystemBarsAppearance(
                    android.view.WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                    android.view.WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS);
        }
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tvTitle = findViewById(R.id.tvTitle);
        emptyMessageLayout = findViewById(R.id.emptyMessageLayout);
        recyclerViewNotifications = findViewById(R.id.recyclerViewNotifications);

        tvTitle.setText("Thông báo");

        btnBack.setOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        notificationList = new ArrayList<>();
        adapter = new NotificationAdapter(this, notificationList, this);
        recyclerViewNotifications.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewNotifications.setAdapter(adapter);
    }

    private void loadNotifications() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        notificationsRef = database.getReference("notifications");

        // Query notifications for current user, ordered by timestamp descending
        Query query = notificationsRef.orderByChild("userId").equalTo(currentUserId);

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                notificationList.clear();

                for (DataSnapshot data : snapshot.getChildren()) {
                    try {
                        Notification notification = data.getValue(Notification.class);
                        if (notification != null) {
                            notification.id = data.getKey();
                            notificationList.add(notification);
                        }
                    } catch (Exception e) {
                        android.util.Log.e("NotificationsActivity",
                                "Error parsing notification: " + data.getKey() + " - " + e.getMessage(), e);
                    }
                }

                // Sort by timestamp descending (newest first)
                Collections.sort(notificationList, (a, b) -> Long.compare(b.timestamp, a.timestamp));

                adapter.notifyDataSetChanged();
                updateEmptyState();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(NotificationsActivity.this, "Lỗi tải thông báo", Toast.LENGTH_SHORT).show();
                android.util.Log.e("NotificationsActivity", "Error loading notifications: " + error.getMessage());
            }
        });
    }

    private void updateEmptyState() {
        if (notificationList.isEmpty()) {
            emptyMessageLayout.setVisibility(View.VISIBLE);
            recyclerViewNotifications.setVisibility(View.GONE);
        } else {
            emptyMessageLayout.setVisibility(View.GONE);
            recyclerViewNotifications.setVisibility(View.VISIBLE);
        }
    }

    private void setupBottomNav() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_home);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                Intent intent = new Intent(NotificationsActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
                return true;
            } else if (id == R.id.nav_recipes) {
                Intent intent = new Intent(NotificationsActivity.this, RecipesListActivity.class);
                startActivity(intent);
                finish();
                return true;
            } else if (id == R.id.nav_plan) {
                Intent intent = new Intent(NotificationsActivity.this, MealPlanActivity.class);
                startActivity(intent);
                finish();
                return true;
            } else if (id == R.id.nav_favorite) {
                Intent intent = new Intent(NotificationsActivity.this, FavoritesActivity.class);
                startActivity(intent);
                finish();
                return true;
            } else if (id == R.id.nav_profile) {
                Intent intent = new Intent(NotificationsActivity.this, ProfileActivity.class);
                startActivity(intent);
                finish();
                return true;
            }
            return false;
        });
    }

    @Override
    public void onNotificationClick(Notification notification) {
        // Mark as read
        if (!notification.isRead && notification.id != null) {
            DatabaseReference notificationRef = FirebaseDatabase.getInstance()
                    .getReference("notifications")
                    .child(notification.id);
            notificationRef.child("isRead").setValue(true);
        }

        // Navigate to recipe detail
        if (notification.recipeId != null) {
            Intent intent = new Intent(this, RecipeDetailActivity.class);
            intent.putExtra("recipeId", notification.recipeId);
            startActivity(intent);
        }
    }
}
