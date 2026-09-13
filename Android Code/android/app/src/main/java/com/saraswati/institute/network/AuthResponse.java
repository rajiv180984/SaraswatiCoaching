package com.saraswati.institute.network;

/** Payload of a successful register / login / refresh-token call. */
public class AuthResponse {

    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private long   expiresIn;      // milliseconds
    private UserResponse user;

    public String getAccessToken()  { return accessToken; }
    public String getRefreshToken() { return refreshToken; }
    public String getTokenType()    { return tokenType != null ? tokenType : "Bearer"; }
    public long   getExpiresIn()    { return expiresIn; }
    public UserResponse getUser()   { return user; }
}
