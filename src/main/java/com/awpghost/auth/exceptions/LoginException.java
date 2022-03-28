package com.awpghost.auth.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class LoginException extends RuntimeException {
    public LoginException(String message) {
        super(message);
    }
}
