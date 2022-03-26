package com.awpghost.auth.dto.requests;

import com.awpghost.auth.validators.ValidEmail;
import com.awpghost.auth.validators.ValidPassword;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
public class AuthEmailDto {
    @ValidEmail
    @NotNull
    @NotEmpty
    private String email;

    @ValidPassword
    @NotNull
    @NotEmpty
    private String password;
}
