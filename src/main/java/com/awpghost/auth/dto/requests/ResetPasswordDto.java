package com.awpghost.auth.dto.requests;

import com.awpghost.auth.validators.ValidPassword;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
public class ResetPasswordDto {
    @NotNull
    @NotEmpty
    private String token;

    @NotNull
    @NotEmpty
    @ValidPassword
    private String password;
}
