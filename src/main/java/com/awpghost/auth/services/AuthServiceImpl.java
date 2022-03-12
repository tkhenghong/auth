package com.awpghost.auth.services;

import com.awpghost.auth.dto.requests.AuthDto;
import com.awpghost.auth.exceptions.UserAlreadyExistException;
import com.awpghost.auth.persistence.models.Auth;
import com.awpghost.auth.persistence.repositories.AuthRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuthServiceImpl implements AuthService {

    private final AuthRepository authRepository;

    private final PasswordEncoder passwordEncoder;

    @Autowired
    AuthServiceImpl(AuthRepository authRepository, PasswordEncoder passwordEncoder) {
        this.authRepository = authRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Auth registerByEmail(AuthDto authDto) {
        if (emailExists(authDto.getEmail())) {
            throw new UserAlreadyExistException("There is an account with that email address: "
                    + authDto.getEmail());
        }

        Auth auth = Auth.builder()
                .email(authDto.getEmail())
                .password(passwordEncoder.encode(authDto.getPassword()))
                .build();

        // TODO: create user
        return authRepository.save(auth);
    }

    @Override
    public void login(String username, String password) {

    }

    @Override
    public void logout() {

    }

    private boolean emailExists(String email) {
        return authRepository.findByEmail(email) != null;
    }
}
