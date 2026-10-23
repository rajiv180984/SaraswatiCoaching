package com.saraswati.institute.network;

/**
 * Generic acknowledgement from auth-service.
 *
 * <p>{@code debugToken} is only populated while the server runs with
 * {@code app.security.expose-tokens-in-response=true} (local testing without SMTP); in
 * production it is absent and the token only ever arrives by email.
 */
public class MessageResponse {

    private String message;
    private String debugToken;

    public String getMessage() {
        return message;
    }

    public String getDebugToken() {
        return debugToken;
    }

    public String messageOr(String fallback) {
        return message != null && !message.isEmpty() ? message : fallback;
    }
}
