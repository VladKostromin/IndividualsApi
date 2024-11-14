package com.vladkostromin.individualsapi.exception;


import org.springframework.http.HttpStatusCode;

public class PasswordMismatchException extends ApiException {


    public PasswordMismatchException(HttpStatusCode status, String message) {
        super(status, message);
    }
}
