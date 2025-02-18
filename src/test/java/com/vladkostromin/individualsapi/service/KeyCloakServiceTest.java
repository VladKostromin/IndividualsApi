package com.vladkostromin.individualsapi.service;


import com.vladkostromin.individualsapi.auth.KeyCloakAuthProvider;
import com.vladkostromin.individualsapi.exception.PasswordMismatchException;
import com.vladkostromin.individualsapi.model.TokenResponse;
import com.vladkostromin.individualsapi.model.UserRegistrationRequest;
import com.vladkostromin.individualsapi.model.UserResponse;
import com.vladkostromin.individualsapi.utils.ApiDataUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatusCode;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class KeyCloakServiceTest {
    @Mock
    private KeyCloakAuthProvider keyCloakAuthProvider;

    @InjectMocks
    private KeyCloakService keyCloakServiceUnderTest;


    @Test
    @DisplayName("Test register user success functionality")
    public void givenRegistrationRequest_whenRegisterUser_thenSuccessTokenResponse() {
        //given
        given(keyCloakAuthProvider.registerUser(anyString(), anyString(), anyString()))
                .willReturn(Mono.just(ApiDataUtils.getTokenResponse()));
        TokenResponse expectedTokenResponse = ApiDataUtils.getTokenResponse();
        //when
        Mono<TokenResponse> resultTokenResponse = keyCloakServiceUnderTest.registerUser(ApiDataUtils.getUserRegistrationRequest());
        //then
        StepVerifier.create(resultTokenResponse)
                .assertNext(tokenResponse -> {
                    assertThat(tokenResponse.getAccessToken()).isEqualTo(expectedTokenResponse.getAccessToken());
                    assertThat(tokenResponse.getRefreshToken()).isEqualTo(expectedTokenResponse.getRefreshToken());
                    assertThat(tokenResponse.getExpiresIn()).isEqualTo(expectedTokenResponse.getExpiresIn());
                })
                .verifyComplete();
        verify(keyCloakAuthProvider, times(1)).registerUser(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Test authenticate user success functionality")
    public void givenAuthRequest_whenAuthenticateUser_thenSuccessTokenResponse() {
        // given
        TokenResponse expectedResponse = ApiDataUtils.getTokenResponse();

        given(keyCloakAuthProvider.authenticate(any(String.class), any(String.class)))
                .willReturn(Mono.just(expectedResponse));

        // when
        Mono<TokenResponse> result = keyCloakServiceUnderTest.authenticateUser(ApiDataUtils.getAuthRequest());

        // then
        StepVerifier.create(result)
                .assertNext(tokenResponse -> {
                    assertThat(tokenResponse.getAccessToken()).isEqualTo(expectedResponse.getAccessToken());
                    assertThat(tokenResponse.getRefreshToken()).isEqualTo(expectedResponse.getRefreshToken());
                    assertThat(tokenResponse.getExpiresIn()).isEqualTo(expectedResponse.getExpiresIn());
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Test refresh token success functionality")
    public void givenRefreshTokenRequest_whenRefreshUserToken_thenSuccessTokenResponse() {
        // given
        TokenResponse expectedResponse = ApiDataUtils.getTokenResponse();

        given(keyCloakAuthProvider.refreshToken(any(String.class)))
                .willReturn(Mono.just(expectedResponse));

        // when
        Mono<TokenResponse> result = keyCloakServiceUnderTest.refreshUserToken(ApiDataUtils.getRefreshTokenRequest());

        // then
        StepVerifier.create(result)
                .assertNext(tokenResponse -> {
                    assertThat(tokenResponse.getAccessToken()).isEqualTo(expectedResponse.getAccessToken());
                    assertThat(tokenResponse.getRefreshToken()).isEqualTo(expectedResponse.getRefreshToken());
                    assertThat(tokenResponse.getExpiresIn()).isEqualTo(expectedResponse.getExpiresIn());
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Test get user by token success functionality")
    public void givenValidToken_whenGetUserByUserId_thenSuccessUserResponse() {
        // given
        UserResponse expectedResponse = ApiDataUtils.getUser();

        given(keyCloakAuthProvider.getUserByUserToken(any(String.class)))
                .willReturn(Mono.just(expectedResponse));

        // when
        Mono<UserResponse> result = keyCloakServiceUnderTest.getUserFromToken("valid-token");

        // then
        StepVerifier.create(result)
                .assertNext(userResponse -> {
                    assertThat(userResponse.getId()).isEqualTo(expectedResponse.getId());
                    assertThat(userResponse.getEmail()).isEqualTo(expectedResponse.getEmail());
                    assertThat(userResponse.getRoles()).isEqualTo(expectedResponse.getRoles());
                })
                .verifyComplete();
    }


    @Test
    @DisplayName("Test registration user functionality")
    public void givenRegistrationRequest_whenRegisterUser_thenPasswordMismatchExceptionIsThrown() {
        //given
        UserRegistrationRequest userRegistrationRequest = ApiDataUtils.getUserRegistrationRequest();
        userRegistrationRequest.setConfirmPassword("confirmPasswordMismatch");

        given(keyCloakAuthProvider.registerUser(anyString(), anyString(), anyString()))
                .willReturn(Mono.error(new PasswordMismatchException(HttpStatusCode.valueOf(400), "Password do not match")));
        //when
        Mono<TokenResponse> resultTokenResponse = keyCloakServiceUnderTest.registerUser(ApiDataUtils.getUserRegistrationRequest());
        //then
        StepVerifier.create(resultTokenResponse)
                .expectErrorMatches(throwable -> throwable instanceof PasswordMismatchException &&
                        throwable.getMessage().equals("Password do not match") &&
                        ((PasswordMismatchException) throwable).getStatus().is4xxClientError())
                .verify();
        verify(keyCloakAuthProvider, times(1)).registerUser(anyString(), anyString(), anyString());
    }

}
