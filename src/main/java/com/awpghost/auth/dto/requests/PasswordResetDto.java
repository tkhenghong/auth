package com.awpghost.auth.dto.requests;


import lombok.Builder;

// A DTO to Email microservice to inform the password is reset successfully.
@Builder
public class PasswordResetDto {
    private String email;
}
