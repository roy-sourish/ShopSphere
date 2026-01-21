package com.shopsphere.common.error;

import java.util.List;

public class ErrorResponse {
    private final String errorType;
    private final String message;
    private final List<FieldErrorResponse> fieldErrors;

    public ErrorResponse(String errorType, String message, List<FieldErrorResponse> fieldErrors){
        this.errorType = errorType;
        this.message = message;
        this.fieldErrors = fieldErrors;
    }

    public String getErrorType() {
        return errorType;
    }

    public String getMessage() {
        return message;
    }

    public List<FieldErrorResponse> getFieldErrors() {
        return fieldErrors;
    }
}
