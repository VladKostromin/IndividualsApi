package com.vladkostromin.individualsapi.controller;

import com.vladkostromin.individualsapi.auth.KeyCloakAuthProvider;
import com.vladkostromin.individualsapi.config.KeycloakTestcontainersConfig;
import com.vladkostromin.individualsapi.model.UserRegistrationRequest;
import com.vladkostromin.individualsapi.service.UserService;
import com.vladkostromin.individualsapi.utils.UserServiceUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import({KeycloakTestcontainersConfig.class})
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class AuthControllerTest {
    @Autowired
    private WebTestClient webClient;

    @Autowired
    private UserService userService;

    @Autowired
    private KeyCloakAuthProvider keyCloakAuthProvider;

    @Test
    @DisplayName("Test create user functionality")
    public void givenUserRegistrationRequest_whenRegisterUser_thenUserIsRegistered() {
        //given
        UserRegistrationRequest userRegistrationRequest = UserServiceUtils.getUserRegistrationRequest();
        //when
        WebTestClient.ResponseSpec result = webClient.post()
                .uri("/api/v1/auth/registration")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(userRegistrationRequest)
                .exchange();
        //then
        result.expectStatus().isOk();

    }
}
