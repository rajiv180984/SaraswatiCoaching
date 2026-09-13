package com.saraswati.institute.network;

import androidx.annotation.NonNull;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Attaches {@code Authorization: Bearer <accessToken>} to every request except the
 * public auth endpoints (which must go out unauthenticated).
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

        String path = original.url().encodedPath();
        if (isPublic(path)) {
            return chain.proceed(original);
        }

        String token = session.getAccessToken();
        if (token == null || token.isEmpty()) {
            return chain.proceed(original);
        }

        Request authed = original.newBuilder()
                .header("Authorization", "Bearer " + token)
                .build();
        return chain.proceed(authed);
    }

    private static boolean isPublic(String path) {
        return path.endsWith("/api/auth/login")
                || path.endsWith("/api/auth/register")
                || path.endsWith("/api/auth/refresh-token")
                || path.endsWith("/api/auth/forgot-password")
                || path.endsWith("/api/auth/reset-password");
    }
}
