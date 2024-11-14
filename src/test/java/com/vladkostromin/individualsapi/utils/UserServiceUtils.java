package com.vladkostromin.individualsapi.utils;

import com.vladkostromin.individualsapi.model.*;

import java.time.LocalDateTime;
import java.util.Collections;

public class UserServiceUtils {

    public static TokenResponse getTokenResponse() {
        return TokenResponse.builder()
                .accessToken("test access token")
                .refreshToken("test refresh token")
                .expiresIn(1730953787)
                .tokenType("Bearer")
                .build();
    }

    public static UserResponse getUser() {
        return UserResponse.builder()
                .id("9e367e7c-52eb-477d-ab99-ad3d727062b8")
                .roles(Collections.emptyList())
                .email("testuser@mail.com")
                .createdAt(LocalDateTime.now())
                .build();
    }

    public static AuthRequest getAuthRequest() {
        return AuthRequest.builder()
                .email("testuser@mail.com")
                .password("testpassword")
                .build();
    }
    public static UserRegistrationRequest getUserRegistrationRequest() {
        return UserRegistrationRequest.builder()
                .email("testuser@mail.com")
                .password("testpassword")
                .confirmPassword("testpassword")
                .build();
    }

    public static RefreshTokenRequest getRefreshTokenRequest() {
        return RefreshTokenRequest.builder()
                .refreshToken("test refresh token")
                .build();
    }
}
