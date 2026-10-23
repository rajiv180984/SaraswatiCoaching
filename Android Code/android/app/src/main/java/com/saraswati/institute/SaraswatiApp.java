package com.saraswati.institute;

import android.app.Application;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.saraswati.institute.network.ApiClient;
import com.saraswati.institute.network.SessionManager;

import java.security.Security;

import org.conscrypt.Conscrypt;

/**
 * Application entry point.
 *
 * <p>Installs Conscrypt as the top-priority security provider so TLS 1.3 is available on
 * every supported API level — the platform providers only negotiate it from API 29, while
 * this app's {@code minSdk} is 24 — then initialises {@link ApiClient} for auth-service.
 *
 * <p>Also registers the hook that {@code TokenAuthenticator} fires when a refresh token is
 * rejected, which is the one case where the user has to be sent back to the login screen
 * from outside any activity.
 */
public class SaraswatiApp extends Application {

    private static final String TAG = "SaraswatiApp";

    @Override
    public void onCreate() {
        super.onCreate();

        try {
            Security.insertProviderAt(Conscrypt.newProvider(), 1);
            Log.i(TAG, "Conscrypt installed - TLS 1.3 available on every API level");
        } catch (Throwable t) {
            Log.w(TAG, "Could not install Conscrypt; relying on platform TLS", t);
        }

        ApiClient.init(this);
        ApiClient.setSessionExpiredListener(this::onSessionExpired);
    }

    /** Called from an OkHttp thread, so the restart is posted to the main looper. */
    private void onSessionExpired() {
        Log.i(TAG, "Session expired; returning to the login screen");
        SessionManager.getInstance(this).clear();
        new Handler(Looper.getMainLooper()).post(() -> {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }
}
