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
 * Renews the access token when an authenticated call comes back {@code 401}.
 *
 * <p>Access tokens live 15 minutes ({@code app.jwt.access-token-ttl}); refresh tokens 7 days
 * and <b>rotate</b> — each exchange revokes the presented token and issues a new pair, and
 * presenting an already-exchanged token is treated as theft and revokes every session for
 * that user. Two consequences shape this class:
 *
 * <ul>
 *   <li>the whole method is {@code synchronized} and re-checks the stored token first, so
 *       concurrent 401s result in exactly one exchange rather than a second one that would
 *       look like reuse;</li>
 *   <li>the new refresh token is persisted before the request is replayed.</li>
 * </ul>
 *
 * <p>A transport failure leaves the session alone — the refresh token is still good and the
 * next call can try again. An HTTP rejection does not: the session is cleared and
 * {@link ApiClient#notifySessionExpired()} asks the UI to return to the login screen.
 */
public class TokenAuthenticator implements Authenticator {

    private static final String TAG = "TokenAuthenticator";

    private static final String REFRESH_PATH = ApiConfig.AUTH_PREFIX + "refresh-token";

    /** Lazy accessor so the fully built service can be reused without a construction cycle. */
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
        // A 401 from the refresh endpoint itself means the refresh token is gone; never recurse.
        if (response.request().url().encodedPath().endsWith(REFRESH_PATH)) {
            return null;
        }
        if (responseCount(response) >= 2) {
            return null; // already retried once with a fresh token - the token is not the problem
        }

        String attemptedToken = bearerOf(response.request());
        String storedToken = session.getAccessToken();
        if (storedToken != null && !storedToken.isEmpty() && !storedToken.equals(attemptedToken)) {
            // Another thread refreshed while this one waited on the lock; just use its token.
            return retryWith(response.request(), storedToken);
        }

        String refreshToken = session.getRefreshToken();
        if (refreshToken == null || refreshToken.isEmpty()) {
            return null;
        }

        retrofit2.Response<TokenResponse> refreshed;
        try {
            Call<TokenResponse> call =
                    serviceProvider.get().refreshToken(new AuthRequests.RefreshToken(refreshToken));
            refreshed = call.execute();
        } catch (IOException e) {
            // Could not reach the service: the refresh token is probably still valid.
            Log.w(TAG, "Token refresh could not be completed", e);
            return null;
        }

        TokenResponse tokens = refreshed.body();
        if (refreshed.isSuccessful() && tokens != null && tokens.hasTokens()) {
            session.saveSession(tokens);
            return retryWith(response.request(), tokens.getAccessToken());
        }

        Log.w(TAG, "Refresh token rejected (HTTP " + refreshed.code() + "); signing out");
        session.clear();
        ApiClient.notifySessionExpired();
        return null;
    }

    private Request retryWith(Request request, String accessToken) {
        return request.newBuilder()
                .header("Authorization", session.getTokenType() + " " + accessToken)
                .build();
    }

    @Nullable
    private static String bearerOf(Request request) {
        String header = request.header("Authorization");
        if (header == null) {
            return null;
        }
        int space = header.indexOf(' ');
        return space > 0 ? header.substring(space + 1) : header;
    }

    private static int responseCount(Response response) {
        int count = 1;
        while ((response = response.priorResponse()) != null) {
            count++;
        }
        return count;
    }
}
