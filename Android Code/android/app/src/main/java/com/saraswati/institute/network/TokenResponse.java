package com.saraswati.institute.network;

import java.util.concurrent.TimeUnit;

/**
 * Body of a successful {@code login} or {@code refresh-token} call.
 *
 * <p>The service returns the DTO at the top level — there is no {@code success}/{@code data}
 * envelope. {@code expiresIn} and {@code refreshExpiresIn} are <b>seconds</b>, matching
 * {@code JwtService.getAccessTokenTtlSeconds()} / {@code RefreshTokenService.getTtlSeconds()}.
 */
public class TokenResponse {

    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private long expiresIn;          // seconds
    private long refreshExpiresIn;   // seconds
    private String role;             // primary role: USER or ADMIN
    private UserResponse user;

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public String getTokenType() {
        return tokenType != null ? tokenType : "Bearer";
    }

    public long getExpiresInSeconds() {
        return expiresIn;
    }

    public long getRefreshExpiresInSeconds() {
        return refreshExpiresIn;
    }

    public long getExpiresInMillis() {
        return TimeUnit.SECONDS.toMillis(expiresIn);
    }

    public long getRefreshExpiresInMillis() {
        return TimeUnit.SECONDS.toMillis(refreshExpiresIn);
    }

    /** Primary role from the token response, falling back to the embedded user profile. */
    public String getRole() {
        if (role != null && !role.isEmpty()) {
            return role;
        }
        return user != null ? user.getRole() : null;
    }

    public UserResponse getUser() {
        return user;
    }

    public boolean hasTokens() {
        return accessToken != null && !accessToken.isEmpty()
                && refreshToken != null && !refreshToken.isEmpty();
    }
}
