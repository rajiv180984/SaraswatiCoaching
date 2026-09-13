package com.saraswati.institute.network;

/**
 * Body for {@code POST /api/auth/register} (saraswati-auth).
 *
 * Server-side validation:
 *   username  — 3..50 chars
 *   email     — valid email
 *   password  — min 8 chars, must contain upper + lower + digit + special (@$!%*?&)
 *   firstName — required, max 50
 *   lastName  — required, max 50
 *   phone     — optional, ^[+]?[0-9]{10,15}$
 */
public class RegisterRequest {

    private final String username;
    private final String email;
    private final String password;
    private final String firstName;
    private final String lastName;
    private final String phone;

    public RegisterRequest(String username, String email, String password,
                           String firstName, String lastName, String phone) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
    }
}
