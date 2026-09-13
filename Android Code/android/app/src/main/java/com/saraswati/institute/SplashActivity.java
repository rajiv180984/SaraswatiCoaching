package com.saraswati.institute;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;

import androidx.appcompat.app.AppCompatActivity;

import com.saraswati.institute.network.SessionManager;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DURATION_MS = 2600;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        animateIn();

        new Handler(Looper.getMainLooper()).postDelayed(this::routeNext, SPLASH_DURATION_MS);
    }

    private void animateIn() {
        View logo    = findViewById(R.id.fl_logo);
        View name    = findViewById(R.id.tv_app_name);
        View line    = findViewById(R.id.accent_line);
        View tagline = findViewById(R.id.tv_tagline);
        View footer  = findViewById(R.id.tv_footer);

        // Initial state — hidden
        logo.setAlpha(0f);
        logo.setScaleX(0.4f);
        logo.setScaleY(0.4f);

        name.setAlpha(0f);
        name.setTranslationY(30f);

        line.setAlpha(0f);
        line.setScaleX(0f);

        tagline.setAlpha(0f);
        tagline.setTranslationY(20f);

        footer.setAlpha(0f);

        // Logo: scale-bounce in
        logo.animate()
            .alpha(1f).scaleX(1f).scaleY(1f)
            .setDuration(600)
            .setStartDelay(150)
            .setInterpolator(new OvershootInterpolator(1.2f))
            .start();

        // App name: fade + slide up
        name.animate()
            .alpha(1f).translationY(0f)
            .setDuration(500)
            .setStartDelay(550)
            .setInterpolator(new DecelerateInterpolator())
            .start();

        // Accent line: expand from center
        line.animate()
            .alpha(1f).scaleX(1f)
            .setDuration(400)
            .setStartDelay(850)
            .setInterpolator(new DecelerateInterpolator())
            .start();

        // Tagline: fade + slide up
        tagline.animate()
            .alpha(1f).translationY(0f)
            .setDuration(500)
            .setStartDelay(1000)
            .setInterpolator(new DecelerateInterpolator())
            .start();

        // Footer: fade in last
        footer.animate()
            .alpha(1f)
            .setDuration(500)
            .setStartDelay(1300)
            .start();
    }

    private void routeNext() {
        boolean loggedIn = SessionManager.getInstance(this).isLoggedIn();
        Class<?> next = loggedIn ? HomeActivity.class : LoginActivity.class;
        startActivity(new Intent(this, next));
        // Smooth cross-fade transition
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }

    @Override
    public void onBackPressed() {
        // Disable back press during splash
    }
}
