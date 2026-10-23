package com.saraswati.institute.network;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * The signed-in session: the bearer access token, the opaque refresh token, their expiry
 * instants and the cached profile from the last {@code login} / {@code refresh-token} /
 * {@code users/me} response.
 *
 * <p>Stored in a private {@link SharedPreferences} file, encrypted at rest with
 * {@code EncryptedSharedPreferences} when androidx.security-crypto is able to initialise
 * (it needs a hardware-or-software keystore, which can fail on unusual devices); the
 * plaintext private file is the fallback so a keystore problem cannot lock the user out.
 *
 * <p>The service issues <b>rotating</b> refresh tokens with reuse detection: presenting a
 * refresh token that was already exchanged revokes every session for that user. So the
 * stored refresh token must be replaced on every refresh, and only one refresh may be in
 * flight — see {@link TokenAuthenticator}.
 */
public final class SessionManager {

    private static final String TAG = "SessionManager";

    private static final String PREFS = "saraswati_auth_session";

    /** Bump when the stored shape changes; a mismatch clears the file instead of mis-reading it. */
    private static final int SCHEMA_VERSION = 2;

    private static final String KEY_SCHEMA = "schema_version";
    private static final String KEY_ACCESS_TOKEN = "access_token";
    private static final String KEY_REFRESH_TOKEN = "refresh_token";
    private static final String KEY_TOKEN_TYPE = "token_type";
    private static final String KEY_EXPIRES_AT = "expires_at";           // epoch millis
    private static final String KEY_REFRESH_EXPIRES_AT = "refresh_expires_at"; // epoch millis
    private static final String KEY_USER_JSON = "user_json";

    /** Treat the access token as spent this long before its real expiry. */
    private static final long EXPIRY_SKEW_MS = 30_000L;

    private static volatile SessionManager instance;

    private final SharedPreferences prefs;
    private final Gson gson = new Gson();

    private SessionManager(Context context) {
        this.prefs = openPreferences(context.getApplicationContext());
        if (prefs.getInt(KEY_SCHEMA, 0) != SCHEMA_VERSION) {
            // Anything stored by an older build belongs to a different API; drop it.
            prefs.edit().clear().putInt(KEY_SCHEMA, SCHEMA_VERSION).apply();
        }
    }

    public static SessionManager getInstance(Context context) {
        if (instance == null) {
            synchronized (SessionManager.class) {
                if (instance == null) {
                    instance = new SessionManager(context);
                }
            }
        }
        return instance;
    }

    private static SharedPreferences openPreferences(Context context) {
        try {
            androidx.security.crypto.MasterKey masterKey =
                    new androidx.security.crypto.MasterKey.Builder(context)
                            .setKeyScheme(androidx.security.crypto.MasterKey.KeyScheme.AES256_GCM)
                            .build();
            return androidx.security.crypto.EncryptedSharedPreferences.create(
                    context,
                    PREFS,
                    masterKey,
                    androidx.security.crypto.EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    androidx.security.crypto.EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM);
        } catch (Throwable t) {
            Log.w(TAG, "Encrypted preferences unavailable; falling back to the private file", t);
            return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        }
    }

    // ── Save / clear ──────────────────────────────────────────────────────────

    /** Stores a full {@code login} / {@code refresh-token} response. */
    public synchronized void saveSession(TokenResponse tokens) {
        long now = System.currentTimeMillis();
        SharedPreferences.Editor editor = prefs.edit()
                .putInt(KEY_SCHEMA, SCHEMA_VERSION)
                .putString(KEY_ACCESS_TOKEN, tokens.getAccessToken())
                .putString(KEY_REFRESH_TOKEN, tokens.getRefreshToken())
                .putString(KEY_TOKEN_TYPE, tokens.getTokenType())
                .putLong(KEY_EXPIRES_AT, now + Math.max(0L, tokens.getExpiresInMillis()))
                .putLong(KEY_REFRESH_EXPIRES_AT, now + Math.max(0L, tokens.getRefreshExpiresInMillis()));

        if (tokens.getUser() != null) {
            editor.putString(KEY_USER_JSON, gson.toJson(tokens.getUser()));
        }
        editor.apply();
    }

    /** Replaces the cached profile after {@code GET}/{@code PUT /api/v1/users/me}. */
    public synchronized void saveUser(UserResponse user) {
        if (user != null) {
            prefs.edit().putString(KEY_USER_JSON, gson.toJson(user)).apply();
        }
    }

    public synchronized void clear() {
        prefs.edit().clear().putInt(KEY_SCHEMA, SCHEMA_VERSION).apply();
    }

    // ── Read ──────────────────────────────────────────────────────────────────

    @Nullable
    public String getAccessToken() {
        return prefs.getString(KEY_ACCESS_TOKEN, null);
    }

    @Nullable
    public String getRefreshToken() {
        return prefs.getString(KEY_REFRESH_TOKEN, null);
    }

    public String getTokenType() {
        return prefs.getString(KEY_TOKEN_TYPE, "Bearer");
    }

    public long getExpiresAt() {
        return prefs.getLong(KEY_EXPIRES_AT, 0L);
    }

    public long getRefreshExpiresAt() {
        return prefs.getLong(KEY_REFRESH_EXPIRES_AT, 0L);
    }

    /** The profile from the last auth response, or {@code null} when signed out. */
    @Nullable
    public UserResponse getCurrentUser() {
        String json = prefs.getString(KEY_USER_JSON, null);
        if (json == null) {
            return null;
        }
        try {
            return gson.fromJson(json, UserResponse.class);
        } catch (JsonSyntaxException e) {
            Log.w(TAG, "Discarding an unreadable cached profile", e);
            prefs.edit().remove(KEY_USER_JSON).apply();
            return null;
        }
    }

    // Convenience accessors so callers do not have to null-check getCurrentUser().

    @Nullable
    public String getUserId() {
        UserResponse u = getCurrentUser();
        return u != null ? u.getId() : null;
    }

    @Nullable
    public String getUsername() {
        UserResponse u = getCurrentUser();
        return u != null ? u.getUsername() : null;
    }

    @Nullable
    public String getUserEmail() {
        UserResponse u = getCurrentUser();
        return u != null ? u.getEmail() : null;
    }

    @Nullable
    public String getUserFirstName() {
        UserResponse u = getCurrentUser();
        return u != null ? u.getFirstName() : null;
    }

    @Nullable
    public String getUserLastName() {
        UserResponse u = getCurrentUser();
        return u != null ? u.getLastName() : null;
    }

    @Nullable
    public String getUserName() {
        UserResponse u = getCurrentUser();
        return u != null ? u.getFullName() : null;
    }

    @Nullable
    public String getUserRole() {
        UserResponse u = getCurrentUser();
        return u != null ? u.getRole() : null;
    }

    public boolean isAdmin() {
        UserResponse u = getCurrentUser();
        return u != null && u.isAdmin();
    }

    public boolean isEmailVerified() {
        UserResponse u = getCurrentUser();
        return u != null && u.isEmailVerified();
    }

    public boolean isUserEnabled() {
        UserResponse u = getCurrentUser();
        return u != null && u.isEnabled();
    }

    @Nullable
    public String getUserCreatedAt() {
        UserResponse u = getCurrentUser();
        return u != null ? u.getCreatedAt() : null;
    }

    @Nullable
    public String getUserLastLoginAt() {
        UserResponse u = getCurrentUser();
        return u != null ? u.getLastLoginAt() : null;
    }

    // ── State ─────────────────────────────────────────────────────────────────

    /**
     * True when there is still a usable session: a refresh token that has not expired.
     * The access token itself may well be stale — {@link TokenAuthenticator} renews it.
     */
    public boolean isLoggedIn() {
        String refreshToken = getRefreshToken();
        if (refreshToken == null || refreshToken.isEmpty()) {
            return false;
        }
        long refreshExpiresAt = getRefreshExpiresAt();
        return refreshExpiresAt <= 0L || System.currentTimeMillis() < refreshExpiresAt;
    }

    /** True when the access token is missing or within {@value #EXPIRY_SKEW_MS}ms of expiry. */
    public boolean isAccessTokenExpired() {
        String token = getAccessToken();
        if (token == null || token.isEmpty()) {
            return true;
        }
        long expiresAt = getExpiresAt();
        return expiresAt <= 0L || System.currentTimeMillis() >= (expiresAt - EXPIRY_SKEW_MS);
    }
}
