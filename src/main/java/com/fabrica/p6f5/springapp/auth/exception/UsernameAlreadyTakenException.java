package com.fabrica.p6f5.springapp.auth.exception;

import com.fabrica.p6f5.springapp.common.exception.BusinessException;

public class UsernameAlreadyTakenException extends BusinessException {
    public UsernameAlreadyTakenException(String message) {
        super(message);
    }

    public UsernameAlreadyTakenException(String message, Throwable cause) {
        super(message, cause);
    }
}
