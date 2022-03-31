package com.awpghost.auth.services.auth;

import com.awpghost.auth.dto.requests.AuthEmailDto;
import com.awpghost.auth.dto.requests.AuthMobileNoDto;
import com.awpghost.auth.dto.requests.ChangePasswordDto;
import com.awpghost.auth.dto.requests.ResetPasswordDto;
import com.awpghost.auth.dto.responses.AccessToken;
import com.awpghost.auth.dto.responses.OTPResponse;
import com.awpghost.auth.persistence.models.Auth;
import reactor.core.publisher.Mono;

public interface AuthService {
    Mono<Auth> registerByEmail(AuthEmailDto authEmailDto);

    Mono<Void> registerByMobileNo(AuthMobileNoDto authMobileNoDto);

    Mono<AccessToken> loginByEmail(AuthEmailDto authEmailDto);

    Mono<OTPResponse> loginByMobileNo(AuthMobileNoDto authMobileNoDto);

    Mono<Boolean> emailForgotPassword(String email);

    Mono<Boolean> emailResetPassword(ResetPasswordDto resetPasswordDto);

    Mono<Boolean> emailChangePassword(ChangePasswordDto changePasswordDto);

    Mono<Void> logout();
}
