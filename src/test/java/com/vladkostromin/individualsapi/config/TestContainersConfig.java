package com.vladkostromin.individualsapi.config;


import dasniko.testcontainers.keycloak.KeycloakContainer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.keycloak.OAuth2Constants;
import org.mockserver.client.MockServerClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MockServerContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.utility.DockerImageName;



public abstract class TestContainersConfig {
    private static final String KEYCLOAK_IMAGE = "quay.io/keycloak/keycloak:latest";
    private static final String REALM_EXPORT_JSON = "/realm-export.json";
    private static final Logger log = LoggerFactory.getLogger(TestContainersConfig.class);

    private static final String MOCK_SERVER_IMAGE = "mockserver/mockserver:5.15.0";


    private static final String clientId = "orchestrator-api";
    private static final String clientSecret = "OY8dBw6hyng4oJL7LV10PAPivX113Yqx";
    private static final String realm = "authorization-api";

    @Container
    static KeycloakContainer keycloak = new KeycloakContainer(KEYCLOAK_IMAGE).withRealmImportFile(REALM_EXPORT_JSON);

    @Container
    static MockServerContainer mockServer =  new MockServerContainer(DockerImageName.parse(MOCK_SERVER_IMAGE));

    protected static MockServerClient mockServerClient;



    @BeforeAll
    static void startContainers() {
        keycloak.start();
        mockServer.start();
        mockServerClient = new MockServerClient(mockServer.getHost(), mockServer.getServerPort());
    }

    @AfterAll
    static void stopContainers() {
        keycloak.stop();
        mockServer.stop();
    }

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        log.info("keycloak.getFirstMappedPort()={}", keycloak.getFirstMappedPort());
        registry.add("keycloak.server-url",
                () -> "http://localhost:" + keycloak.getFirstMappedPort());
        registry.add("keycloak.auth-server-url",
                () -> "http://localhost:" + keycloak.getFirstMappedPort());
        registry.add("keycloak.get-token-url",
                () -> "http://localhost:" + keycloak.getFirstMappedPort() + "/realms/" + realm + "/protocol/openid-connect/token");
        registry.add("spring.security.oauth2.resourceserver.jwt.issuer-uri",
                () -> "http://localhost:" + keycloak.getFirstMappedPort() + "/realms/" + realm);
        registry.add("spring.security.oauth2.resourceserver.jwt.jwk-set-uri",
                () -> "http://localhost:" + keycloak.getFirstMappedPort() + "/realms/" + realm + "/protocol/openid-connect/certs");
        registry.add("keycloak.realm", () -> realm);
        registry.add("keycloak.grant-type", () -> OAuth2Constants.CLIENT_CREDENTIALS);
        registry.add("keycloak.client-id", () -> clientId);
        registry.add("keycloak.client-secret", () -> clientSecret);
        registry.add("scope", () -> "openid");

        registry.add("user-service.url",
                () -> "http://localhost:" + mockServer.getServerPort() + "/api/v1/individual");
    }

}
