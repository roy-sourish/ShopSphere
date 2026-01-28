package com.shopsphere.common.exception;

public class OptimisticConflictException extends RuntimeException {
    public OptimisticConflictException(String message) {
        super(message);
    }

    public OptimisticConflictException(String resourceName, Long resourceId) {
        super(resourceName + " with id " + resourceId +
                " was modified by another request. Please reload and retry.");
    }
}
