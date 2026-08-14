package com.mitocode.handlers;

import com.mitocode.documents.User;
import com.mitocode.documents.Role;
import com.mitocode.documents.User;
import com.mitocode.repositories.RoleRepository;
import com.mitocode.repositories.UserRepository;
import com.mitocode.security.AuthRequest;
import com.mitocode.security.AuthResponse;
import com.mitocode.security.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;

import static org.springframework.web.reactive.function.BodyInserters.fromValue;

@Component
public class AuthHandler {

    private final Logger logger = LoggerFactory.getLogger(AuthHandler.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Autowired
    public AuthHandler(UserRepository userRepository, RoleRepository roleRepository, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.jwtUtil = jwtUtil;
    }

    public Mono<ServerResponse> login(ServerRequest req) {
        Mono<AuthRequest> monoReq = req.bodyToMono(AuthRequest.class);
        return monoReq.flatMap(ar -> userRepository.findOneByUsername(ar.getUsername())
                .flatMap(user -> {
                    if (user.getPassword() == null || !passwordEncoder.matches(ar.getPassword(), user.getPassword())) {
                        return ServerResponse.status(401).contentType(MediaType.TEXT_PLAIN).body(fromValue("Invalid credentials"));
                    }
                    if (user.getStatus() != null && !user.getStatus()) {
                        return ServerResponse.status(403).contentType(MediaType.TEXT_PLAIN).body(fromValue("User disabled"));
                    }
                    String token = jwtUtil.generateToken(user);
                    AuthResponse resp = new AuthResponse(user.getUsername(), token);
                    return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).body(fromValue(resp));
                })
                .switchIfEmpty(ServerResponse.status(401).contentType(MediaType.TEXT_PLAIN).body(fromValue("Invalid credentials")))
        );
    }

    public Mono<ServerResponse> register(ServerRequest req) {
        Mono<User> monoUser = req.bodyToMono(User.class);
        return monoUser.flatMap(u ->
            userRepository.findOneByUsername(u.getUsername())
                .flatMap(existing -> ServerResponse.status(409).contentType(MediaType.TEXT_PLAIN).body(fromValue("Username already exists")))
                .switchIfEmpty(Mono.defer(() -> {
                    // encode password
                    u.setPassword(passwordEncoder.encode(u.getPassword()));
                    u.setStatus(true);
                    if (u.getRoles() == null || u.getRoles().isEmpty()) {
                        return roleRepository.findOneByName("USER")
                                .switchIfEmpty(roleRepository.save(new Role(null, "USER")))
                                .flatMap(role -> {
                                    u.setRoles(List.of(role));
                                    return userRepository.save(u).flatMap(saved -> ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).body(fromValue(saved)));
                                });
                    } else {
                        return Flux.fromIterable(u.getRoles())
                                .flatMap(r -> roleRepository.findOneByName(r.getName()).switchIfEmpty(roleRepository.save(new Role(null, r.getName()))))
                                .collectList()
                                .flatMap(foundRoles -> {
                                    u.setRoles(foundRoles);
                                    return userRepository.save(u).flatMap(saved -> ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).body(fromValue(saved)));
                                });
                    }
                }))
        );
    }
}
