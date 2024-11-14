package com.vladkostromin.individualsapi.service;

import com.vladkostromin.individualsapi.auth.KeyCloakAuthProvider;
import com.vladkostromin.individualsapi.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class UserService {
    private final KeyCloakAuthProvider keyCloakAuthProvider;

    public Mono<TokenResponse> registerUser(UserRegistrationRequest registrationRequest) {
        return keyCloakAuthProvider.registerUser(registrationRequest.getEmail(), registrationRequest.getPassword(), registrationRequest.getConfirmPassword());
    }

    public Mono<TokenResponse> authenticateUser(AuthRequest authRequest) {
        return keyCloakAuthProvider.authenticate(authRequest.getEmail(), authRequest.getPassword());
    }

    public Mono<TokenResponse> refreshUserToken(RefreshTokenRequest refreshTokenRequest) {
        return keyCloakAuthProvider.refreshToken(refreshTokenRequest.getRefreshToken());
    }
    public Mono<UserResponse> getUserFromToken(String token) {
        return keyCloakAuthProvider.getUserByUserToken(token);
    }
}
