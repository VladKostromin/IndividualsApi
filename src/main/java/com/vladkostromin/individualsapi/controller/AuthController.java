package com.vladkostromin.individualsapi.controller;

import com.vladkostromin.individualsapi.model.*;
import com.vladkostromin.individualsapi.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/registration")
    public Mono<TokenResponse> registerUser(@RequestBody UserRegistrationRequest registrationRequest) {
        return userService.registerUser(registrationRequest);
    }

    @PostMapping("/login")
    public Mono<TokenResponse> loginUser(@RequestBody AuthRequest authRequest) {
        return userService.authenticateUser(authRequest);
    }

    @PostMapping("/refresh-token")
    public Mono<TokenResponse> refreshToken(@RequestBody RefreshTokenRequest refreshTokenRequest) {
        return userService.refreshUserToken(refreshTokenRequest);
    }

    @GetMapping("/me")
    public Mono<UserResponse> getUser(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
        String accessToken = authorization.replace("Bearer ", "");
        return userService.getUserFromToken(accessToken);
    }
}
