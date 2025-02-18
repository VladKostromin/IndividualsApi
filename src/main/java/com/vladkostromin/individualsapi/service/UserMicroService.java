package com.vladkostromin.individualsapi.service;

import com.vladkostromin.individualsapi.exception.ApiException;
import com.vladkostrov.dto.rest.userserviceapi.RegisterIndividualRequest;
import com.vladkostrov.dto.rest.userserviceapi.RegisterIndividualResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.LinkedHashMap;

@Service
@Slf4j
public class UserMicroService {


    private final WebClient webClient;

    public UserMicroService(@Qualifier("microServiceWebClient")WebClient webClient) {
        this.webClient = webClient;
    }

    public Mono<RegisterIndividualResponse> registerIndividual(RegisterIndividualRequest registerIndividualRequest) {
        log.info("IN registerIndividual request:{}",registerIndividualRequest);
        return webClient.post()
                .uri("/register")
                .bodyValue(registerIndividualRequest)
                .retrieve()
                .onStatus(HttpStatusCode::isError, clientResponse ->
                        clientResponse.bodyToMono(LinkedHashMap.class)
                                .flatMap(body -> Mono.error(new ApiException(clientResponse.statusCode(), body.get("message").toString()))))
                .bodyToMono(RegisterIndividualResponse.class);
    }

    public Mono<RegisterIndividualResponse> rollbackIndividual(RegisterIndividualResponse request) {
        log.info("IN rollbackIndividual request:{}",request);
        return webClient.post()
                .uri("/rollback")
                .bodyValue(request)
                .retrieve()
                .onStatus(HttpStatusCode::isError, clientResponse ->
                        clientResponse.bodyToMono(LinkedHashMap.class)
                                .flatMap(body -> Mono.error(new ApiException(clientResponse.statusCode(), body.get("message").toString()))))
                .bodyToMono(RegisterIndividualResponse.class);
    }

}
