package com.awpghost.auth.controllers;

import com.awpghost.auth.dto.responses.AccessToken;
import com.awpghost.auth.exceptions.TokenVerificationException;
import com.awpghost.auth.services.auth.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RequestMapping("/auth")
@Log4j2
@RestController
public class VerificationController {
    private final AuthService authService;

    @Autowired
    public VerificationController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "Verify mobile number OTP/token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OTP/token verification is successful. Login successful"),
    })
    @PostMapping("/mobileNo/verify")
    public Mono<AccessToken> mobileVerification(@Parameter(description = "OTP or token") @RequestParam(name = "token") final String token, @RequestParam(name = "otp") final String otp) {
        if (StringUtils.hasText(token)) {
            log.info("Verify user account mobile number with token: {}", token);
            return authService.verifyMobileNoToken(token);
        } else if (StringUtils.hasText(otp)) {
            log.info("Verify user account mobile number with otp: {}", otp);
            return authService.verifyMobileNoOTP(otp);
        } else {
            return Mono.error(new TokenVerificationException("No token or otp provided"));
        }
    }

    @Operation(summary = "Verify email address with token/OTP")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful email address verification"),
    })
    @GetMapping("/email/verify")
    public Mono<AccessToken> emailVerification(@RequestParam(name = "token") final String token, @RequestParam(name = "otp") final String otp) {
        if (StringUtils.hasText(token)) {
            log.info("Verify user account email address with token: {}", token);
            return authService.verifyEmailToken(token);
        } else if (StringUtils.hasText(otp)) {
            log.info("Verify user account email address with otp: {}", otp);
            return authService.verifyEmailOTP(otp);
        } else {
            return Mono.error(new TokenVerificationException("No token or otp provided"));
        }
    }
}
