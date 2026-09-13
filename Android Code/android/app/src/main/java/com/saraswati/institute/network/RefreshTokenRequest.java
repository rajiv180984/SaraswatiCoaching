package com.saraswati.institute.network;

/** Body for {@code POST /api/auth/refresh-token}. */
public class RefreshTokenRequest {

    private final String refreshToken;

    public RefreshTokenRequest(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
