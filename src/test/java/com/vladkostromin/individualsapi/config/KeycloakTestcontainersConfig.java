package com.vladkostromin.individualsapi.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
@Configuration
public class KeycloakTestcontainersConfig {

    private static final String KEYCLOAK_IMAGE = "quay.io/keycloak/keycloak:20.0.0";
    private static final String REALM_IMPORT_FILE = "resources/realm-export.json";

    @Bean
    @ServiceConnection
    public GenericContainer<?> keycloakContainer() {
        return new GenericContainer<>(DockerImageName.parse(KEYCLOAK_IMAGE))
                .withExposedPorts(8080)
                .withEnv("KEYCLOAK_ADMIN", "admin")
                .withEnv("KEYCLOAK_ADMIN_PASSWORD", "admin")
                .withEnv("KEYCLOAK_IMPORT", REALM_IMPORT_FILE)
                .withCommand("start-dev")
                .waitingFor(org.testcontainers.containers.wait.strategy.Wait.forHttp("/").forStatusCode(200));
    }


}
