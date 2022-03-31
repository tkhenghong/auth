package com.awpghost.auth.services.user;

import com.awpghost.auth.dto.requests.UserDto;
import com.awpghost.auth.dto.responses.OTPResponse;
import com.awpghost.auth.dto.responses.UserInfoDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import reactivefeign.spring.config.ReactiveFeignClient;
import reactor.core.publisher.Mono;

@ReactiveFeignClient(name = "user-service")
public interface UserClient {
    @PostMapping("/user/register")
    Mono<UserInfoDto> registerUser(@RequestBody UserDto userDto);

    @GetMapping("/verify-mobileNo")
    Mono<Boolean> mobileNumberVerification(@RequestParam(name = "token") String token, @RequestParam(name = "otp") String otp);

    @GetMapping("/verify-email")
    Mono<Boolean> emailVerification(@RequestParam(name = "token") String token, @RequestParam(name = "otp") String otp);

    @GetMapping("")
    Mono<UserInfoDto> getUserById(@RequestParam("id") String id);

    @GetMapping("")
    Mono<UserInfoDto> getUserByEmail(@RequestParam("email") String email);

    @GetMapping("")
    Mono<UserInfoDto> getUserByMobileNo(@RequestParam("mobileNo") String mobileNo);

    @PostMapping("/mobileNo/otp")
    Mono<OTPResponse> generateMobileNumberOTP(@RequestParam(name = "id") final String id);

    @PostMapping("/email/token")
    Mono<Boolean> generateEmailToken(@RequestParam(name = "id") final String id);
}
