package com.saraswati.institute.network;

/**
 * Coordinates of the auth-service deployment this build talks to.
 *
 * <p>The service is the Spring Boot {@code auth-service} (MongoDB flavour): HTTPS only,
 * versioned under {@code /api/v1}, bearer-token authentication, opaque rotating refresh
 * tokens. Swagger lives at {@code /swagger-ui/index.html} on the same host.
 */
public final class ApiConfig {

    /** Host part of {@link #BASE_URL}, used for certificate/hostname handling in {@link TlsConfig}. */
    public static final String HOST = "3.108.167.166";

    public static final int PORT = 8443;

    /** Trailing slash is required by Retrofit. */
    public static final String BASE_URL = "https://" + HOST + ":" + PORT + "/";

    /** Every endpoint of the service is versioned; public auth endpoints sit under this prefix. */
    public static final String AUTH_PREFIX = "/api/v1/auth/";

    /**
     * Server-side password rule ({@code PasswordPolicy.REGEX}): 8-64 characters, no whitespace,
     * at least one lower case, one upper case, one digit and one non-alphanumeric character.
     * Mirrored here so the UI can reject bad passwords before a round trip.
     */
    public static final String PASSWORD_REGEX =
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9])\\S{8,64}$";

    /** Server-side username rule: 3-50 chars of letters, digits, {@code .}, {@code _} and {@code -}. */
    public static final String USERNAME_REGEX = "^[A-Za-z0-9._-]{3,50}$";

    private ApiConfig() { }
}
