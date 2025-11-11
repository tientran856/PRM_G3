package com.example.prm_g3;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.example.prm_g3.R;
import com.example.prm_g3.activity.RecipeDetailActivity;
import com.example.prm_g3.models.Notification;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class NotificationHelper {
    private static final String CHANNEL_ID = "recipe_comments_channel";
    private static final String CHANNEL_NAME = "Thông báo bình luận";
    private static final String CHANNEL_DESCRIPTION = "Thông báo khi có người bình luận vào công thức của bạn";
    private static final int NOTIFICATION_ID = 1001;

    private Context context;
    private NotificationManager notificationManager;

    public NotificationHelper(Context context) {
        this.context = context;
        this.notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        createNotificationChannel();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH); // Tăng importance để đảm bảo hiển thị
            channel.setDescription(CHANNEL_DESCRIPTION);
            channel.enableVibration(true);
            channel.enableLights(true);
            channel.setShowBadge(true); // Hiển thị badge trên icon app

            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    public void showCommentNotification(String recipeId, String recipeTitle, String commenterName,
            String commentContent) {
        showCommentNotification(recipeId, recipeTitle, commenterName, commentContent, null);
    }

    public void showCommentNotification(String recipeId, String recipeTitle, String commenterName,
            String commentContent, String userId) {
        try {
            Log.d("NotificationHelper", "showCommentNotification called: recipeId=" + recipeId + ", userId=" + userId + ", commenterName=" + commenterName);
            
            // Lưu thông báo vào Firebase nếu có userId
            if (userId != null && !userId.isEmpty()) {
                saveNotificationToFirebase(userId, recipeId, recipeTitle, commenterName, commentContent);
                Log.d("NotificationHelper", "Notification saved to Firebase for userId: " + userId);
            } else {
                Log.w("NotificationHelper", "userId is null or empty, skipping Firebase save");
            }

            // Lấy current user ID để kiểm tra xem có nên hiển thị notification ngay không
            String currentUserId = com.example.prm_g3.UserManager.getInstance().getCurrentUserId();
            if (currentUserId == null) {
                currentUserId = "user_002";
            }
            
            // Chỉ hiển thị notification trên thiết bị của chủ sở hữu công thức
            // (userId là chủ sở hữu, currentUserId là người đang dùng thiết bị này)
            if (userId != null && userId.equals(currentUserId)) {
                Log.d("NotificationHelper", "Showing notification on device of recipe owner: " + userId);
                
                // Tạo Intent để mở RecipeDetailActivity khi click vào notification
                Intent intent = new Intent(context, RecipeDetailActivity.class);
                intent.putExtra("recipeId", recipeId);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                PendingIntent pendingIntent = PendingIntent.getActivity(
                        context,
                        0,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

                // Tạo notification
                NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_notification) // Dùng icon của app
                        .setContentTitle("Bình luận mới")
                        .setContentText(commenterName + " đã bình luận vào công thức: " + recipeTitle)
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(commenterName + " đã bình luận: \"" +
                                        (commentContent.length() > 100 ? commentContent.substring(0, 100) + "..."
                                                : commentContent)
                                        +
                                        "\" vào công thức \"" + recipeTitle + "\""))
                        .setPriority(NotificationCompat.PRIORITY_HIGH) // Tăng priority
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true)
                        .setDefaults(NotificationCompat.DEFAULT_SOUND | NotificationCompat.DEFAULT_VIBRATE)
                        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC); // Hiển thị trên lock screen

                // Hiển thị notification
                if (notificationManager != null) {
                    notificationManager.notify(NOTIFICATION_ID + recipeId.hashCode(), builder.build());
                    Log.d("NotificationHelper", "Notification displayed for recipe: " + recipeId);
                }
            } else {
                Log.d("NotificationHelper", "Skipping notification display: userId=" + userId + ", currentUserId=" + currentUserId + " (notification will appear in list when user opens app)");
            }
        } catch (Exception e) {
            Log.e("NotificationHelper", "Error showing notification: " + e.getMessage(), e);
        }
    }

    private void saveNotificationToFirebase(String userId, String recipeId, String recipeTitle,
            String commenterName, String commentContent) {
        try {
            Log.d("NotificationHelper", "Saving notification to Firebase: userId=" + userId + ", recipeId=" + recipeId);
            DatabaseReference notificationsRef = FirebaseDatabase.getInstance().getReference("notifications");
            Notification notification = new Notification(userId, recipeId, recipeTitle, commenterName, commentContent, "comment");
            
            String notificationId = notificationsRef.push().getKey();
            if (notificationId != null) {
                notification.id = notificationId;
                notificationsRef.child(notificationId).setValue(notification)
                        .addOnSuccessListener(aVoid -> {
                            Log.d("NotificationHelper", "Notification saved to Firebase successfully: " + notificationId + " for userId: " + userId);
                        })
                        .addOnFailureListener(e -> {
                            Log.e("NotificationHelper", "Error saving notification to Firebase: " + e.getMessage(), e);
                        });
            } else {
                Log.e("NotificationHelper", "Failed to generate notification ID");
            }
        } catch (Exception e) {
            Log.e("NotificationHelper", "Error saving notification to Firebase: " + e.getMessage(), e);
        }
    }

    public static boolean hasNotificationPermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            return nm != null && nm.areNotificationsEnabled();
        }
        return true; // Android < 13 không cần permission
    }
}
