package org.grupolys.spring.service.exception;

import org.grupolys.spring.model.responses.ErrorResponse;

public class ServiceException extends Exception {
    private ErrorResponse error;

    public ServiceException(ErrorResponse error) {
        super(error.getMessage());
        this.error = error;
    }

    public ErrorResponse getErrorResponse() {
        return this.error;
    }
}
