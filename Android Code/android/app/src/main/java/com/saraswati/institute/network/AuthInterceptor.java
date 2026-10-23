package com.saraswati.institute.network;

import androidx.annotation.NonNull;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Adds {@code Accept: application/json} to every request and
 * {@code Authorization: <tokenType> <accessToken>} to the authenticated ones.
 *
 * <p>Everything under {@code /api/v1/auth/} is public and must go out unauthenticated:
 * sending a stale bearer token to {@code refresh-token} or {@code login} would only invite
 * a 401 from the filter chain before the endpoint is reached.
 */
public class AuthInterceptor implements Interceptor {

    private final SessionManager session;

    public AuthInterceptor(SessionManager session) {
        this.session = session;
    }

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request original = chain.request();
        Request.Builder builder = original.newBuilder()
                .header("Accept", "application/json");

        if (!isPublic(original.url().encodedPath())) {
            String token = session.getAccessToken();
            if (token != null && !token.isEmpty()) {
                builder.header("Authorization", session.getTokenType() + " " + token);
            }
        }
        return chain.proceed(builder.build());
    }

    /** Every endpoint the service exposes without authentication lives under one prefix. */
    private static boolean isPublic(String encodedPath) {
        return encodedPath.startsWith(ApiConfig.AUTH_PREFIX);
    }
}
