package com.vladkostromin.individualsapi.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {
    @Value("${keycloak.server-url}")
    private String baseUrl;

    private static final String[] publicRoutes = {"/api/v1/auth/registration", "/api/v1/auth/login", "/api/v1/auth/refresh-token", "/api/v1/auth/me"};

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        http
                .oauth2ResourceServer(oauth2ResourceServerSpec -> oauth2ResourceServerSpec.jwt(Customizer.withDefaults()))
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(authorizeExchangeSpec -> authorizeExchangeSpec
                        .pathMatchers(publicRoutes).permitAll()
                        .anyExchange().authenticated());
        return http
                .build();
    }

    @Bean
    public WebClient keycloakWebClient() {
        return WebClient.builder()
                .baseUrl(baseUrl)
                .build();
    }
}