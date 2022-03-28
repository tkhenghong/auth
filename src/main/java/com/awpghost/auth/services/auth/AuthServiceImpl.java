package com.awpghost.auth.services.auth;

import com.awpghost.auth.configurations.web_security.MyUserDetailsService;
import com.awpghost.auth.dto.requests.*;
import com.awpghost.auth.dto.responses.AccessToken;
import com.awpghost.auth.dto.responses.OTPResponse;
import com.awpghost.auth.dto.responses.UserInfoDto;
import com.awpghost.auth.enums.VerificationType;
import com.awpghost.auth.exceptions.*;
import com.awpghost.auth.persistence.models.Auth;
import com.awpghost.auth.persistence.models.Role;
import com.awpghost.auth.persistence.models.relationships.AuthUser;
import com.awpghost.auth.persistence.repositories.AuthRepository;
import com.awpghost.auth.persistence.repositories.AuthUserRepository;
import com.awpghost.auth.persistence.repositories.RoleRepository;
import com.awpghost.auth.utils.jwt.JwtUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveValueOperations;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

@Log4j2
@Transactional
@Service
public class AuthServiceImpl implements AuthService {

    private final AuthRepository authRepository;

    private final AuthUserRepository authUserRepository;

    private final RoleRepository roleRepository;

    private final PasswordEncoder passwordEncoder;

    private final KafkaTemplate<String, String> kafkaTemplate;

    private final ReactiveValueOperations<String, String> reactiveValueOps;

    private final WebClient userWebClient;

    private final MyUserDetailsService myUserDetailsService;

    private final JwtUtil jwtUtil;

    private final Integer TOKEN_EXPIRATION_TIME;

    private final AuthenticationManager authenticationManager;

    private final Integer TOKEN_LENGTH;

    private final AtomicReference<ObjectMapper> objectMapper;

    @Autowired
    AuthServiceImpl(@Lazy AuthRepository authRepository,
                    @Lazy AuthUserRepository authUserRepository,
                    @Lazy RoleRepository roleRepository,
                    @Lazy MyUserDetailsService myUserDetailsService,
                    JwtUtil jwtUtil,
                    PasswordEncoder passwordEncoder,
                    ObjectMapper objectMapper,
                    AuthenticationManager authenticationManager,
                    KafkaTemplate<String, String> kafkaTemplate,
                    WebClient.Builder webClientBuilder,
                    ReactiveRedisTemplate<String, String> reactiveRedisTemplate,
                    @Value("${otp.length}") Integer TOKEN_LENGTH,
                    @Value("${token.expiration.time.seconds}") Integer TOKEN_EXPIRATION_TIME) {
        this.authRepository = authRepository;
        this.authUserRepository = authUserRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.kafkaTemplate = kafkaTemplate;
        this.reactiveValueOps = reactiveRedisTemplate.opsForValue();
        this.myUserDetailsService = myUserDetailsService;
        this.jwtUtil = jwtUtil;
        this.TOKEN_EXPIRATION_TIME = TOKEN_EXPIRATION_TIME;
        this.TOKEN_LENGTH = TOKEN_LENGTH;
        this.authenticationManager = authenticationManager;
        this.objectMapper = new AtomicReference<>(objectMapper);

        userWebClient = webClientBuilder.baseUrl("http://user").build();
    }

    @Override
    public Mono<Auth> registerByEmail(AuthEmailDto authEmailDto) {
        return getUserInfo(authEmailDto, VerificationType.EMAIL)
                .flatMap(userInfoDto -> createAuthFromUser(userInfoDto, VerificationType.EMAIL));
    }

    @Override
    public Mono<Void> registerByMobileNo(AuthMobileNoDto authMobileNoDto) {
        return getUserInfo(authMobileNoDto, VerificationType.MOBILE_NUMBER)
                .flatMap(userInfoDto -> createAuthFromUser(userInfoDto, VerificationType.MOBILE_NUMBER))
                .flatMap(auth -> {
                    auth.setPassword(passwordEncoder.encode(authMobileNoDto.getPassword()));
                    return Mono.just(authRepository.save(auth));
                }).then();
    }

//    public Mono<Boolean> verifyMobileNoToken(String token) {
//        return verifyToken(token, VerificationType.MOBILE_NUMBER);
//    }
//
//    /**
//     * @param token: A token that is used to verify the user.
//     * @return Access token if the user is verified.
//     */
//    @Override
//    public Mono<AccessToken> verifyMobileNoOTP(String token, String otp) {
//        return verifyOTP(token, otp, VerificationType.MOBILE_NUMBER);
//    }
//
//    @Override
//    public Mono<Boolean> verifyEmailToken(String token) {
//        return verifyToken(token, VerificationType.EMAIL);
//    }

    @Override
    public Mono<AccessToken> loginByEmail(AuthEmailDto authEmailDto) {
        return userWebClient.get().uri("?email=", authEmailDto.getEmail())
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .retrieve()
                .bodyToMono(UserInfoDto.class)
                .flatMap(userInfoDto -> Mono.just(authRepository.findById(userInfoDto.getId()).get())).cast(Auth.class)
                .onErrorMap(NoSuchElementException.class, e -> new UserNotFoundException("User not found"))
                .flatMap(this::generateJWT);
    }

    /**
     * @param authMobileNoDto: The mobile number and OTP.
     * @return A string that relates to the current authentication request. The user must use this string to verify the OTP together.
     */
    @Override
    public Mono<OTPResponse> loginByMobileNo(AuthMobileNoDto authMobileNoDto) {
        return userWebClient.get().uri("?mobileNo=", authMobileNoDto.getMobileNo())
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .retrieve()
                .bodyToMono(UserInfoDto.class)
                .flatMap(userInfoDto -> Mono.just(authRepository.findById(userInfoDto.getId()).get())).cast(Auth.class)
                .flatMap(auth -> Mono.just(authUserRepository.findById(auth.getId()).get())).cast(AuthUser.class)
                .onErrorMap(NoSuchElementException.class, e -> new UserNotFoundException("User not found"))
                .flatMap(this::requestOTP).cast(OTPResponse.class);
    }

    private Mono<OTPResponse> requestOTP(AuthUser authUser) {
        return userWebClient.post().uri("/mobileNo/otp?id=" + authUser.getUserId())
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .retrieve().bodyToMono(OTPResponse.class);
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

    private Mono<UserInfoDto> getUserInfo(RegistrationDto registrationDto, VerificationType verificationType) {
        String url;
        String id;
        UserDto userDto = UserDto.builder().build();
        String errorMessage = "Unable to register user with";

        switch (verificationType) {
            case EMAIL:
                AuthEmailDto authEmailDto = (AuthEmailDto) registrationDto;
                id = authEmailDto.getEmail();
                url = "?email=" + id;
                userDto.setEmail(id);
                break;
            case MOBILE_NUMBER:
                AuthMobileNoDto authMobileNoDto = (AuthMobileNoDto) registrationDto;
                id = authMobileNoDto.getMobileNo();
                url = "?mobileNo=" + id;
                userDto.setMobileNo(id);
                break;
            default:
                throw new IllegalArgumentException("Invalid verification type");
        }

        return userWebClient.get().uri(url)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .retrieve()
                .bodyToMono(UserInfoDto.class)
                .onErrorResume(WebClientResponseException.class, clientResponse -> {
                    if (clientResponse.getStatusCode().equals(HttpStatus.NOT_FOUND)) {
                        return getUserInfo(userDto);
                    } else {
                        log.info(errorMessage + id);
                        return Mono.error(new RegistrationException(errorMessage + id));
                    }
                });
    }

    private Mono<UserInfoDto> getUserInfo(UserDto userDto) {
        return userWebClient.post().uri("/register").body(Mono.just(userDto), UserDto.class)
                .retrieve()
                .bodyToMono(UserInfoDto.class);
    }

    private Mono<Auth> createAuthFromUser(UserInfoDto userInfoDto, VerificationType verificationType) {
        return Mono.fromCallable(() -> {
            Optional<Role> rolesOptional = roleRepository.findByName("ROLE_USER");

            Role role = rolesOptional.orElseThrow(() -> new RegistrationException("Internal error occurred while registering user."));

            Auth auth = Auth.builder()
                    .roles(Collections.singletonList(role))
                    .build();

            Auth saved = authRepository.save(auth);

            AuthUser authUser = AuthUser.builder()
                    .userId(userInfoDto.getId())
                    .build();

            authUserRepository.save(authUser);

            switch (verificationType) {
                case EMAIL:
                    kafkaTemplate.send("user.verify.email", userInfoDto.getId());
                    break;
                case MOBILE_NUMBER:
                    kafkaTemplate.send("user.verify.mobile", userInfoDto.getId());
                    break;
            }

            return saved;
        });
    }

//    private Mono<AccessToken> verifyOTP(String usedIdToken, String otp, VerificationType verificationType) {
//        String url;
//
//        switch (verificationType) {
//            case EMAIL:
//                url = "verify-email?token=" + usedIdToken + "&otp=" + otp;
//                break;
//            case MOBILE_NUMBER:
//                url = "verify-mobileNo?token=" + usedIdToken + "&otp=" + otp;
//                break;
//            default:
//                throw new IllegalArgumentException("Invalid verification type");
//        }
//
//        return Mono.fromCallable(() -> userWebClient.get().uri(url)
//                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
//                        .retrieve()
//                        .onStatus(HttpStatus::is4xxClientError, clientResponse -> {
//                            if (clientResponse.statusCode().equals(HttpStatus.NOT_FOUND)) {
//                                throw new VerificationException("Something went wrong");
//                            } else {
//                                throw new VerificationException("Invalid token");
//                            }
//                        })
//                        .bodyToMono(Boolean.class)
//                        .flatMap(verified -> {
//                            if (verified) {
//                                Optional<AuthUser> authUserOptional = authUserRepository.findByUserId(usedIdToken);
//                                if (authUserOptional.isPresent()) {
//                                    Optional<Auth> authOptional = authRepository.findById(authUserOptional.get().getAuth().getId());
//
//                                    if (authOptional.isPresent()) {
//                                        return Mono.just(authOptional.get());
//                                    }
//                                }
//                            }
//
//                            return Mono.error(new VerificationException("Invalid/Expired token"));
//                        }))
//                .cast(Auth.class)
//                .flatMap(this::generateJWT)
//                .onErrorMap(e -> new TokenVerificationException("Unable to verify token: " + usedIdToken));
//    }

    private Mono<AccessToken> generateJWT(Auth auth) {
        return Mono.fromCallable(() -> {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(auth.getId(), auth.getPassword()));

            final UserDetails userDetails = myUserDetailsService.loadUserByUsername(auth.getId());

            final String jwt = jwtUtil.generateToken(userDetails);

            final Date jwtExpirationTime = jwtUtil.extractExpiration(jwt);

            return AccessToken.builder()
                    .accessToken(jwt)
                    .refreshToken("")
                    .expiresIn(jwtExpirationTime.toInstant().atZone(ZoneId.systemDefault()))
                    .build();
        }).onErrorMap(AuthenticationException.class, e -> new TokenVerificationException("Invalid credentials"));
    }

    private Mono<String> convertMonoObjectToString(Object object) {
        return Mono.create((monoSink) -> {
            try {
                monoSink.success(objectMapper.get().writeValueAsString(object));
            } catch (JsonProcessingException e) {
                monoSink.error(e);
            }
        }).cast(String.class);
    }
}
