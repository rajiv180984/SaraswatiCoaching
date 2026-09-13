package com.saraswati.institute.network;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;

/**
 * Local cache for the signed-in session: JWT access/refresh tokens and the full user
 * profile returned by the auth API. Read from any Activity via {@link #getInstance(Context)}
 * so the same data doesn't need to be re-fetched or passed screen to screen.
 *
 * <p>Backed by a private {@link SharedPreferences} file. For production you may want to
 * swap this for {@code EncryptedSharedPreferences} (androidx.security-crypto); the public
 * API of this class is deliberately small so that change stays local.
 */
public final class SessionManager {

    private static final String PREFS = "saraswati_auth_session";

    private static final String KEY_ACCESS_TOKEN  = "access_token";
    private static final String KEY_REFRESH_TOKEN = "refresh_token";
    private static final String KEY_TOKEN_TYPE    = "token_type";
    private static final String KEY_EXPIRES_AT    = "expires_at";      // epoch millis
    private static final String KEY_USER_JSON     = "user_json";       // full UserResponse, as JSON

    private static volatile SessionManager instance;

    private final SharedPreferences prefs;
    private final Gson gson = new Gson();

    private SessionManager(Context context) {
        this.prefs = context.getApplicationContext()
                .getSharedPreferences(PREFS, Context.MODE_PRIVATE);
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

    // ── Save / clear ──────────────────────────────────────────────────────────

    public synchronized void saveSession(AuthResponse auth) {
        SharedPreferences.Editor e = prefs.edit();
        e.putString(KEY_ACCESS_TOKEN, auth.getAccessToken());
        e.putString(KEY_REFRESH_TOKEN, auth.getRefreshToken());
        e.putString(KEY_TOKEN_TYPE, auth.getTokenType());
        e.putLong(KEY_EXPIRES_AT, System.currentTimeMillis() + Math.max(0, auth.getExpiresIn()));

        if (auth.getUser() != null) {
            e.putString(KEY_USER_JSON, gson.toJson(auth.getUser()));
        } else {
            e.remove(KEY_USER_JSON);
        }
        e.apply();
    }

    /** Update only the tokens (used after a silent refresh). */
    public synchronized void updateTokens(String accessToken, String refreshToken, long expiresInMs) {
        prefs.edit()
                .putString(KEY_ACCESS_TOKEN, accessToken)
                .putString(KEY_REFRESH_TOKEN, refreshToken)
                .putLong(KEY_EXPIRES_AT, System.currentTimeMillis() + Math.max(0, expiresInMs))
                .apply();
    }

    public synchronized void clear() {
        prefs.edit().clear().apply();
    }

    // ── Read ──────────────────────────────────────────────────────────────────

    public String getAccessToken()  { return prefs.getString(KEY_ACCESS_TOKEN, null); }
    public String getRefreshToken() { return prefs.getString(KEY_REFRESH_TOKEN, null); }
    public String getTokenType()    { return prefs.getString(KEY_TOKEN_TYPE, "Bearer"); }
    public long   getExpiresAt()    { return prefs.getLong(KEY_EXPIRES_AT, 0L); }

    /** The full user profile from the last login/register/refresh response, or {@code null}. */
    public UserResponse getCurrentUser() {
        String json = prefs.getString(KEY_USER_JSON, null);
        return json != null ? gson.fromJson(json, UserResponse.class) : null;
    }

    // Convenience accessors so callers don't need to null-check getCurrentUser() themselves.

    public Long    getUserId()        { UserResponse u = getCurrentUser(); return u != null ? u.getId() : null; }
    public String  getUsername()      { UserResponse u = getCurrentUser(); return u != null ? u.getUsername() : null; }
    public String  getUserEmail()     { UserResponse u = getCurrentUser(); return u != null ? u.getEmail() : null; }
    public String  getUserFirstName() { UserResponse u = getCurrentUser(); return u != null ? u.getFirstName() : null; }
    public String  getUserLastName()  { UserResponse u = getCurrentUser(); return u != null ? u.getLastName() : null; }
    public String  getUserName()      { UserResponse u = getCurrentUser(); return u != null ? u.getFullName() : null; }
    public String  getUserPhone()     { UserResponse u = getCurrentUser(); return u != null ? u.getPhone() : null; }
    public String  getUserRole()      { UserResponse u = getCurrentUser(); return u != null ? u.getRole() : null; }
    public boolean isUserEnabled()    { UserResponse u = getCurrentUser(); return u != null && u.isEnabled(); }
    public String  getUserCreatedAt() { UserResponse u = getCurrentUser(); return u != null ? u.getCreatedAt() : null; }
    public String  getUserLastLoginAt() { UserResponse u = getCurrentUser(); return u != null ? u.getLastLoginAt() : null; }

    public boolean isLoggedIn() {
        String token = getAccessToken();
        return token != null && !token.isEmpty();
    }

    /** True when the access token is missing or past (or within 30s of) its expiry. */
    public boolean isAccessTokenExpired() {
        long expiresAt = getExpiresAt();
        return expiresAt <= 0 || System.currentTimeMillis() >= (expiresAt - 30_000L);
    }
}
