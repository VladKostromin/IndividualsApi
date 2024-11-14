package com.vladkostromin.individualsapi.it;

import com.vladkostromin.individualsapi.config.KeycloakTestcontainersConfig;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import({KeycloakTestcontainersConfig.class})
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class AuthRestControllerV1Test {



//    @Autowired
//    private WebTestClient webTestClient;
//
//    @ServiceConnection
//    static KeycloakContainer keycloak = new KeycloakContainer("quay.io/keycloak/keycloak:20.0.0")
//            .withRealmImportFile("resources/realm-export.json")
//            .withAdminUsername("admin")
//            .withAdminPassword("admin");
//
//    @DynamicPropertySource
//    static void registerProperties(DynamicPropertyRegistry registry) {
//        registry.add("keycloak.url", () -> keycloak.getAuthServerUrl());
//    }
//
//    @Test
//    @DisplayName("Test user registration with valid data")
//    public void givenValidRegistrationRequest_whenRegisterUser_thenReturnTokenResponse() {
//        // given
//        UserRegistrationRequest request = new UserRegistrationRequest("testuser@example.com", "password", "password");
//
//        //when
//        webTestClient.post()
//                .uri("/api/v1/auth/register")
//                .contentType(MediaType.APPLICATION_JSON)
//                .body(BodyInserters.fromValue(request))
//                .exchange()
//                .expectStatus().isOk()
//                .expectBody(TokenResponse.class)
//                .value(tokenResponse -> {
//                    assertNotNull(tokenResponse.getAccessToken());
//                    assertNotNull(tokenResponse.getRefreshToken());
//                    assertEquals(3600, tokenResponse.getExpiresIn());
//                });
//        //then
//    }
//
//    @Test
//    @DisplayName("Test registration with mismatched passwords")
//    public void givenMismatchedPasswords_whenRegisterUser_thenReturnBadRequest() {
//        // Создание запроса, где пароли не совпадают
//        UserRegistrationRequest request = new UserRegistrationRequest("testuser@example.com", "password", "differentPassword");
//
//        // Выполнение POST-запроса и проверка статуса ошибки
//        webTestClient.post()
//                .uri("/api/v1/auth/register")
//                .contentType(MediaType.APPLICATION_JSON)
//                .body(BodyInserters.fromValue(request))
//                .exchange()
//                .expectStatus().isBadRequest()
//                .expectBody()
//                .jsonPath("$.errors[0].message").isEqualTo("Passwords do not match");
//    }

}
