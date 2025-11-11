package com.example.prm_g3.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.prm_g3.R;
import com.example.prm_g3.models.Notification;

import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

    private Context context;
    private List<Notification> notifications;
    private OnNotificationClickListener listener;

    public interface OnNotificationClickListener {
        void onNotificationClick(Notification notification);
    }

    public NotificationAdapter(Context context, List<Notification> notifications,
            OnNotificationClickListener listener) {
        this.context = context;
        this.notifications = notifications;
        this.listener = listener;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        Notification notification = notifications.get(position);

        // Set notification content
        if (notification.type != null && notification.type.equals("comment")) {
            holder.tvTitle.setText("Bình luận mới");
            String content = notification.commenterName + " đã bình luận vào công thức: " + notification.recipeTitle;
            holder.tvContent.setText(content);
        } else {
            holder.tvTitle.setText("Thông báo");
            holder.tvContent.setText(notification.commentContent != null ? notification.commentContent : "");
        }

        // Set time
        holder.tvTime.setText(formatTimeAgo(notification.timestamp));

        // Set read/unread state
        if (notification.isRead) {
            holder.itemView.setBackgroundColor(Color.WHITE);
            holder.tvTitle.setTextColor(Color.parseColor("#666666"));
            holder.tvContent.setTextColor(Color.parseColor("#999999"));
        } else {
            holder.itemView.setBackgroundColor(Color.parseColor("#F0F8FF"));
            holder.tvTitle.setTextColor(Color.parseColor("#333333"));
            holder.tvContent.setTextColor(Color.parseColor("#666666"));
        }

        // Set click listener
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onNotificationClick(notification);
            }
        });
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    private String formatTimeAgo(long timestamp) {
        long now = System.currentTimeMillis();
        long diff = now - timestamp;

        if (diff < 60000) { // less than 1 minute
            return "Vừa xong";
        } else if (diff < 3600000) { // less than 1 hour
            return (diff / 60000) + " phút trước";
        } else if (diff < 86400000) { // less than 1 day
            return (diff / 3600000) + " giờ trước";
        } else {
            return (diff / 86400000) + " ngày trước";
        }
    }

    public static class NotificationViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvContent, tvTime;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvContent = itemView.findViewById(R.id.tvContent);
            tvTime = itemView.findViewById(R.id.tvTime);
        }
    }
}
