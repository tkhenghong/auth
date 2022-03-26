package com.awpghost.auth.services.auth;

import com.awpghost.auth.dto.requests.*;
import com.awpghost.auth.dto.responses.AccessToken;
import com.awpghost.auth.dto.responses.UserInfoDto;
import com.awpghost.auth.exceptions.RegistrationException;
import com.awpghost.auth.exceptions.TokenVerificationException;
import com.awpghost.auth.persistence.models.Auth;
import com.awpghost.auth.persistence.repositories.AuthRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveValueOperations;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

@Log4j2
@Service
@Transactional
public class AuthServiceImpl implements AuthService {

    private final AuthRepository authRepository;

    private final PasswordEncoder passwordEncoder;

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private final WebClient.Builder webClientBuilder;

    private final ReactiveValueOperations<String, String> reactiveValueOps;

    @Autowired
    AuthServiceImpl(AuthRepository authRepository,
                    PasswordEncoder passwordEncoder,
                    KafkaTemplate<String, Object> kafkaTemplate,
                    WebClient.Builder webClientBuilder,
                    ReactiveRedisTemplate<String, String> reactiveRedisTemplate) {
        this.authRepository = authRepository;
        this.passwordEncoder = passwordEncoder;
        this.kafkaTemplate = kafkaTemplate;
        this.webClientBuilder = webClientBuilder;
        this.reactiveValueOps = reactiveRedisTemplate.opsForValue();
    }

    @Override
    public Mono<Auth> registerByEmail(AuthEmailDto authEmailDto) {
        WebClient webClient = webClientBuilder.baseUrl("http://user/").build();

        return webClient.get().uri("/email/{email}", authEmailDto.getEmail())
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .retrieve()
                .bodyToMono(UserInfoDto.class)
                .onErrorResume(WebClientResponseException.class, clientResponse -> {
                    if (clientResponse.getStatusCode().equals(HttpStatus.NOT_FOUND)) {
                        UserDto userDto = UserDto.builder()
                                .email(authEmailDto.getEmail())
                                .build();
                        return webClient.post().uri("/user/register").body(Mono.just(userDto), UserDto.class)
                                .retrieve()
                                .bodyToMono(UserInfoDto.class);
                    } else {
                        log.info("User not exist with email: {}", authEmailDto.getEmail());

                        return Mono.error(new RegistrationException("Unable to register user with email: " + authEmailDto.getEmail()));
                    }
                })
                .flatMap(userInfoDto -> {
                    Auth auth = Auth.builder()
                            .password(passwordEncoder.encode(authEmailDto.getPassword()))
                            .build();

                    kafkaTemplate.send("user.verify.email", userInfoDto.getId());
                    return Mono.just(authRepository.save(auth));
                });
    }

    @Override
    public Mono<Void> registerByMobileNo(AuthMobileNoDto authMobileNoDto) {
        WebClient webClient = webClientBuilder.baseUrl("http://user/").build();

        return webClient.get().uri("/mobileNo/{mobileNo}", authMobileNoDto.getMobileNo())
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .retrieve()
                .bodyToMono(UserInfoDto.class)
                .onErrorResume(WebClientResponseException.class, clientResponse -> {
                    if (clientResponse.getStatusCode().equals(HttpStatus.NOT_FOUND)) {
                        UserDto userDto = UserDto.builder()
                                .email(authMobileNoDto.getMobileNo())
                                .build();
                        return webClient.post().uri("/user/register").body(Mono.just(userDto), UserDto.class)
                                .retrieve()
                                .bodyToMono(UserInfoDto.class);
                    } else {
                        log.info("User not exist with mobile no: {}", authMobileNoDto.getMobileNo());
                        throw new RegistrationException("Unable to register user with email: " + authMobileNoDto.getMobileNo());
                    }
                })
                .flatMap(userInfoDto -> {
                    kafkaTemplate.send("user.verify.mobileno", userInfoDto.getId());
                    return Mono.empty();
                }).then();
    }

    public Mono<AccessToken> verifyMobileNoToken(String token) {
        return Mono.fromCallable(() -> {

            return AccessToken.builder().build();
        }).onErrorMap(e -> new TokenVerificationException("Unable to verify token: " + token));
    }

    public Mono<AccessToken> verifyMobileNoOTP(String otp) {
        return Mono.fromCallable(() -> {

            return AccessToken.builder().build();
        }).onErrorMap(e -> new TokenVerificationException("Unable to verify otp: " + otp));
    }

    public Mono<AccessToken> verifyEmailToken(String token) {
        return Mono.fromCallable(() -> {

            return AccessToken.builder().build();
        }).onErrorMap(e -> new TokenVerificationException("Unable to verify token: " + token));
    }

    public Mono<AccessToken> verifyEmailOTP(String otp) {
        return Mono.fromCallable(() -> {

            return AccessToken.builder().build();
        }).onErrorMap(e -> new TokenVerificationException("Unable to verify otp: " + otp));
    }

    @Override
    public Mono<String> loginByEmail(AuthEmailDto authEmailDto) {
        return null;
    }

    @Override
    public Mono<Void> loginByMobileNo(AuthMobileNoDto authMobileNoDto) {
        return null;
    }

    @Override
    public Mono<Void> emailForgotPassword(String email) {
        return null;
    }

    @Override
    public Mono<Void> emailResetPassword(ResetPasswordDto resetPasswordDto) {
        return null;
    }

    @Override
    public Mono<Void> emailChangePassword(ChangePasswordDto changePasswordDto) {
        return null;
    }

    @Override
    public Mono<Void> logout() {
        return null;
    }

}
