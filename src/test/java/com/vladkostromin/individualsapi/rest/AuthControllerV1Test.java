package com.vladkostromin.individualsapi.rest;

import com.vladkostromin.individualsapi.config.SecurityConfig;
import com.vladkostromin.individualsapi.model.TokenResponse;
import com.vladkostromin.individualsapi.model.UserRegistrationRequest;
import com.vladkostromin.individualsapi.service.UserMicroService;
import com.vladkostromin.individualsapi.service.KeyCloakService;
import com.vladkostromin.individualsapi.utils.ApiDataUtils;
import com.vladkostrov.dto.rest.userserviceapi.RegisterIndividualRequest;
import com.vladkostrov.dto.rest.userserviceapi.RegisterIndividualResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.any;

@ComponentScan({"com.vladkostromin.individualsapi.exception.errorhandler"})
@Import(SecurityConfig.class)
@ExtendWith(SpringExtension.class)
@WebFluxTest(controllers = AuthRestControllerV1.class)
public class AuthControllerV1Test {

    @Autowired
    private WebTestClient webClient;

    @MockBean
    private KeyCloakService keyCloakServiceUnderTest;

    @MockBean
    private UserMicroService userMicroServiceUnderTest;

    @Test
    @DisplayName("Test register user success functionality")
    public void givenAuthRequest_whenRegisterUser_thenSuccessTokenResponse() {
        //given
        UserRegistrationRequest userRegistrationRequest = ApiDataUtils.getUserRegistrationRequest();

        TokenResponse tokenResponse = ApiDataUtils.getTokenResponse();
        RegisterIndividualResponse registerIndividualResponse = ApiDataUtils.getRegisterIndividualResponse();

        BDDMockito.given(keyCloakServiceUnderTest.registerUser(any(UserRegistrationRequest.class))).willReturn(Mono.just(tokenResponse));
        BDDMockito.given(userMicroServiceUnderTest.registerIndividual(any(RegisterIndividualRequest.class)))
                .willReturn(Mono.just(registerIndividualResponse));
        //when
        WebTestClient.ResponseSpec exchange = webClient.post()
                .uri("/api/v1/auth/registration")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(userRegistrationRequest), UserRegistrationRequest.class)
                .exchange();
        //then
        exchange.expectStatus().isOk()
                .expectBody(TokenResponse.class)
                .consumeWith(System.out::println);
    }
}
