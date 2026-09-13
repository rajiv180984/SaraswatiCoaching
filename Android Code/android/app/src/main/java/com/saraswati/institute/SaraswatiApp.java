package com.saraswati.institute;

import android.app.Application;
import android.util.Log;

import com.saraswati.institute.network.ApiClient;

import java.security.Security;

import org.conscrypt.Conscrypt;

/**
 * Application entry point.
 *
 * <p>Installs Conscrypt as the top-priority security provider so that TLS 1.3 is available
 * on every supported API level (the platform providers only negotiate TLS 1.3 from API 29),
 * then initialises {@link ApiClient} for the saraswati-auth API.
 */
public class SaraswatiApp extends Application {

    private static final String TAG = "SaraswatiApp";

    @Override
    public void onCreate() {
        super.onCreate();

        try {
            Security.insertProviderAt(Conscrypt.newProvider(), 1);
            Log.i(TAG, "Conscrypt installed — TLS 1.3 available");
        } catch (Throwable t) {
            Log.w(TAG, "Could not install Conscrypt; relying on platform TLS", t);
        }

        ApiClient.init(this);
    }
}
