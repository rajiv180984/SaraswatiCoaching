package com.saraswati.institute.network;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

/**
 * Retrofit binding for the saraswati-auth REST API (https://localhost:8443/).
 *
 * Public endpoints (no token): register, login, refresh-token, forgot-password.
 * Authenticated endpoints rely on {@link AuthInterceptor} to attach the bearer token.
 */
public interface AuthApiService {

    @POST("api/auth/register")
    Call<ApiResponse<AuthResponse>> register(@Body RegisterRequest request);

    @POST("api/auth/login")
    Call<ApiResponse<AuthResponse>> login(@Body LoginRequest request);

    @POST("api/auth/refresh-token")
    Call<ApiResponse<AuthResponse>> refreshToken(@Body RefreshTokenRequest request);

    @POST("api/auth/forgot-password")
    Call<ApiResponse<Void>> forgotPassword(@Body ForgotPasswordRequest request);

    @POST("api/auth/logout")
    Call<ApiResponse<Void>> logout();

    @GET("api/users/me")
    Call<ApiResponse<UserResponse>> getMe();
}
