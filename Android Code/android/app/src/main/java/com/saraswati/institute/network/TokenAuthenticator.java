package com.saraswati.institute.network;

import android.util.Log;

import androidx.annotation.Nullable;

import java.io.IOException;

import okhttp3.Authenticator;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;
import retrofit2.Call;

/**
 * When any authenticated call comes back {@code 401}, this tries once to mint a fresh
 * access token from the stored refresh token ({@code POST /api/auth/refresh-token}) and
 * replays the original request with the new token. If the refresh itself fails, the
 * session is cleared and the request is left to fail (the UI should route back to login).
 */
public class TokenAuthenticator implements Authenticator {

    private static final String TAG = "TokenAuthenticator";

    /** Lazy accessor so we can reuse the fully-built service without a construction cycle. */
    public interface ServiceProvider {
        AuthApiService get();
    }

    private final SessionManager session;
    private final ServiceProvider serviceProvider;

    public TokenAuthenticator(SessionManager session, ServiceProvider serviceProvider) {
        this.session = session;
        this.serviceProvider = serviceProvider;
    }

    @Nullable
    @Override
    public synchronized Request authenticate(@Nullable Route route, Response response) {
        if (responseCount(response) >= 2) {
            return null; // already retried once — give up
        }

        String currentAccess = session.getAccessToken();
        String requestToken = tokenOf(response.request());
        // Another thread may have refreshed already while we waited on the lock.
        if (currentAccess != null && requestToken != null && !currentAccess.equals(requestToken)) {
            return response.request().newBuilder()
                    .header("Authorization", "Bearer " + currentAccess)
                    .build();
        }

        String refreshToken = session.getRefreshToken();
        if (refreshToken == null || refreshToken.isEmpty()) {
            return null;
        }

        try {
            Call<ApiResponse<AuthResponse>> call =
                    serviceProvider.get().refreshToken(new RefreshTokenRequest(refreshToken));
            retrofit2.Response<ApiResponse<AuthResponse>> refreshResponse = call.execute();

            if (refreshResponse.isSuccessful()
                    && refreshResponse.body() != null
                    && refreshResponse.body().getData() != null) {
                AuthResponse fresh = refreshResponse.body().getData();
                session.updateTokens(fresh.getAccessToken(), fresh.getRefreshToken(), fresh.getExpiresIn());
                return response.request().newBuilder()
                        .header("Authorization", "Bearer " + fresh.getAccessToken())
                        .build();
            }
            Log.w(TAG, "Refresh token rejected (HTTP " + refreshResponse.code() + "); clearing session");
        } catch (IOException e) {
            Log.w(TAG, "Refresh token call failed", e);
            return null; // transient network error — don't nuke the session
        }

        session.clear();
        return null;
    }

    @Nullable
    private static String tokenOf(Request request) {
        String header = request.header("Authorization");
        return (header != null && header.startsWith("Bearer ")) ? header.substring(7) : null;
    }

    private static int responseCount(Response response) {
        int count = 1;
        while ((response = response.priorResponse()) != null) {
            count++;
        }
        return count;
    }
}
