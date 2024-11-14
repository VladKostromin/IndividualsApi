package com.vladkostromin.individualsapi.auth;

import reactor.core.publisher.Mono;

public interface AuthProvider<T, U> {
    Mono<T> authenticate(String email, String password);
    Mono<T> refreshToken(String refreshToken);
    Mono<T> registerUser(String email, String password, String confirmPassword);
    Mono<U> getUserByUserToken(String id);
}
