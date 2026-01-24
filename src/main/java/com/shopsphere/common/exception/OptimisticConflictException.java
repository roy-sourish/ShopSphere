package com.shopsphere.common.exception;

public class OptimisticConflictException extends RuntimeException {
    public OptimisticConflictException(String message) {
        super(message);
    }
}
