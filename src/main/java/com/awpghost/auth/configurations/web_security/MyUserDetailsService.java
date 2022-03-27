package com.awpghost.auth.configurations.web_security;

import com.awpghost.auth.dto.responses.UserInfoDto;
import com.awpghost.auth.persistence.models.Auth;
import com.awpghost.auth.persistence.models.Privilege;
import com.awpghost.auth.persistence.models.Role;
import com.awpghost.auth.persistence.repositories.AuthRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Log4j2
@Service
@Transactional
public class MyUserDetailsService implements UserDetailsService {

    private final WebClient.Builder webClientBuilder;

    private final AuthRepository authRepository;

    private final LoginAttemptService loginAttemptService;

    private final HttpServletRequest request;

    @Autowired
    public MyUserDetailsService(WebClient.Builder webClientBuilder, @Lazy AuthRepository authRepository, LoginAttemptService loginAttemptService, HttpServletRequest request) {
        super();
        this.webClientBuilder = webClientBuilder;
        this.authRepository = authRepository;
        this.loginAttemptService = loginAttemptService;
        this.request = request;
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        final String ip = getClientIP();

        if (loginAttemptService.isBlocked(ip)) {
            throw new RuntimeException("blocked");
        }

        WebClient webClient = webClientBuilder.baseUrl("http://user/").build();

        return webClient.get()
                .uri("/user/{username}", username)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .retrieve()
                .bodyToMono(UserInfoDto.class)
                .flatMap(userInfoDto -> {
                    Optional<Auth> authOptional = authRepository.findById(userInfoDto.getId());

                    if (authOptional.isPresent()) {
                        Auth auth = authOptional.get();

                        return Mono.just(new User(auth.getId(), auth.getPassword(), getUserAuthorities(auth.getRoles())));
                    } else {
                        return Mono.error(new UsernameNotFoundException("User not found"));
                    }
                }).block();
    }

    // List of Roles >> Privileges >> List of GrantedAuthorities in Spring Security.
    private Collection<? extends GrantedAuthority> getUserAuthorities(Collection<Role> roles) {
        return getUserGrantedAuthorities(getPrivileges(roles));
    }

    private List<String> getPrivileges(Collection<Role> roles) {
        List<String> privileges = new ArrayList<>();
        List<Privilege> collection = new ArrayList<>();
        for (Role role : roles) {
            collection.addAll(role.getPrivileges());
        }
        for (Privilege item : collection) {
            privileges.add(item.getName());
        }
        return privileges;
    }

    private List<GrantedAuthority> getUserGrantedAuthorities(List<String> privileges) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        for (String privilege : privileges) {
            authorities.add(new SimpleGrantedAuthority(privilege));
        }
        return authorities;
    }

    private String getClientIP() {
        final String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader != null) {
            return xfHeader.split(",")[0];
        }
        return request.getRemoteAddr();
    }

}
