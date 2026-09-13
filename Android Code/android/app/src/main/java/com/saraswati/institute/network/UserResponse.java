package com.saraswati.institute.network;

/** Authenticated user profile as returned by saraswati-auth. */
public class UserResponse {

    private Long id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private String role;
    private boolean enabled;
    private String createdAt;
    private String lastLoginAt;

    public Long    getId()          { return id; }
    public String  getUsername()    { return username; }
    public String  getEmail()       { return email; }
    public String  getFirstName()   { return firstName; }
    public String  getLastName()    { return lastName; }
    public String  getPhone()       { return phone; }
    public String  getRole()        { return role; }
    public boolean isEnabled()      { return enabled; }
    public String  getCreatedAt()   { return createdAt; }
    public String  getLastLoginAt() { return lastLoginAt; }

    public String getFullName() {
        StringBuilder sb = new StringBuilder();
        if (firstName != null) sb.append(firstName);
        if (lastName != null && !lastName.isEmpty()) {
            if (sb.length() > 0) sb.append(' ');
            sb.append(lastName);
        }
        return sb.toString();
    }
}
