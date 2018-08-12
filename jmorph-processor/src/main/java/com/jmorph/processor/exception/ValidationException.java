package com.jmorph.processor.exception;

public class ValidationException extends Exception {
    private final String error;

    public ValidationException(String error) {
        this.error = error;
    }

    public String getError() {
        return error;
    }
}
