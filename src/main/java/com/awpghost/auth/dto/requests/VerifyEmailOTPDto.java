package com.awpghost.auth.dto.requests;

import lombok.Builder;

@Builder
public class VerifyEmailOTPDto {
    private String email;
    private String otp;
}
