package com.jse.configs;

import com.jse.EnrollmentManagementApplication;
import com.jse.documents.Role;
import com.jse.documents.User;
import com.jse.handlers.AuthHandler;
import com.jse.repositories.RoleRepository;
import com.jse.repositories.UserRepository;
import com.jse.security.AuthRequest;
import com.jse.security.AuthResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = EnrollmentManagementApplication.class)
@Import(AuthHandler.class)
class AuthFunctionalTest {

    @Autowired
    private RouterConfig config;

    @Autowired
    private AuthHandler authHandler;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private RoleRepository roleRepository;

    @Test
    void givenValidCredentials_whenLogin_thenTokenReturned() {
        WebTestClient client = WebTestClient.bindToRouterFunction(config.authRoutes(authHandler)).build();

        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String rawPassword = "secret123";
        User user = new User("u-1", "jimmy", passwordEncoder.encode(rawPassword), true, List.of(new Role("r-1", "USER")));
        AuthRequest request = new AuthRequest();
        request.setUsername("jimmy");
        request.setPassword(rawPassword);

        given(userRepository.findOneByUsername("jimmy")).willReturn(Mono.just(user));

        client.post()
                .uri("/api/v1/auth/sign-in")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().exists("Authorization")
                .expectBody(AuthResponse.class)
                .value(response -> {
                    assertEquals("jimmy", response.getUsername());
                    assertNotNull(response.getToken());
                });
    }

    @Test
    void givenInvalidCredentials_whenLogin_thenUnauthorized() {
        WebTestClient client = WebTestClient.bindToRouterFunction(config.authRoutes(authHandler)).build();

        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        User user = new User("u-1", "jimmy", passwordEncoder.encode("secret123"), true, List.of(new Role("r-1", "USER")));
        AuthRequest request = new AuthRequest();
        request.setUsername("jimmy");
        request.setPassword("wrong-password");

        given(userRepository.findOneByUsername("jimmy")).willReturn(Mono.just(user));

        client.post()
                .uri("/api/v1/auth/sign-in")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isUnauthorized()
                .expectBody(String.class)
                .isEqualTo("Invalid credentials");
    }

    @Test
    void givenNewUser_whenRegister_thenUserCreated() {
        WebTestClient client = WebTestClient.bindToRouterFunction(config.authRoutes(authHandler)).build();

        User user = new User(null, "newuser", "secret123", true, null);
        Role defaultRole = new Role("r-1", "USER");

        given(userRepository.findOneByUsername("newuser")).willReturn(Mono.empty());
        given(roleRepository.findOneByName("USER")).willReturn(Mono.just(defaultRole));
        given(roleRepository.save(any(Role.class))).willAnswer(invocation -> {
            Role role = invocation.getArgument(0);
            role.setId("r-1");
            return Mono.just(role);
        });
        given(userRepository.save(any(User.class))).willAnswer(invocation -> {
            User saved = invocation.getArgument(0);
            saved.setId("u-2");
            saved.setRoles(List.of(defaultRole));
            return Mono.just(saved);
        });

        client.post()
                .uri("/api/v1/auth/sign-up")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(user)
                .exchange()
                .expectStatus().isOk()
                .expectBody(User.class)
                .value(response -> {
                    assertEquals("newuser", response.getUsername());
                    assertNotNull(response.getPassword());
                    assertEquals(Boolean.TRUE, response.getStatus());
                    assertNotNull(response.getRoles());
                });
    }

    @Test
    void givenExistingUsername_whenRegister_thenConflict() {
        WebTestClient client = WebTestClient.bindToRouterFunction(config.authRoutes(authHandler)).build();

        User user = new User(null, "jimmy", "secret123", true, null);

        given(userRepository.findOneByUsername("jimmy")).willReturn(Mono.just(user));

        client.post()
                .uri("/api/v1/auth/sign-up")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(user)
                .exchange()
                .expectStatus().isEqualTo(409)
                .expectBody(String.class)
                .isEqualTo("Username already exists");
    }
}
