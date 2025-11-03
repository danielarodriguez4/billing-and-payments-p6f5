package com.fabrica.p6f5.springapp.common.exception;

/**
 * Exception for business logic violations.
 */
public class BusinessException extends RuntimeException {
    
    public BusinessException(String message) {
        super(message);
    }
    
    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}

