package com.fabrica.p6f5.springapp.auth.exception;

import com.fabrica.p6f5.springapp.common.exception.BusinessException;

public class EmailAlreadyTakenException extends BusinessException {
    public EmailAlreadyTakenException(String message) {
        super(message);
    }

    public EmailAlreadyTakenException(String message, Throwable cause) {
        super(message, cause);
    }
}
