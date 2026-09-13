package com.saraswati.institute.network;

import android.content.Context;
import android.util.Log;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Single entry point for the saraswati-auth API.
 *
 * <p>Base URL: {@value #BASE_URL} — HTTPS, <b>TLS 1.3 only</b>, self-signed dev certificate
 * bundled at {@code res/raw/saraswati_auth.pem} (see {@link TlsConfig}).
 *
 * <p>Call {@link #init(Context)} once from {@code Application.onCreate()} before any
 * {@link #getAuthApi()} usage.
 */
public final class ApiClient {

    private static final String TAG = "ApiClient";

    /**
     * Dev server. On the Android emulator the host loopback is {@code 10.0.2.2}; when
     * running against a device use {@code adb reverse tcp:8443 tcp:8443} so {@code localhost}
     * resolves to the dev machine.
     */
    public static final String BASE_URL = "https://13.127.80.33:8443/";
    private static final String HOST = "13.127.80.33";

    private static volatile AuthApiService authApi;

    private ApiClient() { }

    public static synchronized void init(Context context) {
        if (authApi != null) {
            return;
        }
        Context app = context.getApplicationContext();
        TlsConfig tls = TlsConfig.create(app);
        Log.i(TAG, "Pinned auth cert: " + TlsConfig.describe(app));

        SessionManager session = SessionManager.getInstance(app);

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        logging.redactHeader("Authorization");

        OkHttpClient client = new OkHttpClient.Builder()
                .connectionSpecs(Collections.singletonList(TlsConfig.TLS_1_3_ONLY))
                .sslSocketFactory(tls.sslContext().getSocketFactory(), tls.trustManager())
                .hostnameVerifier(tls.hostnameVerifier(HOST))
                .addInterceptor(new AuthInterceptor(session))
                .authenticator(new TokenAuthenticator(session, ApiClient::getAuthApi))
                .addInterceptor(logging)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        authApi = retrofit.create(AuthApiService.class);
    }

    public static AuthApiService getAuthApi() {
        AuthApiService api = authApi;
        if (api == null) {
            throw new IllegalStateException(
                    "ApiClient.init(context) must be called before getAuthApi() — "
                    + "did you set android:name=\".SaraswatiApp\" in the manifest?");
        }
        return api;
    }
}
