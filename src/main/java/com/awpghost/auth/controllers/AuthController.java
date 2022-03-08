package com.awpghost.auth.controllers;

import com.awpghost.auth.dto.requests.AuthDto;
import com.awpghost.auth.dto.responses.GenericResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@RestController
@RequestMapping("/auth")
@Log4j2
public class AuthController {

    @PostMapping("/registration")
    public GenericResponse registerByEmail(@Valid final AuthDto authDto, final HttpServletRequest request) {
        log.debug("Registering user account with information: {}", authDto);

        return new GenericResponse(HttpStatus.OK, "User account registered successfully");
    }
}
