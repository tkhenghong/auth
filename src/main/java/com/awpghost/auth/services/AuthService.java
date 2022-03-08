package com.awpghost.auth.services;

import com.awpghost.auth.dto.requests.AuthDto;
import com.awpghost.auth.persistence.models.Auth;

public interface AuthService {
    public Auth register(AuthDto authDto);
    public void login(String username, String password);
    public void logout();
}
