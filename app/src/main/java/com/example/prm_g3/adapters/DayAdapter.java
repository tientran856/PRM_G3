package com.example.prm_g3.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.prm_g3.R;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class DayAdapter extends RecyclerView.Adapter<DayAdapter.ViewHolder> {
    private Context context;
    private List<Calendar> days;
    private int selectedPosition = -1;
    private OnDayClickListener listener;

    public interface OnDayClickListener {
        void onDayClick(int position, Calendar day);
    }

    public DayAdapter(Context context, List<Calendar> days) {
        this.context = context;
        this.days = days;
        // Set today as selected by default
        Calendar today = Calendar.getInstance();
        for (int i = 0; i < days.size(); i++) {
            if (isSameDay(days.get(i), today)) {
                selectedPosition = i;
                break;
            }
        }
    }

    public void setOnDayClickListener(OnDayClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_day, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public int getItemCount() {
        return days.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDayName, tvDayNumber;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDayName = itemView.findViewById(R.id.tvDayName);
            tvDayNumber = itemView.findViewById(R.id.tvDayNumber);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        Calendar day = days.get(position);

        String[] dayNames = { "CN", "T2", "T3", "T4", "T5", "T6", "T7" };
        int dayOfWeek = day.get(Calendar.DAY_OF_WEEK);
        String dayName = dayNames[dayOfWeek - 1];

        holder.tvDayName.setText(dayName);
        holder.tvDayNumber.setText(String.valueOf(day.get(Calendar.DAY_OF_MONTH)));

        boolean isSelected = position == selectedPosition;
        holder.itemView.setSelected(isSelected);
        holder.itemView.setBackgroundResource(
                isSelected ? R.drawable.day_selected_background : R.drawable.day_unselected_background);
        holder.tvDayName.setTextColor(isSelected ? 0xFFFFFFFF : 0xFF666666);
        holder.tvDayNumber.setTextColor(isSelected ? 0xFFFFFFFF : 0xFF666666);

        holder.itemView.setOnClickListener(v -> {
            int oldPosition = selectedPosition;
            selectedPosition = position;
            notifyItemChanged(oldPosition);
            notifyItemChanged(position);
            if (listener != null) {
                listener.onDayClick(position, day);
            }
        });
    }

    private boolean isSameDay(Calendar cal1, Calendar cal2) {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }
}
