package com.vladkostromin.individualsapi.auth;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.vladkostromin.individualsapi.exception.ApiException;
import com.vladkostromin.individualsapi.exception.PasswordMismatchException;
import com.vladkostromin.individualsapi.model.TokenResponse;
import com.vladkostromin.individualsapi.model.UserResponse;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.OAuth2Constants;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class KeyCloakAuthProvider implements AuthProvider<TokenResponse, UserResponse>{

    private final WebClient webClient;

    @Value("${keycloak.client-id}")
    private String clientId;
    @Value("${keycloak.client-secret}")
    private String clientSecret;
    @Value("${keycloak.realm}")
    private String realm;
    public KeyCloakAuthProvider(@Qualifier("keycloakWebClient")WebClient webClient) {
        this.webClient = webClient;
    }


    @Override
    public Mono<TokenResponse> authenticate(String email, String password) {
        log.info("IN KeyCloakAuthProvider authenticate");
        return webClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/realms/{realm}/protocol/openid-connect/token")
                        .build(realm))
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .bodyValue("grant_type=" + OAuth2Constants.PASSWORD + "&client_id=" + clientId + "&client_secret=" + clientSecret
                        + "&username=" + email + "&password=" + password)
                .retrieve()
                .bodyToMono(TokenResponse.class)
                .onErrorResume(WebClientResponseException.class, error -> Mono.error(new ApiException(error.getStatusCode(), error.getResponseBodyAsString())));
    }

    @Override
    public Mono<TokenResponse> refreshToken(String refreshToken) {
        log.info("IN KeyCloakAuthProvider refreshToken");
        return webClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/realms/{realm}/protocol/openid-connect/token")
                        .build(realm))
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .bodyValue("grant_type=" + OAuth2Constants.REFRESH_TOKEN + "&client_id=" + clientId + "&client_secret=" + clientSecret
                        + "&refresh_token=" + refreshToken)
                .retrieve()
                .bodyToMono(TokenResponse.class)
                .onErrorResume(WebClientResponseException.class, error -> Mono.error(new ApiException(error.getStatusCode(), error.getResponseBodyAsString())));
    }

    @Override
    public Mono<TokenResponse> registerUser(String email, String password, String confirmPassword) {
        log.info("IN KeyCloakAuthProvider registerUser");
        if(!password.equals(confirmPassword)) {
            return Mono.error(new PasswordMismatchException(HttpStatusCode.valueOf(400), "Passwords do not match"));
        }
        UserRepresentation user = new UserRepresentation();
        user.setEmail(email);
        user.setUsername(email);
        user.setEnabled(true);
        user.setEmailVerified(true);

        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(password);
        credential.setTemporary(false);

        user.setCredentials(Collections.singletonList(credential));
        return getAdminAccessToken()
                .flatMap(adminAccessToken -> webClient.post()
                        .uri(uriBuilder -> uriBuilder
                                .path("/admin/realms/{realm}/users")
                                .build(realm))
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminAccessToken.getAccessToken())
                        .bodyValue(user)
                        .retrieve()
                        .toBodilessEntity()
                        .then(authenticate(email, password))
                )
                .onErrorResume(WebClientResponseException.class, error -> Mono.error(new ApiException(error.getStatusCode(), error.getResponseBodyAsString())));
    }

    @Override
    public Mono<UserResponse> getUserByUserToken(String token) {
        log.info("IN KeycloakAuthProvider getUserById");
        try {
            DecodedJWT decodedJWT = JWT.decode(token);
            String userId = decodedJWT.getSubject();
            String email = decodedJWT.getClaim("email").asString();
            Long createdAtMills = decodedJWT.getClaim("created_at").asLong();
            LocalDateTime createdAt = Instant.ofEpochMilli(createdAtMills).atZone(ZoneId.systemDefault()).toLocalDateTime();
            return Mono.just(UserResponse.builder()
                    .id(userId)
                    .email(email)
                    .roles(getUserRoles(token))
                    .createdAt(createdAt)
                    .build());
        } catch (Exception e) {
            log.error("Failed to decode JWT", e);
            return Mono.error(new ApiException(HttpStatusCode.valueOf(400), "Invalid Token"));
        }
    }

    private Mono<TokenResponse> getAdminAccessToken() {
        return webClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/realms/{realm}/protocol/openid-connect/token")
                        .build(realm))
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .bodyValue("grant_type=" + OAuth2Constants.CLIENT_CREDENTIALS + "&client_id=" + clientId + "&client_secret=" + clientSecret)
                .retrieve()
                .bodyToMono(TokenResponse.class)
                .onErrorResume(WebClientResponseException.class, error -> Mono.error(new ApiException(error.getStatusCode(), error.getResponseBodyAsString())));
    }
    
    @SuppressWarnings("unchecked")
    private List<String> getUserRoles(String token) {
        try {
            DecodedJWT decodedJWT = JWT.decode(token);
            Claim realmAccessClaim = decodedJWT.getClaim("realm_access");
            if (realmAccessClaim != null && !realmAccessClaim.isNull()) {
                Map<String, Object> claims = realmAccessClaim.asMap();
                if(claims.containsKey("roles")) {
                    return (List<String>) claims.get("roles");
                }
            }
        } catch (Exception e) {
            log.error("Failed to extract roles from token: {}", e.getMessage());
        }
        return Collections.emptyList();
    }
}



