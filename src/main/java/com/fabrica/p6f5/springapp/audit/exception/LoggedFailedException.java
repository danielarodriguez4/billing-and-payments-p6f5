package com.fabrica.p6f5.springapp.audit.exception;

import com.fabrica.p6f5.springapp.common.exception.BusinessException;

public class LoggedFailedException extends BusinessException {
    public LoggedFailedException(String message) {
        super(message);
    }

    public LoggedFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
