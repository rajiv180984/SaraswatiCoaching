package com.saraswati.institute;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.List;

public class NotificationActivity extends AppCompatActivity {

    private List<NotificationItem> notifications;
    private NotificationAdapter    adapter;
    private TextView               tvBanner;
    private LinearLayout           emptyState;
    private RecyclerView           rv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        setupToolbar();

        tvBanner   = findViewById(R.id.tv_unread_banner);
        emptyState = findViewById(R.id.empty_state);
        rv         = findViewById(R.id.rv_notifications);

        notifications = buildDemoData();
        adapter = new NotificationAdapter(notifications, this::onItemClicked);

        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        rv.setAdapter(adapter);

        refreshUI();
    }

    // ── Toolbar ───────────────────────────────────────────

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Notifications");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.notification_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }
        if (id == R.id.action_mark_all_read) {
            markAllRead();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // ── Click handling ────────────────────────────────────

    private void onItemClicked(int position) {
        NotificationItem notif = notifications.get(position);
        if (!notif.isRead) {
            notif.isRead = true;
            adapter.notifyItemChanged(position);
            refreshUI();
        }
    }

    private void markAllRead() {
        boolean changed = false;
        for (NotificationItem n : notifications) {
            if (!n.isRead) { n.isRead = true; changed = true; }
        }
        if (changed) {
            adapter.notifyDataSetChanged();
            refreshUI();
        }
    }

    // ── UI state ──────────────────────────────────────────

    private void refreshUI() {
        int unread = countUnread();
        boolean isEmpty = notifications.isEmpty();

        // Banner
        if (unread > 0) {
            tvBanner.setVisibility(View.VISIBLE);
            tvBanner.setText(unread + " unread notification" + (unread > 1 ? "s" : ""));
        } else {
            tvBanner.setVisibility(View.GONE);
        }

        // Empty state vs list
        if (isEmpty) {
            rv.setVisibility(View.GONE);
            emptyState.setVisibility(View.VISIBLE);
        } else {
            rv.setVisibility(View.VISIBLE);
            emptyState.setVisibility(View.GONE);
        }

        // Toolbar subtitle
        if (getSupportActionBar() != null) {
            getSupportActionBar().setSubtitle(unread > 0 ? unread + " new" : null);
        }
    }

    private int countUnread() {
        int count = 0;
        for (NotificationItem n : notifications) {
            if (!n.isRead) count++;
        }
        return count;
    }

    // ── Demo data ─────────────────────────────────────────

    private List<NotificationItem> buildDemoData() {
        List<NotificationItem> list = new ArrayList<>();

        // ── UNREAD (3) ──
        list.add(new NotificationItem(
            NotificationItem.TYPE_TEST,
            "Mathematics Test Tomorrow",
            "Chapter 6 & 7 test is scheduled for tomorrow, 24 May at 10:00 AM. Duration: 45 min, 30 marks.",
            "2 min ago",
            false
        ));
        list.add(new NotificationItem(
            NotificationItem.TYPE_FEE,
            "Fee Payment Reminder",
            "Monthly fee of ₹2,500 is due by 31 May 2026. A late fee of ₹100 applies after the due date.",
            "1 hour ago",
            false
        ));
        list.add(new NotificationItem(
            NotificationItem.TYPE_RESULT,
            "Unit Test Result Published",
            "Your Physics Unit Test result is out! View your score, percentile, and rank in the Tests tab.",
            "3 hours ago",
            false
        ));

        // ── READ (5) ──
        list.add(new NotificationItem(
            NotificationItem.TYPE_ANNOUNCEMENT,
            "Holiday Notice — 26 May",
            "The institute will remain closed on 26 May (Eid-ul-Adha). Regular classes resume from 27 May.",
            "Today",
            true
        ));
        list.add(new NotificationItem(
            NotificationItem.TYPE_ANNOUNCEMENT,
            "New Study Material Uploaded",
            "Chapter 7 Chemistry Notes and Mind Maps are now available. Download them from the Study tab.",
            "Yesterday",
            true
        ));
        list.add(new NotificationItem(
            NotificationItem.TYPE_SCHEDULE,
            "Class Schedule Change",
            "Monday's Science class (4:00–5:00 PM) has been shifted to 5:00–6:00 PM starting next week.",
            "2 days ago",
            true
        ));
        list.add(new NotificationItem(
            NotificationItem.TYPE_TEST,
            "Mock Test Now Available",
            "Physics Full Syllabus Mock Test is live. Attempt before 30 May to receive evaluation and ranking.",
            "3 days ago",
            true
        ));
        list.add(new NotificationItem(
            NotificationItem.TYPE_FEE,
            "Fee Receipt — April 2026",
            "Your fee receipt for April 2026 has been generated. Download it from Payment Details in the menu.",
            "1 week ago",
            true
        ));

        return list;
    }
}
