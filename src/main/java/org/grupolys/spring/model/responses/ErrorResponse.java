package org.grupolys.spring.model.responses;

import lombok.Value;
import org.springframework.http.HttpStatus;

@Value
public class ErrorResponse {
    String message;
    String resource;
    HttpStatus code;

    public ErrorResponse(String message, HttpStatus code) {
        this(message, null, code);
    }

    public ErrorResponse(String message, String resource, HttpStatus code) {
        this.message = message;
        this.resource = resource;
        this.code = code;
    }
}
