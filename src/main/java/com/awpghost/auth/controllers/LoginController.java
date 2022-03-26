package com.awpghost.auth.controllers;

import com.awpghost.auth.dto.requests.AuthEmailDto;
import com.awpghost.auth.dto.requests.AuthMobileNoDto;
import com.awpghost.auth.dto.responses.GenericResponse;
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

import javax.validation.Valid;

@RequestMapping("/auth")
@Log4j2
@RestController
public class LoginController {
    private final AuthService authService;

    @Autowired
    public LoginController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "Login with email address")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful login"),
    })
    @PostMapping("/login/email")
    public GenericResponse emailLogin(@Parameter(description = "Login Information") @Valid AuthEmailDto authEmailDto) {
        log.debug("Login using email address. Body: {}", authEmailDto.toString());

        String response = authService.loginByEmail(authEmailDto).block();

        return new GenericResponse("success");
    }

    @Operation(summary = "Login with mobile number")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login OTP/token is generated. Proceed to verify OTP/token."),
    })
    @PostMapping("/login/mobileNo")
    public GenericResponse mobileLogin(@Parameter(description = "OTP or token") @Valid final AuthMobileNoDto authMobileNoDto) {
        log.debug("Login using mobile number. Body: {}", authMobileNoDto.toString());

        authService.loginByMobileNo(authMobileNoDto);

        return new GenericResponse("success");
    }
}
