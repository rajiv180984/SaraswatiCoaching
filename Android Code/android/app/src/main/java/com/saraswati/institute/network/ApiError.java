package com.saraswati.institute.network;

import android.util.Log;

import androidx.annotation.Nullable;

import com.google.gson.Gson;


import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Map;

import javax.net.ssl.SSLException;

import okhttp3.ResponseBody;
import retrofit2.Response;

/**
 * Turns a failed call into something the UI can show.
 *
 * <p>Two shapes of failure are handled: an HTTP error, whose body is the service's
 * {@link ErrorResponse}, and a transport failure (no connection, timeout, TLS handshake),
 * which never reaches the service at all. {@link #isTransport()} tells them apart —
 * a transport failure means the request may safely be retried, an HTTP error usually not.
 */
public final class ApiError {

    private static final String TAG = "ApiError";
    private static final Gson GSON = new Gson();

    /** Codes used by auth-service beyond the usual ones. */
    public static final int HTTP_LOCKED = 423;

    private final int status;
    private final String message;
    private final Map<String, String> fieldErrors;
    private final boolean transport;

    private ApiError(int status, String message, Map<String, String> fieldErrors, boolean transport) {
        this.status = status;
        this.message = message;
        this.fieldErrors = fieldErrors;
        this.transport = transport;
    }

    // ── Factories ─────────────────────────────────────────────────────────────

    /** Reads the {@link ErrorResponse} body of an unsuccessful response. */
    public static ApiError of(Response<?> response) {
        ErrorResponse parsed = parse(response.errorBody());
        String message = parsed != null && parsed.getMessage() != null && !parsed.getMessage().isEmpty()
                ? parsed.getMessage()
                : defaultMessageFor(response.code());
        Map<String, String> fields = parsed != null
                ? parsed.getFieldErrors()
                : Collections.<String, String>emptyMap();
        return new ApiError(response.code(), message, fields, false);
    }

    /** Describes a call that never completed (DNS, connect, timeout, TLS, malformed body). */
    public static ApiError of(@Nullable Throwable cause) {
        return new ApiError(0, transportMessageFor(cause), Collections.<String, String>emptyMap(), true);
    }

    @Nullable
    private static ErrorResponse parse(@Nullable ResponseBody body) {
        if (body == null) {
            return null;
        }
        try {
            return GSON.fromJson(body.charStream(), ErrorResponse.class);
        } catch (RuntimeException e) {
            Log.w(TAG, "Unparseable error body", e);
            return null;
        } finally {
            body.close();
        }
    }

    // ── Accessors ─────────────────────────────────────────────────────────────

    /** HTTP status, or {@code 0} when the request never reached the service. */
    public int status() {
        return status;
    }

    /** Message intended for the user: the server's own text when there is one. */
    public String message() {
        return message;
    }

    /** True when the call failed below HTTP — worth offering a retry. */
    public boolean isTransport() {
        return transport;
    }

    /** Per-field validation messages from a 400, keyed by request field name. */
    public Map<String, String> fieldErrors() {
        return fieldErrors;
    }

    @Nullable
    public String fieldError(String field) {
        return fieldErrors.get(field);
    }

    /** Bad credentials. */
    public boolean isUnauthorized() {
        return status == 401;
    }

    /** Account disabled, or email verification required before signing in. */
    public boolean isForbidden() {
        return status == 403;
    }

    /** Username or email already taken (registration). */
    public boolean isConflict() {
        return status == 409;
    }

    /** Too many failed attempts: locked for {@code app.security.lock-duration}. */
    public boolean isAccountLocked() {
        return status == HTTP_LOCKED;
    }

    /**
     * True when a 403 says the address still needs verifying, rather than the account being
     * disabled — the two cases are only distinguishable by the message.
     */
    public boolean isEmailNotVerified() {
        return status == 403 && message != null
                && message.toLowerCase(java.util.Locale.ROOT).contains("verif");
    }

    @Override
    public String toString() {
        return transport ? "ApiError(transport: " + message + ')' : "ApiError(" + status + ": " + message + ')';
    }

    // ── Message mapping ───────────────────────────────────────────────────────

    private static String defaultMessageFor(int status) {
        switch (status) {
            case 400:
                return "Some of the details are not valid. Please check and try again.";
            case 401:
                return "Invalid username/email or password.";
            case 403:
                return "This account is not allowed to sign in.";
            case 404:
                return "That resource no longer exists.";
            case 409:
                return "Those details are already registered.";
            case HTTP_LOCKED:
                return "Too many failed attempts. The account is locked for a short while.";
            case 429:
                return "Too many requests. Please wait a moment and try again.";
            default:
                return status >= 500
                        ? "The server could not complete the request. Please try again."
                        : "Request failed (HTTP " + status + ").";
        }
    }

    private static String transportMessageFor(@Nullable Throwable cause) {
        if (cause instanceof SSLException) {
            return "The secure connection to the server could not be established.";
        }
        if (cause instanceof SocketTimeoutException) {
            return "The server took too long to respond. Please try again.";
        }
        if (cause instanceof UnknownHostException || cause instanceof ConnectException) {
            return "Cannot reach the server. Check your internet connection.";
        }
        if (cause instanceof IOException) {
            return "Network error: " + cause.getMessage();
        }
        return cause != null && cause.getMessage() != null
                ? "Unexpected error: " + cause.getMessage()
                : "Unexpected error.";
    }
}
