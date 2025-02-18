package com.vladkostromin.individualsapi.it;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vladkostromin.individualsapi.config.TestContainersConfig;
import com.vladkostromin.individualsapi.model.*;
import com.vladkostromin.individualsapi.utils.ApiDataUtils;
import com.vladkostrov.dto.rest.userserviceapi.RegisterIndividualResponse;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockserver.model.HttpResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockserver.model.HttpRequest.request;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class AuthRestControllerV1Test extends TestContainersConfig {

    @LocalServerPort
    int randomServerPort;

    @Autowired
    OAuth2ResourceServerProperties oAuth2ResourceServerProperties;

    @Autowired
    private WebTestClient webClient;

    @Autowired
    private ObjectMapper objectMapper;


    @BeforeEach
    void setup(ApplicationContext context) {
        webClient = WebTestClient.bindToServer()
                .baseUrl("http://localhost:%d".formatted(randomServerPort))
                .responseTimeout(Duration.ofMillis(30000))
                .build();
    }


    @Test
    @DisplayName("test user registration success functionality")
    void givenUserRegistrationRequest_whenRegisterUser_thenSuccessTokenResponse() throws Exception {
        //given
        UserRegistrationRequest userRegistrationRequest = ApiDataUtils.getUserRegistrationRequest();
        userRegistrationRequest.setEmail("testForRegistration@test.com");
        RegisterIndividualResponse registerIndividualResponse = ApiDataUtils.getRegisterIndividualResponse();
        mockServerClient.when(request()
                .withMethod("POST")
                .withPath("/api/v1/individual/register"))
                .respond(HttpResponse.response()
                        .withStatusCode(201)
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody(objectMapper.writeValueAsBytes(registerIndividualResponse)));
        //when
        WebTestClient.ResponseSpec result = webClient.post()
                .uri("/api/v1/auth/registration")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(userRegistrationRequest))
                .exchange();
        //then
        result.expectStatus().isOk()
                .expectBody(TokenResponse.class)
                .value(tokenResponse -> {
                    assertNotNull(tokenResponse.getAccessToken());
                    assertNotNull(tokenResponse.getRefreshToken());
                    assertEquals(300, tokenResponse.getExpiresIn());
                    assertEquals("Bearer", tokenResponse.getTokenType());
                });
    }

    @Test
    @DisplayName("test user registration password miss match fail functionality")
    void givenUserRegistrationRequest_whenRegisterUser_thenPasswordMismatchResponse() throws Exception {
        //given
        UserRegistrationRequest userRegistrationRequest = ApiDataUtils.getUserRegistrationRequest();
        userRegistrationRequest.setEmail("testForRegistrationPasswordMissmatch@test.com");
        userRegistrationRequest.setConfirmPassword("invalidConfirmPassword");
        RegisterIndividualResponse registerIndividualResponse = ApiDataUtils.getRegisterIndividualResponse();
        RegisterIndividualResponse registerIndividualDeleteResponse = ApiDataUtils.getRegisterIndividualResponse();
        registerIndividualDeleteResponse.setStatus("DELETED");
        mockServerClient.when(request()
                        .withMethod("POST")
                        .withPath("/api/v1/individual/register"))
                .respond(HttpResponse.response()
                        .withStatusCode(201)
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody(objectMapper.writeValueAsBytes(registerIndividualResponse)));
        mockServerClient.when(request()
                        .withPath("POST")
                        .withPath("/api/v1/individual/rollback"))
                .respond(HttpResponse.response()
                        .withStatusCode(200)
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody(objectMapper.writeValueAsBytes(registerIndividualDeleteResponse)));
        //when
        WebTestClient.ResponseSpec result = webClient.post()
                .uri("/api/v1/auth/registration")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(userRegistrationRequest))
                .exchange();
        //then
        result.expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.errors[0].code").isEqualTo("400 BAD_REQUEST")
                .jsonPath("$.errors[0].message").isEqualTo("Passwords do not match");
    }



    @Test
    @DisplayName("Test user registration fail with same email functionality")
    public void givenUserRegistrationRequestWithSameEmail_whenRegisterUser_thenConflictResponse() throws Exception {
        // given
        UserRegistrationRequest userRegistrationRequest = ApiDataUtils.getUserRegistrationRequest();
        userRegistrationRequest.setEmail("testFailRegistrationSameEmail@test.com");
        RegisterIndividualResponse registerIndividualCreateResponse = ApiDataUtils.getRegisterIndividualResponse();
        RegisterIndividualResponse registerIndividualDeleteResponse = ApiDataUtils.getRegisterIndividualResponse();
        registerIndividualDeleteResponse.setStatus("DELETED");

        mockServerClient.when(request()
                        .withMethod("POST")
                        .withPath("/api/v1/individual/register"))
                .respond(HttpResponse.response()
                        .withStatusCode(201)
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody(objectMapper.writeValueAsBytes(registerIndividualCreateResponse)));
        mockServerClient.when(request()
                .withPath("POST")
                .withPath("/api/v1/individual/rollback"))
                .respond(HttpResponse.response()
                        .withStatusCode(200)
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody(objectMapper.writeValueAsBytes(registerIndividualDeleteResponse)));

        //when
        WebTestClient.ResponseSpec firstResponse = webClient.post()
                .uri("/api/v1/auth/registration")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(userRegistrationRequest))
                .exchange();

        firstResponse.expectStatus().isOk()
                .expectBody(TokenResponse.class)
                .value(tokenResponse -> {
                    assertNotNull(tokenResponse.getAccessToken());
                    assertNotNull(tokenResponse.getRefreshToken());
                    assertEquals(300, tokenResponse.getExpiresIn());
                    assertEquals("Bearer", tokenResponse.getTokenType());
                });

        WebTestClient.ResponseSpec secondResponse = webClient.post()
                .uri("/api/v1/auth/registration")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(userRegistrationRequest))
                .exchange();
        ;
        //then
        secondResponse.expectStatus().isEqualTo(HttpStatus.CONFLICT)
                .expectBody()
                .jsonPath("$.errors[0].code").isEqualTo("409 CONFLICT")
                .jsonPath("$.errors[0].message").value(CoreMatchers.containsString("User exists with same email"));
    }

    @Test
    @DisplayName("Test user login success functionality")
    void givenValidAuthRequest_whenLoginUser_thenSuccessTokenResponse() throws Exception {
        //given
        AuthRequest authRequest = ApiDataUtils.getAuthRequest();
        authRequest.setEmail("testForLogin@test.com");

        UserRegistrationRequest userRegistrationRequest = ApiDataUtils.getUserRegistrationRequest();
        userRegistrationRequest.setEmail(authRequest.getEmail());

        RegisterIndividualResponse registerIndividualCreateResponse = ApiDataUtils.getRegisterIndividualResponse();
        mockServerClient.when(request()
                        .withMethod("POST")
                        .withPath("/api/v1/individual/register"))
                .respond(HttpResponse.response()
                        .withStatusCode(201)
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody(objectMapper.writeValueAsBytes(registerIndividualCreateResponse)));
        //when
        WebTestClient.ResponseSpec firstResponse = webClient.post()
                .uri("/api/v1/auth/registration")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(userRegistrationRequest))
                .exchange();

        firstResponse.expectStatus().isOk()
                .expectBody(TokenResponse.class)
                .value(tokenResponse -> {
                    assertNotNull(tokenResponse.getAccessToken());
                    assertNotNull(tokenResponse.getRefreshToken());
                    assertEquals(300, tokenResponse.getExpiresIn());
                    assertEquals("Bearer", tokenResponse.getTokenType());
                });

        WebTestClient.ResponseSpec loginResponse = webClient.post()
                .uri("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(authRequest)
                .exchange();

        //then
        loginResponse.expectStatus().isOk()
                .expectBody(TokenResponse.class)
                .value(tokenResponse -> {
                    assertNotNull(tokenResponse.getAccessToken());
                    assertNotNull(tokenResponse.getRefreshToken());
                    assertEquals(300, tokenResponse.getExpiresIn());
                    assertEquals("Bearer", tokenResponse.getTokenType());
                });
    }

    @Test
    @DisplayName("Test user login fail with invalid credentials")
    void givenInvalidAuthRequest_whenLoginUser_thenUnauthorized() throws Exception {
        // given
        AuthRequest authRequest = ApiDataUtils.getAuthRequest();
        authRequest.setEmail("testFailLogin@test.com");
        authRequest.setPassword("invalidPassword");
        UserRegistrationRequest userRegistrationRequest = ApiDataUtils.getUserRegistrationRequest();
        userRegistrationRequest.setEmail(authRequest.getEmail());
        RegisterIndividualResponse registerIndividualResponse = ApiDataUtils.getRegisterIndividualResponse();
        mockServerClient.when(request()
                        .withMethod("POST")
                        .withPath("/api/v1/individual/register"))
                .respond(HttpResponse.response()
                        .withStatusCode(201)
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody(objectMapper.writeValueAsBytes(registerIndividualResponse)));


        // when
        WebTestClient.ResponseSpec firstResponse = webClient.post()
                .uri("/api/v1/auth/registration")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(userRegistrationRequest))
                .exchange();

        firstResponse.expectStatus().isOk()
                .expectBody(TokenResponse.class)
                .value(tokenResponse -> {
                    assertNotNull(tokenResponse.getAccessToken());
                    assertNotNull(tokenResponse.getRefreshToken());
                    assertEquals(300, tokenResponse.getExpiresIn());
                    assertEquals("Bearer", tokenResponse.getTokenType());
                });

        WebTestClient.ResponseSpec result = webClient.post()
                .uri("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(authRequest)
                .exchange();

        // then
        result.expectStatus().isUnauthorized()
                .expectBody()
                .jsonPath("$.errors[0].code").isEqualTo("401 UNAUTHORIZED")
                .jsonPath("$.errors[0].message").value(CoreMatchers.containsString("Invalid user credentials"));
    }

    @Test
    @DisplayName("Test refresh token success functionality")
    void givenValidRefreshTokenRequest_whenRefreshToken_thenSuccessTokenResponse() throws Exception {
        // given
        RefreshTokenRequest refreshTokenRequest = ApiDataUtils.getRefreshTokenRequest();
        UserRegistrationRequest userRegistrationRequest = ApiDataUtils.getUserRegistrationRequest();
        userRegistrationRequest.setEmail("testRefreshToken@test.com");

        RegisterIndividualResponse registerIndividualCreateResponse = ApiDataUtils.getRegisterIndividualResponse();
        mockServerClient.when(request()
                        .withMethod("POST")
                        .withPath("/api/v1/individual/register"))
                .respond(HttpResponse.response()
                        .withStatusCode(201)
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody(objectMapper.writeValueAsBytes(registerIndividualCreateResponse)));
        //when
        WebTestClient.ResponseSpec registerUser = webClient.post()
                .uri("/api/v1/auth/registration")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(userRegistrationRequest))
                .exchange();

        registerUser.expectStatus().isOk()
                .expectBody(TokenResponse.class)
                .value(tokenResponse -> {
                    assertNotNull(tokenResponse.getAccessToken());
                    assertNotNull(tokenResponse.getRefreshToken());
                    assertEquals(300, tokenResponse.getExpiresIn());
                    assertEquals("Bearer", tokenResponse.getTokenType());
                })
                .consumeWith(tokenResponse -> {
                    TokenResponse tokenResponseAfterAuth = tokenResponse.getResponseBody();
                    assertNotNull(tokenResponseAfterAuth);
                    refreshTokenRequest.setRefreshToken(tokenResponseAfterAuth.getRefreshToken());
                });

        WebTestClient.ResponseSpec refreshTokenResponse = webClient.post()
                .uri("/api/v1/auth/refresh-token")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(refreshTokenRequest)
                .exchange();
        // then
        refreshTokenResponse.expectStatus().isOk()
                .expectBody(TokenResponse.class)
                .value(tokenResponse -> {
                    assertNotNull(tokenResponse.getAccessToken());
                    assertNotEquals(tokenResponse.getRefreshToken(), refreshTokenRequest.getRefreshToken());
                    assertEquals(300, tokenResponse.getExpiresIn());
                    assertEquals("Bearer", tokenResponse.getTokenType());
                });
    }
    @Test
    @DisplayName("Test get user success functionality")
    void givenValidAccessToken_whenGetUser_thenSuccessUserResponse() throws Exception {
        //given
        AtomicReference<String> validToken = new AtomicReference<>("");
        UserRegistrationRequest userRegistrationRequest = ApiDataUtils.getUserRegistrationRequest();
        userRegistrationRequest.setEmail("testGetUser@test.com");

        RegisterIndividualResponse registerIndividualCreateResponse = ApiDataUtils.getRegisterIndividualResponse();
        mockServerClient.when(request()
                        .withMethod("POST")
                        .withPath("/api/v1/individual/register"))
                .respond(HttpResponse.response()
                        .withStatusCode(201)
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody(objectMapper.writeValueAsBytes(registerIndividualCreateResponse)));
        //when
        WebTestClient.ResponseSpec registerUser = webClient.post()
                .uri("/api/v1/auth/registration")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(userRegistrationRequest))
                .exchange();

        registerUser.expectStatus().isOk()
                .expectBody(TokenResponse.class)
                .value(tokenResponse -> {
                    assertNotNull(tokenResponse.getAccessToken());
                    assertNotNull(tokenResponse.getRefreshToken());
                    assertEquals(300, tokenResponse.getExpiresIn());
                    assertEquals("Bearer", tokenResponse.getTokenType());
                })
                .consumeWith(tokenResponse -> {
                    TokenResponse responseBody = tokenResponse.getResponseBody();
                    assertNotNull(responseBody);
                    validToken.set(responseBody.getAccessToken());
                });

        WebTestClient.ResponseSpec userDetailsResponse = webClient.get()
                .uri("/api/v1/auth/me")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken.get())
                .exchange();
        //then
        userDetailsResponse.expectStatus().isOk()
                .expectBody(UserResponse.class)
                .value(userResponse -> {
                    assertNotNull(userResponse.getId());
                    assertEquals(userRegistrationRequest.getEmail().toLowerCase(), userResponse.getEmail());
                    assertNotNull(userResponse.getRoles());
                    assertNotNull(userResponse.getCreatedAt());
                });


    }


}
