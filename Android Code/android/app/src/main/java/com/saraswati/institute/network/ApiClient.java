package com.saraswati.institute.network;

import android.content.Context;
import android.util.Log;

import androidx.annotation.Nullable;

import com.saraswati.institute.BuildConfig;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Single entry point for the auth-service API.
 *
 * <p>Base URL {@link ApiConfig#BASE_URL} over HTTPS with TLS 1.3 preferred and TLS 1.2 as the
 * floor; the server certificate is pinned and the system CAs still apply (see
 * {@link TlsConfig}). Requests carry the bearer access token via {@link AuthInterceptor} and
 * a 401 triggers one refresh through {@link TokenAuthenticator}.
 *
 * <p>Call {@link #init(Context)} once from {@code Application.onCreate()} before any
 * {@link #getAuthApi()} use.
 */
public final class ApiClient {

    private static final String TAG = "ApiClient";

    private static volatile AuthApiService authApi;

    /** Invoked when the refresh token is rejected and the user has to sign in again. */
    public interface SessionExpiredListener {
        void onSessionExpired();
    }

    @Nullable
    private static volatile SessionExpiredListener sessionExpiredListener;

    private ApiClient() { }

    public static synchronized void init(Context context) {
        if (authApi != null) {
            return;
        }
        Context app = context.getApplicationContext();
        TlsConfig tls = TlsConfig.create(app);
        Log.i(TAG, "auth-service " + ApiConfig.BASE_URL + " - trusted certificates: "
                + TlsConfig.describe(app));

        SessionManager session = SessionManager.getInstance(app);

        OkHttpClient.Builder client = new OkHttpClient.Builder()
                .connectionSpecs(Collections.singletonList(TlsConfig.MODERN_TLS_ONLY))
                .sslSocketFactory(tls.sslContext().getSocketFactory(), tls.trustManager())
                .hostnameVerifier(tls.hostnameVerifier(ApiConfig.HOST))
                .addInterceptor(new AuthInterceptor(session))
                .authenticator(new TokenAuthenticator(session, new TokenAuthenticator.ServiceProvider() {
                    @Override
                    public AuthApiService get() {
                        return getAuthApi();
                    }
                }))
                .connectTimeout(20, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true);

        if (BuildConfig.DEBUG) {
            // Headers and bodies carry passwords and tokens, so only the request line is logged.
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BASIC);
            logging.redactHeader("Authorization");
            client.addInterceptor(logging);
        }

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ApiConfig.BASE_URL)
                .client(client.build())
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        authApi = retrofit.create(AuthApiService.class);
    }

    public static AuthApiService getAuthApi() {
        AuthApiService api = authApi;
        if (api == null) {
            throw new IllegalStateException(
                    "ApiClient.init(context) must be called before getAuthApi() - "
                            + "is android:name=\".SaraswatiApp\" still set in the manifest?");
        }
        return api;
    }

    public static void setSessionExpiredListener(@Nullable SessionExpiredListener listener) {
        sessionExpiredListener = listener;
    }

    /** Called from {@link TokenAuthenticator} on a background thread. */
    static void notifySessionExpired() {
        SessionExpiredListener listener = sessionExpiredListener;
        if (listener != null) {
            listener.onSessionExpired();
        }
    }
}
