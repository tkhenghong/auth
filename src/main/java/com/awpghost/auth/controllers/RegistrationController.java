package com.awpghost.auth.controllers;

import com.awpghost.auth.dto.requests.AuthEmailDto;
import com.awpghost.auth.dto.requests.AuthMobileNoDto;
import com.awpghost.auth.services.auth.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
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
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/register/email")
    public Mono<Void> registerByEmail(@Parameter(description = "Registration information") @Valid @RequestBody final AuthEmailDto authEmailDto) {
        log.info("Registering user account with information: {}", authEmailDto);

        return authService.registerByEmail(authEmailDto).then();
    }

    @Operation(summary = "Register with mobile number")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Registration OTP/token is generated. Proceed to verify OTP/token."),
    })
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/register/mobileNo")
    public Mono<Void> registerByMobileNumber(@Parameter(description = "Registration information") @Valid @RequestBody final AuthMobileNoDto authMobileNoDto) {
        log.debug("Registering user account with mobile number: {}", authMobileNoDto.getMobileNo());

        return authService.registerByMobileNo(authMobileNoDto).then();
    }
}
