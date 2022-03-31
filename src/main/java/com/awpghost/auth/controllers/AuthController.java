package com.awpghost.auth.controllers;

import com.awpghost.auth.dto.requests.ChangePasswordDto;
import com.awpghost.auth.dto.requests.ForgotPasswordDto;
import com.awpghost.auth.dto.requests.ResetPasswordDto;
import com.awpghost.auth.services.auth.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import javax.validation.Valid;

@RequestMapping("/auth")
@Log4j2
@RestController
public class AuthController {
    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "Forgot Email login password")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reset Password link sent. Check email/SMS for reset password link."),
    })
    @PutMapping("/email/forgot-password")
    public Mono<Boolean> emailForgotPassword(@Parameter(description = "OTP or token") @Valid @RequestBody final ForgotPasswordDto forgotPasswordDto) {
        log.debug("Forgot email password. Body: {}", forgotPasswordDto.toString());

        return authService.emailForgotPassword(forgotPasswordDto.getEmail());
    }

    @Operation(summary = "Reset Email login password")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reset Password successful."),
    })
    @PutMapping("/email/reset-password")
    public Mono<Boolean> emailResetPassword(@Parameter(description = "Reset Password information") @Valid @RequestBody final ResetPasswordDto resetPasswordDto) {
        log.debug("Reset email password. Body: {}", resetPasswordDto.toString());
        return authService.emailResetPassword(resetPasswordDto);
    }

    @Operation(summary = "Email change password")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Change Password successful."),
    })
    @PreAuthorize("hasRole(\"USER\")")
    @PutMapping("/email/change-password")
    public Mono<Boolean> emailChangePassword(@Parameter(description = "Change Password information") @Valid @RequestBody final ChangePasswordDto changePasswordDto) {
        log.debug("Change email password. Body: {}", changePasswordDto.toString());
        return authService.emailChangePassword(changePasswordDto);
    }
}
