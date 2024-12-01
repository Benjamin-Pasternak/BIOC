package com.github.benjaminpasternak.bioc.exceptions;

/**
 * Thrown when multiple constructors in a class are annotated with @Inject,
 * causing ambiguity during dependency injection.
 */
public class ConstructorSelectionException extends RuntimeException {
    public ConstructorSelectionException(String message) {
        super(message);
    }
}
