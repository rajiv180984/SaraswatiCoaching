package com.saraswati.auth.controller;

import com.saraswati.auth.dto.request.UpdateProfileRequest;
import com.saraswati.auth.dto.response.ApiResponse;
import com.saraswati.auth.dto.response.UserResponse;
import com.saraswati.auth.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Users", description = "User profile and admin management")
public class UserController {

    private final UserService userService;

    // ── Current user ───────────────────────────────────────────────────────

    @GetMapping("/api/users/me")
    @Operation(summary = "Get my profile")
    public ResponseEntity<ApiResponse<UserResponse>> getMe(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success("Profile fetched",
                userService.getCurrentUser(userDetails.getUsername())));
    }

    @PutMapping("/api/users/me")
    @Operation(summary = "Update my profile")
    public ResponseEntity<ApiResponse<UserResponse>> updateMe(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Profile updated",
                userService.updateProfile(userDetails.getUsername(), request)));
    }

    @DeleteMapping("/api/users/me")
    @Operation(summary = "Delete my account")
    public ResponseEntity<ApiResponse<Void>> deleteMe(
            @AuthenticationPrincipal UserDetails userDetails) {
        userService.deleteCurrentUser(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Account deleted successfully"));
    }

    // ── Admin ──────────────────────────────────────────────────────────────

    @GetMapping("/api/admin/users")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all users with pagination (Admin)")
    public ResponseEntity<ApiResponse<Page<UserResponse>>> getAllUsers(
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success("Users fetched",
                userService.getAllUsers(pageable)));
    }

    @GetMapping("/api/admin/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get user by ID (Admin)")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("User fetched", userService.getUserById(id)));
    }

    @DeleteMapping("/api/admin/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete user by ID (Admin)")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        userService.deleteUserById(id);
        return ResponseEntity.ok(ApiResponse.success("User deleted"));
    }

    @PatchMapping("/api/admin/users/{id}/toggle-status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Enable or disable a user (Admin)")
    public ResponseEntity<ApiResponse<UserResponse>> toggleStatus(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Status updated",
                userService.toggleUserStatus(id)));
    }
}
