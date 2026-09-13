package com.saraswati.institute;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.badge.BadgeUtils;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.saraswati.institute.network.ApiClient;
import com.saraswati.institute.network.ApiResponse;
import com.saraswati.institute.network.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Host activity for the student app shell.
 *
 * Layout:
 *   DrawerLayout
 *     ├─ MaterialToolbar  (hamburger ≡ + notification bell)
 *     ├─ FrameLayout      (fragment container)
 *     ├─ BottomNavigationView
 *     └─ NavigationView   (side drawer)
 */
public class HomeActivity extends AppCompatActivity {

    // Number of unread notifications — update from your API / data layer
    private static final int UNREAD_NOTIF_COUNT = 3;

    private DrawerLayout  drawerLayout;
    private MaterialToolbar toolbar;
    private BadgeDrawable notifBadge;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        toolbar      = findViewById(R.id.toolbar);
        drawerLayout = findViewById(R.id.drawer_layout);

        setSupportActionBar(toolbar);

        // Hamburger ↔ back-arrow animated toggle
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.drawer_open, R.string.drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Side drawer item clicks
        NavigationView navDrawer = findViewById(R.id.nav_drawer);
        populateDrawerHeader(navDrawer);
        navDrawer.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.drawer_logout) {
                logout();
                return true;
            }
            if (id == R.id.drawer_registration) {
                drawerLayout.closeDrawers();
                startActivity(new Intent(this, RegistrationActivity.class));
                return true;
            }
            // All other drawer items → placeholder with matching title
            String title = null;
            if (id == R.id.drawer_profile)      title = getString(R.string.nav_profile);
            else if (id == R.id.drawer_performance) title = getString(R.string.drawer_performance);
            else if (id == R.id.drawer_attendance)  title = getString(R.string.drawer_attendance);
            else if (id == R.id.drawer_course)      title = getString(R.string.drawer_course);
            else if (id == R.id.drawer_library)     title = getString(R.string.drawer_library);
            else if (id == R.id.drawer_payment)     title = getString(R.string.drawer_payment);
            else if (id == R.id.drawer_teachers)    title = getString(R.string.drawer_teachers);
            else if (id == R.id.drawer_refer)       title = getString(R.string.drawer_refer);
            else if (id == R.id.drawer_location)    title = getString(R.string.drawer_location);
            else if (id == R.id.drawer_contact)     title = getString(R.string.drawer_contact);
            else if (id == R.id.drawer_about)       title = getString(R.string.drawer_about);
            else if (id == R.id.drawer_grievance)   title = getString(R.string.drawer_grievance);
            else if (id == R.id.drawer_feedback)    title = getString(R.string.drawer_feedback);

            if (title != null) {
                swap(new PlaceholderFragment(title));
                setToolbarTitle(title);
                BottomNavigationView bn = findViewById(R.id.bottom_nav);
                bn.setSelectedItemId(0);
            }
            drawerLayout.closeDrawers();
            return true;
        });

        // Bottom navigation tab clicks
        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                swap(new HomeFragment());
                setToolbarTitle(getString(R.string.app_name));
                navDrawer.setCheckedItem(-1);
                return true;
            }
            if (id == R.id.nav_schedule) {
                swap(new PlaceholderFragment("Schedule"));
                setToolbarTitle(getString(R.string.nav_schedule));
                return true;
            }
            if (id == R.id.nav_tests) {
                swap(new TestsFragment());
                setToolbarTitle(getString(R.string.nav_tests));
                return true;
            }
            if (id == R.id.nav_study) {
                swap(new StudyMaterialFragment());
                setToolbarTitle(getString(R.string.nav_study_material));
                return true;
            }
            if (id == R.id.nav_doubts) {
                swap(new DoubtsFragment());
                setToolbarTitle(getString(R.string.nav_doubts));
                return true;
            }
            return false;
        });

        // Default landing — Home tab
        if (savedInstanceState == null) {
            swap(new HomeFragment());
            bottomNav.setSelectedItemId(R.id.nav_home);
            setToolbarTitle(getString(R.string.app_name));

            // Show promotion popup shortly after the UI settles
            new Handler(Looper.getMainLooper()).postDelayed(() ->
                PromoBannerDialog.showIfNeeded(this,
                    () -> startActivity(new Intent(this, RegistrationActivity.class))
                ), 700);
        }
    }

    // ── Toolbar menu (notification bell) ──────────────────

    @SuppressLint("UnsafeOptInUsageError")
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home_toolbar_menu, menu);

        // Attach badge after toolbar is fully laid out
        toolbar.post(() -> {
            notifBadge = BadgeDrawable.create(this);
            notifBadge.setBackgroundColor(getColor(R.color.saffron));
            notifBadge.setBadgeTextColor(getColor(R.color.white));
            if (UNREAD_NOTIF_COUNT > 0) {
                notifBadge.setNumber(UNREAD_NOTIF_COUNT);
                notifBadge.setVisible(true);
            } else {
                notifBadge.setVisible(false);
            }
            BadgeUtils.attachBadgeDrawable(notifBadge, toolbar, R.id.action_notifications);
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_notifications) {
            startActivity(new Intent(this, NotificationActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // ── Helpers ───────────────────────────────────────────

    private void swap(Fragment f) {
        getSupportFragmentManager().beginTransaction()
            .replace(R.id.fragment_container, f)
            .commit();
    }

    private void setToolbarTitle(String title) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
        }
    }

    /** Called by HomeFragment / TestsFragment to launch the test-taking screen. */
    public void openTestAttempt() {
        startActivity(new Intent(this, TestAttemptActivity.class));
    }

    /** Fills the drawer header with the cached profile from {@link SessionManager}. */
    private void populateDrawerHeader(NavigationView navDrawer) {
        View header = navDrawer.getHeaderView(0);
        if (header == null) return;

        SessionManager session = SessionManager.getInstance(this);
        String name = session.getUserName();

        TextView nameView = header.findViewById(R.id.drawer_name);
        if (nameView != null) {
            nameView.setText(!TextUtils.isEmpty(name) ? name : session.getUserEmail());
        }
    }

    // ── Logout ────────────────────────────────────────────

    private void logout() {
        // Best-effort server-side invalidation; the local session is cleared regardless.
        ApiClient.getAuthApi().logout().enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) { }
            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) { }
        });

        SessionManager.getInstance(this).clear();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
