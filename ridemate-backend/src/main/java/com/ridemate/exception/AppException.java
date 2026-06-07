package com.ridemate.exception;

import org.springframework.http.HttpStatus;

/** Thrown from service layer; mapped to HTTP response by GlobalExceptionHandler. */
public class AppException extends RuntimeException {

    private final HttpStatus status;

    public AppException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
