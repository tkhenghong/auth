package com.awpghost.auth.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
public class VerificationException extends RuntimeException {
    public VerificationException(String message) {
        super(message);
    }
}
