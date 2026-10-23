package com.saraswati.institute.network;

/**
 * Body of {@code POST /api/v1/auth/register} (HTTP 201).
 *
 * <p>Registration does <b>not</b> return tokens — the account is created and a verification
 * email is sent, so the app must log in separately afterwards.
 * {@code debugVerificationToken} is only present while the server exposes tokens in
 * responses for local testing.
 */
public class RegistrationResponse {

    private String message;
    private UserResponse user;
    private String debugVerificationToken;

    public String getMessage() {
        return message;
    }

    public UserResponse getUser() {
        return user;
    }

    public String getDebugVerificationToken() {
        return debugVerificationToken;
    }
}
