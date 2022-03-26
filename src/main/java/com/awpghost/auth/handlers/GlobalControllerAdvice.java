package com.awpghost.auth.handlers;

import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;

/**
 * Advice for global controller exceptions.
 */
@Log4j2
@RestControllerAdvice
public class GlobalControllerAdvice {

    /**
     * Handles HttpClientErrorException.
     *
     * @param ex the exception
     * @return the response entity
     */
    @ExceptionHandler(HttpClientErrorException.class)
    public ResponseEntity<String> handleHttpClientErrorException(HttpClientErrorException ex) {
        log.error(ex.getMessage(), ex);
        return ResponseEntity.status(ex.getRawStatusCode()).body(ex.getResponseBodyAsString());
    }
}
// End of GlobalControllerAdvice.java
