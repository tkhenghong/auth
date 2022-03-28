//package com.awpghost.auth.controllers;
//
//import com.awpghost.auth.dto.responses.AccessToken;
//import com.awpghost.auth.exceptions.TokenVerificationException;
//import com.awpghost.auth.services.auth.AuthService;
//import io.swagger.v3.oas.annotations.Operation;
//import io.swagger.v3.oas.annotations.Parameter;
//import io.swagger.v3.oas.annotations.responses.ApiResponse;
//import io.swagger.v3.oas.annotations.responses.ApiResponses;
//import lombok.extern.log4j.Log4j2;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.util.StringUtils;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.bind.annotation.RestController;
//import reactor.core.publisher.Mono;
//
//@RequestMapping("/auth")
//@Log4j2
//@RestController
//public class VerificationController {
//    private final AuthService authService;
//
//    @Autowired
//    public VerificationController(AuthService authService) {
//        this.authService = authService;
//    }
//
//    @Operation(summary = "Verify mobile number token")
//    @ApiResponses(value = {
//            @ApiResponse(responseCode = "200", description = "Token verification is successful"),
//    })
//    @GetMapping("/mobileNo/verify")
//    public Mono<Boolean> mobileVerification(@Parameter(description = "User ID Token") @RequestParam(name = "token") final String token) {
//        if (StringUtils.hasText(token)) {
//            log.info("Verify user account mobile number with token: {}", token);
//            return authService.verifyMobileNoToken(token);
//        } else {
//            return Mono.error(new TokenVerificationException("No token or otp provided"));
//        }
//    }
//
//    @Operation(summary = "Verify mobile number OTP")
//    @ApiResponses(value = {
//            @ApiResponse(responseCode = "200", description = "OTP verification is successful. Login successful"),
//    })
//    @GetMapping("/mobileNo/verify")
//    public Mono<AccessToken> mobileVerification(@Parameter(description = "User ID Token") @RequestParam(name = "token") final String token,
//                                                @Parameter(description = "On Time Password") @RequestParam(name = "otp") final String otp) {
//        if (StringUtils.hasText(token) && StringUtils.hasText(otp)) {
//            log.info("Verify user account mobile number with otp: {}", otp);
//            return authService.verifyMobileNoOTP(token, otp);
//        } else {
//            return Mono.error(new TokenVerificationException("No token or otp provided"));
//        }
//    }
//
//    @Operation(summary = "Verify email address with token/OTP")
//    @ApiResponses(value = {
//            @ApiResponse(responseCode = "200", description = "Successful email address verification"),
//    })
//    @GetMapping("/email/verify")
//    public Mono<Boolean> emailVerification(@Parameter(description = "User ID Token") @RequestParam(name = "User ID Token") final String token) {
//        if (StringUtils.hasText(token)) {
//            log.info("Verify user account email address with token: {}", token);
//            return authService.verifyEmailToken(token);
//        } else {
//            return Mono.error(new TokenVerificationException("No token or otp provided"));
//        }
//    }
//}
