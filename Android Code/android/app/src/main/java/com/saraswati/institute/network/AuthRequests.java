package com.saraswati.institute.network;

/**
 * Request bodies accepted by auth-service, mirroring {@code com.first.auth.dto.AuthRequests}.
 * Field names are the JSON property names, so they must not be renamed without the server.
 */
public final class AuthRequests {

    private AuthRequests() { }

    /** {@code POST /api/v1/auth/register}. The server derives the role; it is never client-supplied. */
    public static final class Register {

        private final String username;
        private final String email;
        private final String password;
        private final String firstName;
        private final String lastName;

        public Register(String username, String email, String password,
                        String firstName, String lastName) {
            this.username = username;
            this.email = email;
            this.password = password;
            this.firstName = firstName;
            this.lastName = lastName;
        }
    }

    /** {@code POST /api/v1/auth/login}. The identifier may be either the username or the email. */
    public static final class Login {

        private final String usernameOrEmail;
        private final String password;

        public Login(String usernameOrEmail, String password) {
            this.usernameOrEmail = usernameOrEmail;
            this.password = password;
        }
    }

    /** {@code POST /api/v1/auth/refresh-token} and {@code POST /api/v1/auth/logout}. */
    public static final class RefreshToken {

        private final String refreshToken;

        public RefreshToken(String refreshToken) {
            this.refreshToken = refreshToken;
        }
    }

    /** {@code POST /api/v1/auth/forgot-password} and {@code .../resend-verification}. */
    public static final class Email {

        private final String email;

        public Email(String email) {
            this.email = email;
        }
    }

    /** {@code POST /api/v1/auth/reset-password} — the token comes from the reset email. */
    public static final class ResetPassword {

        private final String token;
        private final String newPassword;

        public ResetPassword(String token, String newPassword) {
            this.token = token;
            this.newPassword = newPassword;
        }
    }

    /** {@code POST /api/v1/users/me/change-password} — revokes every session on success. */
    public static final class ChangePassword {

        private final String currentPassword;
        private final String newPassword;

        public ChangePassword(String currentPassword, String newPassword) {
            this.currentPassword = currentPassword;
            this.newPassword = newPassword;
        }
    }

    /** {@code PUT /api/v1/users/me}. */
    public static final class UpdateProfile {

        private final String firstName;
        private final String lastName;

        public UpdateProfile(String firstName, String lastName) {
            this.firstName = firstName;
            this.lastName = lastName;
        }
    }

    /** {@code POST /api/v1/auth/validate-token} — access-token introspection. */
    public static final class ValidateToken {

        private final String token;

        public ValidateToken(String token) {
            this.token = token;
        }
    }
}
