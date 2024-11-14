package com.vladkostromin.individualsapi.exception.errorHandler;

import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.autoconfigure.web.reactive.error.AbstractErrorWebExceptionHandler;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import java.util.Map;

@Component
public class AppErrorWebExceptionHandler extends AbstractErrorWebExceptionHandler {
    public AppErrorWebExceptionHandler(AppErrorAttributes appErrorAttributes, ServerCodecConfigurer serverCodecConfigurer, ApplicationContext applicationContext) {
        super(appErrorAttributes, new WebProperties.Resources(), applicationContext);
        super.setMessageWriters(serverCodecConfigurer.getWriters());
        super.setMessageReaders(serverCodecConfigurer.getReaders());
    }

    @Override
    protected RouterFunction<ServerResponse> getRoutingFunction(ErrorAttributes errorAttributes) {
        return RouterFunctions.route(RequestPredicates.all(), request -> {
            Map<String, Object> props = getErrorAttributes(request, ErrorAttributeOptions.defaults());
            return ServerResponse.status(Integer.parseInt(props.getOrDefault("status", "500").toString()))
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(props.get("errors")));
        });
    }
}
