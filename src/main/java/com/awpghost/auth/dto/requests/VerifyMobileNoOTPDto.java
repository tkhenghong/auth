package com.awpghost.auth.dto.requests;

import lombok.Builder;

@Builder
public class VerifyMobileNoOTPDto {
    private String mobileNo;
    private String otp;
}
