package org.grupolys.spring.controllers.exception;

/**
 * ProfileNotFoundException
 */
public class InvalidPosException extends Exception {

    private static final long serialVersionUID = 1L;

    public InvalidPosException(String message) {
        super(message);
    }
}
