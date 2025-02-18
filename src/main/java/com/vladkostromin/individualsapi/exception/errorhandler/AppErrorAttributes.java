package com.vladkostromin.individualsapi.exception.errorhandler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vladkostromin.individualsapi.exception.ApiException;
import com.vladkostromin.individualsapi.exception.PasswordMismatchException;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.DefaultErrorAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;

import java.io.IOException;
import java.util.*;

@Component
public class AppErrorAttributes extends DefaultErrorAttributes {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Map<String, Object> getErrorAttributes(ServerRequest request, ErrorAttributeOptions options) {
        Map<String, Object> errorAttributes = super.getErrorAttributes(request, ErrorAttributeOptions.defaults());
        Throwable error = getError(request);

        List<Map<String, Object>> errorList = new ArrayList<>();

        HttpStatus status;
        if(error instanceof PasswordMismatchException) {
            status = HttpStatus.valueOf(((PasswordMismatchException) error).getStatus().value());
            LinkedHashMap<String, Object> errorDetails = new LinkedHashMap<>();
            errorDetails.put("code", status.toString());
            errorDetails.put("message", parseErrorMessage(error.getMessage()));
            errorList.add(errorDetails);
        } else if (error instanceof ApiException) {
            status = HttpStatus.valueOf(((ApiException) error).getStatus().value());
            LinkedHashMap<String, Object> errorDetails = new LinkedHashMap<>();
            errorDetails.put("code", status.toString());
            errorDetails.put("message", parseErrorMessage(error.getMessage()));
            errorList.add(errorDetails);
        } else {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
            String message = error.getMessage();
            if(message != null) {
                message = error.getClass().getName();
            }
            LinkedHashMap<String, Object> errorDetails = new LinkedHashMap<>();
            errorDetails.put("code", "INTERNAL_ERROR");
            errorDetails.put("message", message);
            errorList.add(errorDetails);
        }
        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("errors", errorList);
        errorAttributes.put("status", status.value());
        errorAttributes.put("errors", errorDetails);
        return errorAttributes;
    }

    private String parseErrorMessage(String errorMessage) {
        try {
            JsonNode jsonNode = objectMapper.readTree(errorMessage);
            if(jsonNode.has("errorMessage")) {
                return jsonNode.get("errorMessage").asText();
            } else if(jsonNode.has("error_description")) {
                return jsonNode.get("error_description").asText();
            } else if(jsonNode.has("error")) {
                return jsonNode.get("error").asText();
            } else {
                return errorMessage;
            }
        } catch (IOException e) {
            return errorMessage;
        }
    }
}
