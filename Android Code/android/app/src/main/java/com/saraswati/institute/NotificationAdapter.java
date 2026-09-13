package com.saraswati.institute;

import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.VH> {

    public interface OnItemClick {
        void onClick(int position);
    }

    private final List<NotificationItem> items;
    private final OnItemClick            listener;

    public NotificationAdapter(List<NotificationItem> items, OnItemClick listener) {
        this.items    = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_notification, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        NotificationItem item = items.get(position);
        android.content.Context ctx = h.itemView.getContext();

        // Title — bold when unread
        h.tvTitle.setText(item.title);
        h.tvTitle.setTypeface(null, item.isRead ? Typeface.NORMAL : Typeface.BOLD);

        h.tvDescription.setText(item.description);
        h.tvTime.setText(item.time);

        // Unread dot
        h.viewDot.setVisibility(item.isRead ? View.GONE : View.VISIBLE);

        // Row background: light blue for unread, white for read
        int bgColor = ctx.getResources().getColor(
            item.isRead ? R.color.surface_subtle : R.color.reliance_blue_light,
            ctx.getTheme());
        h.itemView.setBackgroundColor(bgColor);

        // Type icon + circle background color
        int iconRes  = iconForType(item.type);
        int circleColor = colorForType(item.type);
        h.ivIcon.setImageResource(iconRes);
        h.flCircle.setBackgroundTintList(ColorStateList.valueOf(circleColor));

        h.itemView.setOnClickListener(v -> {
            int pos = h.getAdapterPosition();
            if (pos != RecyclerView.NO_ID) listener.onClick(pos);
        });
    }

    @Override
    public int getItemCount() { return items.size(); }

    // ── Helpers ───────────────────────────────────────────

    private int iconForType(int type) {
        switch (type) {
            case NotificationItem.TYPE_TEST:     return R.drawable.ic_performance;
            case NotificationItem.TYPE_FEE:      return R.drawable.ic_payment;
            case NotificationItem.TYPE_SCHEDULE: return R.drawable.ic_attendance;
            case NotificationItem.TYPE_RESULT:   return R.drawable.ic_course;
            default:                             return R.drawable.ic_about;  // ANNOUNCEMENT
        }
    }

    private int colorForType(int type) {
        switch (type) {
            case NotificationItem.TYPE_TEST:     return 0xFFE89A3C; // saffron
            case NotificationItem.TYPE_FEE:      return 0xFFD4322D; // red
            case NotificationItem.TYPE_SCHEDULE: return 0xFF7C3AED; // purple
            case NotificationItem.TYPE_RESULT:   return 0xFF1F8A5B; // green
            default:                             return 0xFF1646BF; // blue
        }
    }

    // ── ViewHolder ────────────────────────────────────────

    static class VH extends RecyclerView.ViewHolder {
        final FrameLayout flCircle;
        final ImageView   ivIcon;
        final TextView    tvTitle, tvDescription, tvTime;
        final View        viewDot;

        VH(@NonNull View v) {
            super(v);
            flCircle      = v.findViewById(R.id.fl_icon_circle);
            ivIcon        = v.findViewById(R.id.iv_type_icon);
            tvTitle       = v.findViewById(R.id.tv_notif_title);
            tvDescription = v.findViewById(R.id.tv_notif_description);
            tvTime        = v.findViewById(R.id.tv_notif_time);
            viewDot       = v.findViewById(R.id.view_unread_dot);
        }
    }
}
