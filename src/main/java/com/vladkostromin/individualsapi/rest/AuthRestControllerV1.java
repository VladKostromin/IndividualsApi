package com.vladkostromin.individualsapi.rest;

import com.vladkostromin.individualsapi.exception.ApiException;
import com.vladkostromin.individualsapi.model.*;
import com.vladkostromin.individualsapi.service.UserMicroService;
import com.vladkostromin.individualsapi.service.KeyCloakService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthRestControllerV1 {

    private final KeyCloakService keyCloakService;
    private final UserMicroService userMicroService;

    @PostMapping("/registration")
    public Mono<TokenResponse> registerUser(@RequestBody UserRegistrationRequest userRegistrationRequest) {
        userRegistrationRequest.getRegisterInfo().setEmail(userRegistrationRequest.getEmail());
        return userMicroService.registerIndividual(userRegistrationRequest.getRegisterInfo())
                .flatMap(registerIndividualResponse -> {
                    log.info("AFTER registerIndividual, response:{}", registerIndividualResponse);
                    return keyCloakService.registerUser(userRegistrationRequest)
                            .onErrorResume(ApiException.class, error -> userMicroService.rollbackIndividual(registerIndividualResponse)
                                    .flatMap(result -> Mono.error(error)));
                });
    }

    @PostMapping("/login")
    public Mono<TokenResponse> loginUser(@RequestBody AuthRequest authRequest) {
        return keyCloakService.authenticateUser(authRequest);
    }

    @PostMapping("/refresh-token")
    public Mono<TokenResponse> refreshToken(@RequestBody RefreshTokenRequest refreshTokenRequest) {
        return keyCloakService.refreshUserToken(refreshTokenRequest);
    }

    @GetMapping("/me")
    public Mono<UserResponse> getUser(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
        String accessToken = authorization.replace("Bearer ", "");
        return keyCloakService.getUserFromToken(accessToken);
    }
}
