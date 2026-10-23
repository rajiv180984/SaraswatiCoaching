package com.saraswati.institute.network;

import java.util.Collections;
import java.util.List;

/**
 * Body of {@code POST /api/v1/auth/validate-token}: introspection of an access token.
 *
 * <p>The endpoint answers 200 whether or not the token is valid — {@link #isValid()} carries
 * the verdict and {@link #getError()} the reason when it is {@code false}.
 */
public class TokenValidationResponse {

    private boolean valid;
    private String userId;
    private String username;
    private String role;
    private List<String> roles;
    private String issuedAt;
    private String expiresAt;
    private String error;

    public boolean isValid() {
        return valid;
    }

    public String getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getRole() {
        return role;
    }

    public List<String> getRoles() {
        return roles != null ? roles : Collections.<String>emptyList();
    }

    public String getIssuedAt() {
        return issuedAt;
    }

    public String getExpiresAt() {
        return expiresAt;
    }

    public String getError() {
        return error;
    }
}
