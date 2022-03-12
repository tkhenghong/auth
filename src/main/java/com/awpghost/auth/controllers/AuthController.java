package com.awpghost.auth.controllers;

import com.awpghost.auth.dto.requests.AuthDto;
import com.awpghost.auth.dto.responses.GenericResponse;
import com.awpghost.auth.persistence.models.Auth;
import com.awpghost.auth.services.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@RestController
@RequestMapping("/auth")
@Log4j2
public class AuthController {
    AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "Register with email address")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful registration"),
    })
    @PostMapping("/register/email")
    public GenericResponse registerByEmail(@Parameter(description = "Registration information") @Valid final AuthDto authDto, final HttpServletRequest request) {
        log.debug("Registering user account with information: {}", authDto);

        Auth auth = authService.registerByEmail(authDto);

        return new GenericResponse("success");
    }
}
