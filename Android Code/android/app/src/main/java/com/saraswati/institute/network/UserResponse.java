package com.saraswati.institute.network;

import java.util.Collections;
import java.util.List;

/**
 * User profile as returned by auth-service ({@code AuthResponses.UserResponse}).
 *
 * <p>{@code id} is a MongoDB ObjectId hex string, not a number. {@code lastLoginAt} and
 * {@code createdAt} are ISO-8601 instants. The service has no phone field — the mobile
 * number collected during enrolment is held outside the auth account.
 */
public class UserResponse {

    private String id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String role;           // primary role: USER or ADMIN
    private List<String> roles;    // every role granted
    private boolean enabled;
    private boolean emailVerified;
    private boolean locked;
    private String lastLoginAt;
    private String createdAt;

    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getRole() {
        return role;
    }

    public List<String> getRoles() {
        return roles != null ? roles : Collections.<String>emptyList();
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }

    public boolean isLocked() {
        return locked;
    }

    public String getLastLoginAt() {
        return lastLoginAt;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(role) || getRoles().contains("ADMIN");
    }

    /** Full name, falling back to the username when neither name part is set. */
    public String getFullName() {
        StringBuilder sb = new StringBuilder();
        if (firstName != null && !firstName.isEmpty()) {
            sb.append(firstName);
        }
        if (lastName != null && !lastName.isEmpty()) {
            if (sb.length() > 0) {
                sb.append(' ');
            }
            sb.append(lastName);
        }
        return sb.length() > 0 ? sb.toString() : (username != null ? username : "");
    }
}
