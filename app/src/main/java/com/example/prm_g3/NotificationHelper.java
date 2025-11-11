package com.example.prm_g3;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.example.prm_g3.activity.RecipeDetailActivity;

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
                    NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription(CHANNEL_DESCRIPTION);
            channel.enableVibration(true);
            channel.enableLights(true);

            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    public void showCommentNotification(String recipeId, String recipeTitle, String commenterName,
            String commentContent) {
        try {
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
                    .setSmallIcon(android.R.drawable.ic_dialog_info)
                    .setContentTitle("Bình luận mới")
                    .setContentText(commenterName + " đã bình luận vào công thức: " + recipeTitle)
                    .setStyle(new NotificationCompat.BigTextStyle()
                            .bigText(commenterName + " đã bình luận: \"" +
                                    (commentContent.length() > 100 ? commentContent.substring(0, 100) + "..."
                                            : commentContent)
                                    +
                                    "\" vào công thức \"" + recipeTitle + "\""))
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .setDefaults(NotificationCompat.DEFAULT_SOUND | NotificationCompat.DEFAULT_VIBRATE);

            // Hiển thị notification
            if (notificationManager != null) {
                notificationManager.notify(NOTIFICATION_ID + recipeId.hashCode(), builder.build());
                Log.d("NotificationHelper", "Notification sent for recipe: " + recipeId);
            }
        } catch (Exception e) {
            Log.e("NotificationHelper", "Error showing notification: " + e.getMessage(), e);
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
