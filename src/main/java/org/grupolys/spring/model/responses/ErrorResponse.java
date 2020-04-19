package org.grupolys.spring.model.responses;

import lombok.Value;

@Value
public class ErrorResponse {
    String message;
    String resource;

    public ErrorResponse(String message) {
        this(message, null);
    }

    public ErrorResponse(String message, String resource) {
        this.message = message;
        this.resource = resource;
    }
}
