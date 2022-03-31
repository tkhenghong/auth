package com.awpghost.auth.services.auth;

import com.awpghost.auth.configurations.web_security.MyUserDetailsService;
import com.awpghost.auth.dto.requests.*;
import com.awpghost.auth.dto.responses.AccessToken;
import com.awpghost.auth.dto.responses.OTPResponse;
import com.awpghost.auth.dto.responses.UserInfoDto;
import com.awpghost.auth.enums.VerificationType;
import com.awpghost.auth.exceptions.RegistrationException;
import com.awpghost.auth.exceptions.TokenVerificationException;
import com.awpghost.auth.exceptions.UserNotFoundException;
import com.awpghost.auth.persistence.models.Auth;
import com.awpghost.auth.persistence.models.Role;
import com.awpghost.auth.persistence.models.relationships.AuthUser;
import com.awpghost.auth.persistence.repositories.AuthRepository;
import com.awpghost.auth.persistence.repositories.AuthUserRepository;
import com.awpghost.auth.persistence.repositories.RoleRepository;
import com.awpghost.auth.services.user.UserClient;
import com.awpghost.auth.utils.jwt.JwtUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.ZoneId;
import java.util.Collections;
import java.util.Date;
import java.util.NoSuchElementException;
import java.util.Optional;
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

    private final MyUserDetailsService myUserDetailsService;

    private final JwtUtil jwtUtil;

    private final AuthenticationManager authenticationManager;

    private final AtomicReference<ObjectMapper> objectMapper;

    @Autowired(required = false)
    private UserClient userClient;

    @Autowired
    AuthServiceImpl(@Lazy AuthRepository authRepository,
                    @Lazy AuthUserRepository authUserRepository,
                    @Lazy RoleRepository roleRepository,
                    @Lazy MyUserDetailsService myUserDetailsService,
                    JwtUtil jwtUtil,
                    PasswordEncoder passwordEncoder,
                    ObjectMapper objectMapper,
                    AuthenticationManager authenticationManager,
                    KafkaTemplate<String, String> kafkaTemplate) {
        this.authRepository = authRepository;
        this.authUserRepository = authUserRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.kafkaTemplate = kafkaTemplate;
        this.myUserDetailsService = myUserDetailsService;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
        this.objectMapper = new AtomicReference<>(objectMapper);
    }

    @Override
    public Mono<Auth> registerByEmail(AuthEmailDto authEmailDto) {

        return userClient.getUserByEmail(authEmailDto.getEmail())
                .onErrorResume(WebClientResponseException.class, clientResponse -> {
                    if (clientResponse.getStatusCode().equals(HttpStatus.NOT_FOUND)) {
                        UserDto userDto = UserDto.builder()
                                .email(authEmailDto.getEmail())
                                .build();
                        return userClient.registerUser(userDto);
                    } else {
                        log.info("Unable to register user with email address: {}", authEmailDto.getEmail());
                        return Mono.error(new RegistrationException("Unable to register user with email address: " + authEmailDto.getEmail()));
                    }
                })
                .flatMap(userInfoDto -> createAuthFromUser(userInfoDto, VerificationType.EMAIL));
    }

    @Override
    public Mono<Void> registerByMobileNo(AuthMobileNoDto authMobileNoDto) {
        return userClient.getUserByEmail(authMobileNoDto.getMobileNo())
                .onErrorResume(WebClientResponseException.class, clientResponse -> {
                    if (clientResponse.getStatusCode().equals(HttpStatus.NOT_FOUND)) {
                        UserDto userDto = UserDto.builder()
                                .mobileNo(authMobileNoDto.getMobileNo())
                                .nationality(authMobileNoDto.getNationality())
                                .build();
                        return userClient.registerUser(userDto);
                    } else {
                        log.info("Unable to register user with mobile number: {}", authMobileNoDto.getMobileNo());
                        return Mono.error(new RegistrationException("Unable to register user with mobile number: {}" + authMobileNoDto.getMobileNo()));
                    }
                })
                .flatMap(userInfoDto -> createAuthFromUser(userInfoDto, VerificationType.MOBILE_NUMBER)).then();
    }

    @Override
    public Mono<AccessToken> loginByEmail(AuthEmailDto authEmailDto) {
        return userClient.getUserByEmail(authEmailDto.getEmail())
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
        return userClient.getUserByMobileNo(authMobileNoDto.getMobileNo())
                .flatMap(userInfoDto -> Mono.just(authRepository.findById(userInfoDto.getId()).get())).cast(Auth.class)
                .flatMap(auth -> Mono.just(authUserRepository.findById(auth.getId()).get())).cast(AuthUser.class)
                .onErrorMap(NoSuchElementException.class, e -> new UserNotFoundException("User not found"))
                .flatMap(authUser -> userClient.generateMobileNumberOTP(authUser.getUserId())).cast(OTPResponse.class);
    }

    @Override
    public Mono<Boolean> emailForgotPassword(String email) {
        return userClient.generateEmailToken(email);
    }

    @Override
    public Mono<Boolean> emailResetPassword(ResetPasswordDto resetPasswordDto) {
        return userClient.getUserById(resetPasswordDto.getToken()).flatMap(userInfoDto -> {
            Auth auth = authRepository.findById(userInfoDto.getId()).get();
            auth.setPassword(passwordEncoder.encode(resetPasswordDto.getPassword()));

            authRepository.save(auth);

            return convertMonoObjectToString(PasswordResetDto.builder().email(userInfoDto.getEmail()).build()).flatMap(converted -> {
                kafkaTemplate.send("email.password.reset", converted);
                return Mono.just(true);
            });
        }).onErrorReturn(false);
    }

    @Override
    public Mono<Boolean> emailChangePassword(ChangePasswordDto changePasswordDto) {
        return Mono.fromCallable(() -> {
            UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
            UserDetails userDetails = (UserDetails) usernamePasswordAuthenticationToken.getPrincipal();

            Optional<Auth> authOptional = authRepository.findById(userDetails.getUsername());

            if (authOptional.isPresent()) {
                Auth auth = authOptional.get();

                auth.setPassword(passwordEncoder.encode(changePasswordDto.getPassword()));

                authRepository.save(auth);

                return true;
            } else {
                throw new UserNotFoundException("User not found");
            }
        }).onErrorReturn(false);
    }

    @Override
    public Mono<Void> logout() {
        return null;
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
