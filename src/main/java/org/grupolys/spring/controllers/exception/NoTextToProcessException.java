package org.grupolys.spring.controllers.exception;

/**
 * ProfileNotFoundException
 */
public class NoTextToProcessException extends Exception {

    private static final long serialVersionUID = -936278389416981048L;

    public NoTextToProcessException(String message) {
        super(message);
    }
}
