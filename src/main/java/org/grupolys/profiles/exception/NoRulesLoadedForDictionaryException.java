package org.grupolys.profiles.exception;

/**
 * ProfileNotFoundException
 */
public class NoRulesLoadedForDictionaryException extends Exception {

    private static final long serialVersionUID = 8346479112119261086L;

    public NoRulesLoadedForDictionaryException(String message) {
        super(message);
    }
}