package com.saraswati.institute.network;

/** Body for {@code POST /api/auth/login}. */
public class LoginRequest {

    private final String email;
    private final String password;

    public LoginRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }
}
