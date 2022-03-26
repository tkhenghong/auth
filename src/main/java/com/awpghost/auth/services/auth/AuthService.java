package com.awpghost.auth.services.auth;

import com.awpghost.auth.dto.requests.AuthEmailDto;
import com.awpghost.auth.dto.requests.AuthMobileNoDto;
import com.awpghost.auth.dto.requests.ChangePasswordDto;
import com.awpghost.auth.dto.requests.ResetPasswordDto;
import com.awpghost.auth.dto.responses.AccessToken;
import com.awpghost.auth.persistence.models.Auth;
import reactor.core.publisher.Mono;

public interface AuthService {
    Mono<Auth> registerByEmail(AuthEmailDto authEmailDto);

    Mono<Void> registerByMobileNo(AuthMobileNoDto authMobileNoDto);

    Mono<AccessToken> verifyMobileNoToken(String token);

    Mono<AccessToken> verifyMobileNoOTP(String otp);

    Mono<AccessToken> verifyEmailToken(String token);

    Mono<AccessToken> verifyEmailOTP(String otp);

    Mono<String> loginByEmail(AuthEmailDto authEmailDto);

    Mono<Void> loginByMobileNo(AuthMobileNoDto authMobileNoDto);

    Mono<Void> emailForgotPassword(String email);

    Mono<Void> emailResetPassword(ResetPasswordDto resetPasswordDto);

    Mono<Void> emailChangePassword(ChangePasswordDto changePasswordDto);

    Mono<Void> logout();
}
