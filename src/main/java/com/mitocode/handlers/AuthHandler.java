package com.mitocode.handlers;

import com.mitocode.documents.User;
import com.mitocode.repositories.UserRepository;
import com.mitocode.security.AuthRequest;
import com.mitocode.security.AuthResponse;
import com.mitocode.security.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.springframework.web.reactive.function.BodyInserters.fromValue;

@Component
public class AuthHandler {

    private final Logger logger = LoggerFactory.getLogger(AuthHandler.class);

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Autowired
    public AuthHandler(UserRepository userRepository, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
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
}
