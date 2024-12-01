package com.github.benjaminpasternak.bioc.exceptions;

public class CyclicDependencyException extends RuntimeException {
    public CyclicDependencyException(String message) {
        super(message);
    }
}
