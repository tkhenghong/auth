package com.awpghost.auth.controllers;

import com.awpghost.auth.dto.requests.AuthEmailDto;
import com.awpghost.auth.dto.requests.AuthMobileNoDto;
import com.awpghost.auth.persistence.models.Auth;
import com.awpghost.auth.services.auth.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@RequestMapping("/auth")
@Log4j2
@RestController
public class RegistrationController {
    private final AuthService authService;

    @Autowired
    public RegistrationController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "Register with email address")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful registration"),
    })
    @PostMapping("/register/email")
    public Mono<Auth> registerByEmail(@Parameter(description = "Registration information") @Valid final AuthEmailDto authEmailDto, final HttpServletRequest request) {
        log.debug("Registering user account with information: {}", authEmailDto);

        return authService.registerByEmail(authEmailDto);
    }

    @Operation(summary = "Register with mobile number")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Registration OTP/token is generated. Proceed to verify OTP/token."),
    })
    @PostMapping("/register/mobileNo")
    public Mono<Void> registerByMobileNumber(@Parameter(description = "Registration information") @Valid final AuthMobileNoDto authMobileNoDto, final HttpServletRequest request) {
        log.debug("Registering user account with mobile number: {}", authMobileNoDto.getMobileNo());

        return authService.registerByMobileNo(authMobileNoDto);
    }
}
