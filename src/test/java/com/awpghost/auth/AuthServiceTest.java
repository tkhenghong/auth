package com.awpghost.auth;

import com.awpghost.auth.controllers.AuthController;
import com.awpghost.auth.dto.requests.AuthEmailDto;
import com.awpghost.auth.dto.requests.AuthMobileNoDto;
import com.awpghost.auth.dto.responses.UserInfoDto;
import com.awpghost.auth.persistence.models.Auth;
import com.awpghost.auth.persistence.models.Privilege;
import com.awpghost.auth.persistence.models.Role;
import com.awpghost.auth.persistence.models.relationships.AuthUser;
import com.awpghost.auth.persistence.repositories.PrivilegeRepository;
import com.awpghost.auth.persistence.repositories.RoleRepository;
import com.awpghost.auth.services.auth.AuthService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javafaker.Faker;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.client.HttpClientErrorException;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;

/**
 * https://stackoverflow.com/questions/61433806/junit-5-with-spring-boot-when-to-use-extendwith-spring-or-mockito
 */

@ExtendWith(SpringExtension.class)
@WebFluxTest(controllers = AuthController.class)
@Import(AuthService.class)
@AutoConfigureWebTestClient
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
public class AuthServiceTest {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @MockBean
    private RoleRepository roleRepository;

    @MockBean
    private PrivilegeRepository privilegeRepository;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @MockBean
    private AuthService authService;

    @Autowired
    private WebTestClient webClient;

    @Autowired
    ObjectMapper objectMapper;

    private final Faker faker = new Faker();

    public final MockWebServer mockBackEnd = new MockWebServer();

    @BeforeEach
    public void setup() throws IOException {
        mockBackEnd.start();
    }

    @AfterEach
    public void tearDown() throws IOException {
        mockBackEnd.shutdown();
    }

    @BeforeEach
    public void setupMocks() {
        MockitoAnnotations.openMocks(this);
    }

    Privilege generatePrivilege() {
        return Privilege.builder()
                .id(UUID.randomUUID().toString())
                .arangoId(UUID.randomUUID().toString())
                .name(faker.name().name())
                .build();
    }

    Role generateRole(Collection<Privilege> privileges) {
        return Role.builder()
                .id(UUID.randomUUID().toString())
                .arangoId(UUID.randomUUID().toString())
                .privileges(privileges)
                .name(faker.name().name())
                .build();
    }

    AuthUser generateAuthUser(Auth auth, String userId) {
        return AuthUser.builder()
                .auth(auth)
                .userId(userId)
                .build();
    }

    Auth generateAuth(Collection<Role> roles) {
        return Auth.builder()
                .roles(roles)
                .build();
    }

    UserInfoDto generateUserInfoDto() {
        UserInfoDto userInfoDto = new UserInfoDto();
        userInfoDto.setEmail(faker.internet().emailAddress());
        userInfoDto.setFirstName(faker.name().firstName());
        userInfoDto.setLastName(faker.name().lastName());
        userInfoDto.setNationality(faker.address().country());
        return userInfoDto;
    }

    AuthEmailDto generateAuthEmailDto() {
        AuthEmailDto authEmailDto = new AuthEmailDto();
        authEmailDto.setEmail(faker.internet().emailAddress());
        authEmailDto.setPassword(faker.internet().password());
        return authEmailDto;
    }

    AuthMobileNoDto generateAuthMobileNoDto() {
        AuthMobileNoDto authMobileNoDto = new AuthMobileNoDto();
        authMobileNoDto.setMobileNo(faker.phoneNumber().cellPhone());
        authMobileNoDto.setNationality(faker.address().country());
        return authMobileNoDto;
    }

    @Test
    public void testRegisterByEmail() throws JsonProcessingException, InterruptedException {
        AuthEmailDto authEmailDto = generateAuthEmailDto();
//        UserInfoDto userInfoDto = generateUserInfoDto();
        Privilege privilege1 = generatePrivilege();
        Privilege privilege2 = generatePrivilege();
        Role role1 = generateRole(Arrays.asList(privilege1, privilege2));
        Role role2 = generateRole(Arrays.asList(privilege1, privilege2));

        Auth auth = generateAuth(Arrays.asList(role1, role2));

        String getUserByEmailUrl = "http://user?email=" + authEmailDto.getEmail();

        mockBackEnd.enqueue(new MockResponse()
                .setResponseCode(HttpStatus.SC_NOT_FOUND)
                .addHeader("Content-Type", "application/json"));

        StepVerifier.create(authService.registerByEmail(authEmailDto))
                .assertNext(Assertions::assertNull)
                .verifyError(HttpClientErrorException.class);

        RecordedRequest request = mockBackEnd.takeRequest();
    }

}
