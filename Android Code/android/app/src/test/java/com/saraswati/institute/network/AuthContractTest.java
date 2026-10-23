package com.saraswati.institute.network;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.google.gson.Gson;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

/**
 * Guards the wire contract with auth-service.
 *
 * <p>The JSON below is not hand-written: it was produced by serialising
 * {@code com.first.auth.dto.AuthResponses} with Jackson configured the way Spring Boot
 * configures it, so it is byte-for-byte what the service puts on the wire. The request
 * bodies are checked the other way round — the exact JSON Gson produces here was fed back
 * through the service's own {@code @JsonCreator} constructors with
 * {@code FAIL_ON_UNKNOWN_PROPERTIES} enabled.
 *
 * <p>If the service's DTOs are renamed or retyped, these assertions are what fails first,
 * rather than a null field at runtime.
 */
public class AuthContractTest {

    private final Gson gson = new Gson();

    private static final String LOGIN_RESPONSE = "{"
            + "\"accessToken\":\"eyJhbGciOiJIUzI1NiJ9.payload.sig\","
            + "\"refreshToken\":\"7f3c9a2b-opaque-refresh\","
            + "\"tokenType\":\"Bearer\","
            + "\"expiresIn\":900,"
            + "\"refreshExpiresIn\":604800,"
            + "\"role\":\"USER\","
            + "\"user\":{\"id\":\"68f1c2a4b9e1d20a3c4f5e6d\",\"username\":\"alice\","
            + "\"email\":\"alice@example.com\",\"firstName\":\"Alice\",\"lastName\":\"Wonder\","
            + "\"role\":\"USER\",\"roles\":[\"USER\"],\"enabled\":true,\"emailVerified\":true,"
            + "\"locked\":false,\"lastLoginAt\":\"2026-10-23T09:15:00Z\","
            + "\"createdAt\":\"2026-07-01T06:00:00Z\"}}";

    @Test
    public void parsesLoginResponse() {
        TokenResponse tokens = gson.fromJson(LOGIN_RESPONSE, TokenResponse.class);

        assertEquals("eyJhbGciOiJIUzI1NiJ9.payload.sig", tokens.getAccessToken());
        assertEquals("7f3c9a2b-opaque-refresh", tokens.getRefreshToken());
        assertEquals("Bearer", tokens.getTokenType());
        assertEquals("USER", tokens.getRole());
        assertTrue(tokens.hasTokens());
    }

    /** The service reports both TTLs in seconds; the session stores absolute millis. */
    @Test
    public void readsTokenLifetimesAsSeconds() {
        TokenResponse tokens = gson.fromJson(LOGIN_RESPONSE, TokenResponse.class);

        assertEquals(900L, tokens.getExpiresInSeconds());        // 15 minutes
        assertEquals(900_000L, tokens.getExpiresInMillis());
        assertEquals(604_800L, tokens.getRefreshExpiresInSeconds()); // 7 days
        assertEquals(604_800_000L, tokens.getRefreshExpiresInMillis());
    }

    @Test
    public void parsesEmbeddedUser() {
        UserResponse user = gson.fromJson(LOGIN_RESPONSE, TokenResponse.class).getUser();

        // A Mongo ObjectId hex string, not a number.
        assertEquals("68f1c2a4b9e1d20a3c4f5e6d", user.getId());
        assertEquals("alice", user.getUsername());
        assertEquals("alice@example.com", user.getEmail());
        assertEquals("Alice Wonder", user.getFullName());
        assertEquals("USER", user.getRole());
        assertEquals(Collections.singletonList("USER"), user.getRoles());
        assertTrue(user.isEnabled());
        assertTrue(user.isEmailVerified());
        assertFalse(user.isLocked());
        assertFalse(user.isAdmin());
        assertEquals("2026-10-23T09:15:00Z", user.getLastLoginAt());
        assertEquals("2026-07-01T06:00:00Z", user.getCreatedAt());
    }

    @Test
    public void recognisesAnAdminFromEitherField() {
        UserResponse viaRole = gson.fromJson("{\"role\":\"ADMIN\"}", UserResponse.class);
        UserResponse viaRoles = gson.fromJson("{\"roles\":[\"ADMIN\",\"USER\"]}", UserResponse.class);

        assertTrue(viaRole.isAdmin());
        assertTrue(viaRoles.isAdmin());
    }

    @Test
    public void fallsBackToTheUsernameWhenNoNameIsSet() {
        UserResponse user = gson.fromJson("{\"username\":\"alice\"}", UserResponse.class);

        assertEquals("alice", user.getFullName());
    }

    /** Registration answers with the profile and a message — never with tokens. */
    @Test
    public void parsesRegistrationResponse() {
        String json = "{\"message\":\"User registered. Please verify your email address.\","
                + "\"user\":{\"id\":\"68f1c2a4b9e1d20a3c4f5e6d\",\"username\":\"alice\","
                + "\"email\":\"alice@example.com\",\"role\":\"USER\",\"roles\":[\"USER\"],"
                + "\"enabled\":true,\"emailVerified\":false,\"locked\":false},"
                + "\"debugVerificationToken\":\"verify-token-123\"}";

        RegistrationResponse registration = gson.fromJson(json, RegistrationResponse.class);

        assertEquals("User registered. Please verify your email address.", registration.getMessage());
        assertEquals("alice", registration.getUser().getUsername());
        assertFalse(registration.getUser().isEmailVerified());
        assertEquals("verify-token-123", registration.getDebugVerificationToken());
    }

    @Test
    public void parsesMessageResponse() {
        String json = "{\"message\":\"If an account exists, a link was sent.\","
                + "\"debugToken\":\"reset-tok\"}";

        MessageResponse message = gson.fromJson(json, MessageResponse.class);

        assertEquals("If an account exists, a link was sent.", message.getMessage());
        assertEquals("reset-tok", message.getDebugToken());
    }

    /** In production the service omits debugToken entirely. */
    @Test
    public void toleratesAMessageWithoutADebugToken() {
        MessageResponse message = gson.fromJson("{\"message\":\"Logged out\"}", MessageResponse.class);

        assertEquals("Logged out", message.getMessage());
        assertNull(message.getDebugToken());
        assertEquals("Logged out", message.messageOr("fallback"));
    }

    @Test
    public void parsesTokenValidationResponse() {
        String json = "{\"valid\":true,\"userId\":\"68f1c2a4b9e1d20a3c4f5e6d\",\"username\":\"alice\","
                + "\"role\":\"ADMIN\",\"roles\":[\"ADMIN\",\"USER\"],"
                + "\"issuedAt\":\"2026-10-23T09:15:00Z\",\"expiresAt\":\"2026-10-23T09:30:00Z\"}";

        TokenValidationResponse validation = gson.fromJson(json, TokenValidationResponse.class);

        assertTrue(validation.isValid());
        assertEquals("68f1c2a4b9e1d20a3c4f5e6d", validation.getUserId());
        assertEquals("ADMIN", validation.getRole());
        assertEquals(Arrays.asList("ADMIN", "USER"), validation.getRoles());
        assertEquals("2026-10-23T09:30:00Z", validation.getExpiresAt());
        assertNull(validation.getError());
    }

    @Test
    public void parsesValidationFailure() {
        TokenValidationResponse validation =
                gson.fromJson("{\"valid\":false,\"error\":\"Token has expired\"}",
                        TokenValidationResponse.class);

        assertFalse(validation.isValid());
        assertEquals("Token has expired", validation.getError());
        assertTrue(validation.getRoles().isEmpty());
    }

    @Test
    public void parsesValidationErrorWithFieldMessages() {
        String json = "{\"timestamp\":\"2026-10-23T09:15:00Z\",\"status\":400,"
                + "\"error\":\"Bad Request\",\"message\":\"Validation failed\","
                + "\"path\":\"/api/v1/auth/register\","
                + "\"fieldErrors\":{\"password\":\"Password must be 8-64 characters\"}}";

        ErrorResponse error = gson.fromJson(json, ErrorResponse.class);

        assertEquals(400, error.getStatus());
        assertEquals("Bad Request", error.getError());
        assertEquals("Validation failed", error.getMessage());
        assertEquals("/api/v1/auth/register", error.getPath());
        assertEquals("Password must be 8-64 characters", error.getFieldErrors().get("password"));
    }

    @Test
    public void errorWithoutFieldErrorsYieldsAnEmptyMap() {
        ErrorResponse error = gson.fromJson(
                "{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"Invalid username/email or password\"}",
                ErrorResponse.class);

        assertTrue(error.getFieldErrors().isEmpty());
    }

    // ── Request bodies ────────────────────────────────────────────────────────

    /** The service authenticates on usernameOrEmail, not on an "email" field. */
    @Test
    public void serialisesLoginRequest() {
        assertEquals("{\"usernameOrEmail\":\"alice@example.com\",\"password\":\"Alice@12345\"}",
                gson.toJson(new AuthRequests.Login("alice@example.com", "Alice@12345")));
    }

    @Test
    public void serialisesRegisterRequest() {
        assertEquals("{\"username\":\"alice\",\"email\":\"alice@example.com\","
                        + "\"password\":\"Alice@12345\",\"firstName\":\"Alice\",\"lastName\":\"Wonder\"}",
                gson.toJson(new AuthRequests.Register(
                        "alice", "alice@example.com", "Alice@12345", "Alice", "Wonder")));
    }

    /** lastName is optional server-side, so a one-word name simply omits it. */
    @Test
    public void omitsAnAbsentLastName() {
        assertEquals("{\"username\":\"bob\",\"email\":\"bob@example.com\","
                        + "\"password\":\"Bob@123456\",\"firstName\":\"Bob\"}",
                gson.toJson(new AuthRequests.Register(
                        "bob", "bob@example.com", "Bob@123456", "Bob", null)));
    }

    @Test
    public void serialisesTheRemainingRequestBodies() {
        assertEquals("{\"refreshToken\":\"opaque-refresh\"}",
                gson.toJson(new AuthRequests.RefreshToken("opaque-refresh")));
        assertEquals("{\"email\":\"alice@example.com\"}",
                gson.toJson(new AuthRequests.Email("alice@example.com")));
        assertEquals("{\"token\":\"reset-tok\",\"newPassword\":\"NewAlice@12345\"}",
                gson.toJson(new AuthRequests.ResetPassword("reset-tok", "NewAlice@12345")));
        assertEquals("{\"currentPassword\":\"Alice@12345\",\"newPassword\":\"NewAlice@12345\"}",
                gson.toJson(new AuthRequests.ChangePassword("Alice@12345", "NewAlice@12345")));
        assertEquals("{\"firstName\":\"Alice\",\"lastName\":\"Wonder\"}",
                gson.toJson(new AuthRequests.UpdateProfile("Alice", "Wonder")));
        assertEquals("{\"token\":\"jwt\"}",
                gson.toJson(new AuthRequests.ValidateToken("jwt")));
    }

    // ── Policy parity ─────────────────────────────────────────────────────────

    /** Same rule as the service's PasswordPolicy, so the UI rejects what the server would. */
    @Test
    public void passwordPolicyMatchesTheService() {
        assertTrue("Alice@12345".matches(ApiConfig.PASSWORD_REGEX));
        assertFalse("alice@12345".matches(ApiConfig.PASSWORD_REGEX));   // no upper case
        assertFalse("Alice12345".matches(ApiConfig.PASSWORD_REGEX));    // no special character
        assertFalse("Alice@1".matches(ApiConfig.PASSWORD_REGEX));       // too short
        assertFalse("Alice @12345".matches(ApiConfig.PASSWORD_REGEX));  // whitespace
    }

    @Test
    public void usernamePolicyMatchesTheService() {
        assertTrue("alice".matches(ApiConfig.USERNAME_REGEX));
        assertTrue("alice.w_1-2".matches(ApiConfig.USERNAME_REGEX));
        assertFalse("al".matches(ApiConfig.USERNAME_REGEX));            // under 3 characters
        assertFalse("alice@example.com".matches(ApiConfig.USERNAME_REGEX));
    }

    /** Every authenticated path must sit outside the public prefix the interceptor skips. */
    @Test
    public void authPrefixCoversOnlyThePublicEndpoints() {
        assertTrue("/api/v1/auth/login".startsWith(ApiConfig.AUTH_PREFIX));
        assertTrue("/api/v1/auth/refresh-token".startsWith(ApiConfig.AUTH_PREFIX));
        assertFalse("/api/v1/users/me".startsWith(ApiConfig.AUTH_PREFIX));
        assertFalse("/api/v1/users/me/change-password".startsWith(ApiConfig.AUTH_PREFIX));
    }
}
