package com.saraswati.institute;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import java.util.ArrayList;
import java.util.List;

/**
 * Two-view-type adapter for the Tests tab.
 *   TYPE_HEADER  → subject section label
 *   TYPE_TEST    → upcoming or completed test card
 */
public class TestListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public interface OnTestAction {
        /** Called when the user taps Start / Download on an upcoming test. */
        void onStart(TestItem item);
    }

    private List<TestItem> items = new ArrayList<>();
    private final OnTestAction listener;

    public TestListAdapter(List<TestItem> items, OnTestAction listener) {
        this.items    = items;
        this.listener = listener;
    }

    public void setItems(List<TestItem> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    // ── view type ──────────────────────────────────────────────────────────────

    @Override
    public int getItemViewType(int position) {
        return items.get(position).type;
    }

    // ── create ─────────────────────────────────────────────────────────────────

    @NonNull @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inf = LayoutInflater.from(parent.getContext());
        if (viewType == TestItem.TYPE_HEADER) {
            View v = inf.inflate(R.layout.item_test_section_header, parent, false);
            return new HeaderVH(v);
        }
        View v = inf.inflate(R.layout.item_test_card, parent, false);
        return new TestVH(v);
    }

    // ── bind ───────────────────────────────────────────────────────────────────

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        TestItem item = items.get(position);
        if (item.type == TestItem.TYPE_HEADER) {
            ((HeaderVH) holder).bind(item);
        } else {
            ((TestVH) holder).bind(item, listener);
        }
    }

    @Override public int getItemCount() { return items.size(); }

    // ══════════════════════════════════════════════════════════════════════════
    // Section header VH
    // ══════════════════════════════════════════════════════════════════════════

    static class HeaderVH extends RecyclerView.ViewHolder {
        final TextView title;
        HeaderVH(View v) { super(v); title = v.findViewById(R.id.header_title); }
        void bind(TestItem item) { title.setText(item.sectionTitle); }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Test card VH
    // ══════════════════════════════════════════════════════════════════════════

    static class TestVH extends RecyclerView.ViewHolder {
        final View          subjectDot;
        final TextView      subjectLabel, statusBadge, testTitle, testMeta;
        // completed
        final View          sectionCompleted;
        final ProgressBar   scoreBar;
        final TextView      scoreValue, percentileValue, rankValue;
        // upcoming
        final View          sectionUpcoming;
        final TextView      upcomingHint;
        final MaterialButton actionButton;

        TestVH(View v) {
            super(v);
            subjectDot       = v.findViewById(R.id.subject_dot);
            subjectLabel     = v.findViewById(R.id.subject_label);
            statusBadge      = v.findViewById(R.id.status_badge);
            testTitle        = v.findViewById(R.id.test_title);
            testMeta         = v.findViewById(R.id.test_meta);
            sectionCompleted = v.findViewById(R.id.section_completed);
            scoreBar         = v.findViewById(R.id.score_bar);
            scoreValue       = v.findViewById(R.id.score_value);
            percentileValue  = v.findViewById(R.id.percentile_value);
            rankValue        = v.findViewById(R.id.rank_value);
            sectionUpcoming  = v.findViewById(R.id.section_upcoming);
            upcomingHint     = v.findViewById(R.id.upcoming_hint);
            actionButton     = v.findViewById(R.id.action_button);
        }

        void bind(TestItem item, OnTestAction listener) {
            android.content.Context ctx = itemView.getContext();

            // Subject colour dot
            subjectDot.setBackgroundTintList(
                ColorStateList.valueOf(ctx.getColor(item.subjectColor)));

            subjectLabel.setText(item.subject);
            testTitle.setText(item.title);
            testMeta.setText(item.dateLabel);

            if (item.isCompleted) {
                bindCompleted(item, ctx);
            } else {
                bindUpcoming(item, ctx, listener);
            }
        }

        private void bindCompleted(TestItem item, android.content.Context ctx) {
            // Badge: green "Completed"
            statusBadge.setText(ctx.getString(R.string.tests_completed));
            statusBadge.setTextColor(ctx.getColor(R.color.positive));
            statusBadge.setBackgroundTintList(
                ColorStateList.valueOf(ctx.getColor(R.color.positive_light)));

            sectionCompleted.setVisibility(View.VISIBLE);
            sectionUpcoming.setVisibility(View.GONE);

            int progressPct = item.maxScore > 0
                ? (int) (100f * item.score / item.maxScore) : 0;
            scoreBar.setProgress(progressPct);

            scoreValue.setText(item.score + " / " + item.maxScore);
            percentileValue.setText(
                String.format(java.util.Locale.getDefault(), "%.1f", item.percentile));
            rankValue.setText(item.rank + " / " + item.totalStudents);
        }

        private void bindUpcoming(TestItem item, android.content.Context ctx, OnTestAction listener) {
            // Badge: blue "Online" or grey "Offline"
            if (item.isOnline) {
                statusBadge.setText(ctx.getString(R.string.tests_online));
                statusBadge.setTextColor(ctx.getColor(R.color.reliance_blue));
                statusBadge.setBackgroundTintList(
                    ColorStateList.valueOf(ctx.getColor(R.color.reliance_blue_light)));
            } else {
                statusBadge.setText(ctx.getString(R.string.tests_offline));
                statusBadge.setTextColor(ctx.getColor(R.color.text_secondary));
                statusBadge.setBackgroundTintList(
                    ColorStateList.valueOf(ctx.getColor(R.color.surface_moderate)));
            }

            sectionCompleted.setVisibility(View.GONE);
            sectionUpcoming.setVisibility(View.VISIBLE);

            if (item.isOnline) {
                upcomingHint.setText(ctx.getString(R.string.tests_hint_online));
                actionButton.setText(ctx.getString(R.string.tests_start));
            } else {
                upcomingHint.setText(ctx.getString(R.string.tests_hint_offline));
                actionButton.setText(ctx.getString(R.string.tests_download));
            }

            actionButton.setOnClickListener(v -> {
                if (listener != null) listener.onStart(item);
            });
        }
    }
}
