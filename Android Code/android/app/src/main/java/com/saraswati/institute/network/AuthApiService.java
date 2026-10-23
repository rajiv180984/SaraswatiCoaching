package com.saraswati.institute.network;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Query;

/**
 * Retrofit binding for auth-service ({@code https://3.108.167.166:8443/}).
 *
 * <p>Every endpoint returns its DTO at the top level — there is no success/data envelope.
 * Failures carry an {@link ErrorResponse} body; read it with {@link ApiError#of}.
 *
 * <p>The endpoints under {@code /api/v1/auth/} are public. Those under
 * {@code /api/v1/users/me} need a bearer access token, which {@link AuthInterceptor}
 * attaches and {@link TokenAuthenticator} refreshes on a 401.
 */
public interface AuthApiService {

    // ── Public: /api/v1/auth ──────────────────────────────────────────────────

    /** 201 on success. Returns the created profile, <b>not</b> tokens — log in afterwards. */
    @POST("api/v1/auth/register")
    Call<RegistrationResponse> register(@Body AuthRequests.Register request);

    @POST("api/v1/auth/login")
    Call<TokenResponse> login(@Body AuthRequests.Login request);

    /** Rotating refresh tokens: the presented token is invalidated and must be replaced. */
    @POST("api/v1/auth/refresh-token")
    Call<TokenResponse> refreshToken(@Body AuthRequests.RefreshToken request);

    /** Revokes one refresh token, ending the current session only. */
    @POST("api/v1/auth/logout")
    Call<MessageResponse> logout(@Body AuthRequests.RefreshToken request);

    /** Always 200, whether or not the address is registered (no account enumeration). */
    @POST("api/v1/auth/forgot-password")
    Call<MessageResponse> forgotPassword(@Body AuthRequests.Email request);

    /** Signs the user out of every session on success. */
    @POST("api/v1/auth/reset-password")
    Call<MessageResponse> resetPassword(@Body AuthRequests.ResetPassword request);

    @GET("api/v1/auth/verify-email")
    Call<MessageResponse> verifyEmail(@Query("token") String token);

    @POST("api/v1/auth/resend-verification")
    Call<MessageResponse> resendVerification(@Body AuthRequests.Email request);

    @POST("api/v1/auth/validate-token")
    Call<TokenValidationResponse> validateToken(@Body AuthRequests.ValidateToken request);

    // ── Authenticated: /api/v1/users/me ───────────────────────────────────────

    @GET("api/v1/users/me")
    Call<UserResponse> getMe();

    @PUT("api/v1/users/me")
    Call<UserResponse> updateProfile(@Body AuthRequests.UpdateProfile request);

    /** Revokes every session on success, so the app must sign in again. */
    @POST("api/v1/users/me/change-password")
    Call<MessageResponse> changePassword(@Body AuthRequests.ChangePassword request);

    @POST("api/v1/users/me/logout-all")
    Call<MessageResponse> logoutAll();
}
