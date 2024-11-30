package com.github.benjaminpasternak.bioc.exceptions;

/**
 * Thrown when a bean cannot be instantiated due to reflection-related issues
 * or other problems during bean creation.
 */
public class BeanInstantiationException extends RuntimeException {
    public BeanInstantiationException(String message) {
        super(message);
    }
}
