package com.awpghost.auth.dto.requests;

import lombok.Data;

@Data
public class AuthMobileNoDto extends RegistrationDto {
    private String mobileNo;
}
