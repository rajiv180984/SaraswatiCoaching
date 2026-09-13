package com.saraswati.auth.service;

import com.saraswati.auth.dto.request.UpdateProfileRequest;
import com.saraswati.auth.dto.response.UserResponse;
import com.saraswati.auth.entity.User;
import com.saraswati.auth.exception.BadRequestException;
import com.saraswati.auth.exception.ResourceNotFoundException;
import com.saraswati.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RefreshTokenService refreshTokenService;

    public UserResponse getCurrentUser(String email) {
        return toUserResponse(findByEmail(email));
    }

    @Transactional
    public UserResponse updateProfile(String email, UpdateProfileRequest request) {
        User user = findByEmail(email);

        if (StringUtils.hasText(request.getUsername())
                && !request.getUsername().equals(user.getUsername())) {
            if (userRepository.existsByUsername(request.getUsername())) {
                throw new BadRequestException("Username already taken");
            }
            user.setUsername(request.getUsername());
        }
        if (StringUtils.hasText(request.getFirstName())) user.setFirstName(request.getFirstName());
        if (StringUtils.hasText(request.getLastName()))  user.setLastName(request.getLastName());
        if (StringUtils.hasText(request.getPhone()))     user.setPhone(request.getPhone());

        return toUserResponse(userRepository.save(user));
    }

    @Transactional
    public void deleteCurrentUser(String email) {
        User user = findByEmail(email);
        refreshTokenService.deleteByUser(user);
        userRepository.delete(user);
    }

    // ── Admin ──────────────────────────────────────────────────────────────

    public Page<UserResponse> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(this::toUserResponse);
    }

    public UserResponse getUserById(Long id) {
        return toUserResponse(findById(id));
    }

    @Transactional
    public void deleteUserById(Long id) {
        User user = findById(id);
        refreshTokenService.deleteByUser(user);
        userRepository.delete(user);
    }

    @Transactional
    public UserResponse toggleUserStatus(Long id) {
        User user = findById(id);
        user.setEnabled(!user.isEnabled());
        if (!user.isEnabled()) {
            refreshTokenService.deleteByUser(user);
        }
        return toUserResponse(userRepository.save(user));
    }

    // ── Helpers ────────────────────────────────────────────────────────────

    private User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    private UserResponse toUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phone(user.getPhone())
                .role(user.getRole())
                .enabled(user.isEnabled())
                .createdAt(user.getCreatedAt())
                .lastLoginAt(user.getLastLoginAt())
                .build();
    }
}
