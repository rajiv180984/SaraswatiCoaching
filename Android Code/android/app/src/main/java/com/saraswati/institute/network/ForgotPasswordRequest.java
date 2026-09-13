package com.saraswati.institute.network;

/** Body for {@code POST /api/auth/forgot-password}. */
public class ForgotPasswordRequest {

    private final String email;

    public ForgotPasswordRequest(String email) {
        this.email = email;
    }
}
