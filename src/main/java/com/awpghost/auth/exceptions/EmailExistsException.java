package com.awpghost.auth.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
public class EmailExistsException extends RuntimeException {
    public EmailExistsException(final String message) {
        super(message);
    }
}
